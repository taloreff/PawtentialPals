package com.example.pawtentialpals.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class MyPostsViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<PostModel>>()
    val posts: LiveData<List<PostModel>> get() = _posts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadUserPosts() {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            _error.value = "User not authenticated"
            return
        }

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.mapNotNull { it.toObject(PostModel::class.java) }
                _posts.value = posts
            }
            .addOnFailureListener { e ->
                handleFirestoreError(e)
            }
    }

    fun updatePost(post: PostModel, newDescription: String) {
        val db = firestore
        db.collection("posts").document(post.id)
            .update("description", newDescription)
            .addOnSuccessListener {
                loadUserPosts()
            }
            .addOnFailureListener { e ->
                handleFirestoreError(e)
            }
    }

    fun deletePost(post: PostModel) {
        val db = firestore
        val postId = post.id
        val userId = post.userId

        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(UserModel::class.java)
            user?.let { it ->
                val updatedPosts = it.posts.filter { it.id != postId }
                userRef.update("posts", updatedPosts)
                    .addOnSuccessListener {
                        db.collection("posts").document(postId).delete()
                            .addOnSuccessListener {
                                loadUserPosts()
                            }
                            .addOnFailureListener { e ->
                                handleFirestoreError(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        handleFirestoreError(e)
                    }
            } ?: run {
                _error.value = "User data not found"
            }
        }.addOnFailureListener { e ->
            handleFirestoreError(e)
        }
    }

    private fun handleFirestoreError(e: Exception) {
        _error.value = e.message ?: "Unknown error occurred"
    }
}
