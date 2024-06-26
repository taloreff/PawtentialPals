package com.example.pawtentialpals.viewModels

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.example.pawtentialpals.services.UploadService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddPostViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _postCreationSuccess = MutableLiveData<Boolean>()
    val postCreationSuccess: LiveData<Boolean> get() = _postCreationSuccess

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun postToFirestore(description: String, selectedImageUri: Uri, mapImageUrl: String, selectedLocation: String) {
        _loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val imageUrl = uploadImage(selectedImageUri)
            if (imageUrl != null) {
                fetchUserDataAndSavePost(description, selectedLocation, imageUrl, mapImageUrl)
            } else {
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to upload image"
                    _loading.value = false
                }
            }
        }
    }

    private fun uploadImage(uri: Uri): String? {
        val filePath = getPathFromUri(uri) ?: return null
        return UploadService.uploadImg(filePath)
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = getApplication<Application>().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun fetchUserDataAndSavePost(description: String, location: String, imageUrl: String, mapImageUrl: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserModel::class.java) ?: return@addOnSuccessListener
                val userName = user.name
                val userImage = user.image

                savePostToFirestore(userId, userName, Uri.parse(userImage), description, location, imageUrl, mapImageUrl)
            }
            .addOnFailureListener {
                _error.postValue("Failed to fetch user data")
                _loading.value = false
            }
    }

    private fun savePostToFirestore(userId: String, userName: String, userImage: Uri, description: String, location: String, imageUrl: String, mapImageUrl: String) {
        val postId = UUID.randomUUID().toString()
        val currentTimestamp = System.currentTimeMillis()

        val post = PostModel(
            id = postId,
            userId = userId,
            userName = userName,
            userImage = userImage.toString(),
            timestamp = currentTimestamp,
            description = description,
            location = location,
            postImage = imageUrl,
            mapImage = mapImageUrl,
            likes = 0,
            comments = emptyList()
        )

        firestore.collection("posts").document(postId).set(post)
            .addOnSuccessListener {
                firestore.collection("users").document(userId)
                    .update("posts", com.google.firebase.firestore.FieldValue.arrayUnion(post))
                    .addOnSuccessListener {
                        _postCreationSuccess.postValue(true)
                        _loading.value = false
                    }
                    .addOnFailureListener {
                        _error.postValue("Failed to save post to user")
                        _loading.value = false
                    }
            }
            .addOnFailureListener {
                _error.postValue("Failed to create post")
                _loading.value = false
            }
    }

    fun resetPostCreationSuccess() {
        _postCreationSuccess.value = false
    }
}
