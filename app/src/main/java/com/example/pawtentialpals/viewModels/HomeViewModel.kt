package com.example.pawtentialpals.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.services.PostRepository
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val postRepository = PostRepository(application)

    private val _posts = MutableLiveData<List<PostModel>>()
    val posts: LiveData<List<PostModel>> get() = _posts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loadPosts() {
        _loading.value = true

        // Load posts from SQLite
        val localPosts = postRepository.getAllPosts()
        if (localPosts.isNotEmpty()) {
            _posts.value = localPosts.sortedByDescending { it.timestamp }
        }

        // Sync with Firestore
        firestore.collection("posts").get()
            .addOnSuccessListener { result ->
                val posts = result.mapNotNull { it.toObject(PostModel::class.java) }
                val sortedPosts = posts.sortedByDescending { it.timestamp }
                _posts.value = sortedPosts
                sortedPosts.forEach { postRepository.insertPost(it) } // Save to SQLite
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _loading.value = false
            }
    }

    fun likePost(post: PostModel, userId: String) {
        val postRef = firestore.collection("posts").document(post.id)
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likes") ?: 0

            val userSnapshot = transaction.get(userRef)
            val likedPosts = userSnapshot.get("likedPosts") as? List<String> ?: emptyList()

            if (likedPosts.contains(post.id)) {
                transaction.update(postRef, "likes", currentLikes - 1)
                transaction.update(userRef, "likedPosts", com.google.firebase.firestore.FieldValue.arrayRemove(post.id))
            } else {
                transaction.update(postRef, "likes", currentLikes + 1)
                transaction.update(userRef, "likedPosts", com.google.firebase.firestore.FieldValue.arrayUnion(post.id))
            }
        }.addOnSuccessListener {
            loadPosts()
        }
    }
}
