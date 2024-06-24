// MyPostsViewModel.kt
package com.example.pawtentialpals.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPostsViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<PostModel>>()
    val posts: LiveData<List<PostModel>> get() = _posts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadUserPosts() {
        _loading.value = true
        val userId = firebaseAuth.currentUser?.uid ?: run {
            _error.value = "User not authenticated"
            _loading.value = false
            return
        }

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.mapNotNull { it.toObject(PostModel::class.java) }
                _posts.value = posts
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _loading.value = false
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
                _error.value = e.message
                _loading.value = false
            }
    }

    fun deletePost(post: PostModel) {
        val db = firestore
        val postId = post.id
        val userId = post.userId

        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(UserModel::class.java)
            user?.let {
                val updatedPosts = it.posts.filter { it.id != postId }
                userRef.update("posts", updatedPosts)
                    .addOnSuccessListener {
                        db.collection("posts").document(postId).delete()
                            .addOnSuccessListener {
                                loadUserPosts()
                            }
                            .addOnFailureListener { e ->
                                _error.value = e.message
                                _loading.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = e.message
                        _loading.value = false
                    }
            }
        }.addOnFailureListener { e ->
            _error.value = e.message
            _loading.value = false
        }
    }
}
