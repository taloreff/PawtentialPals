package com.example.pawtentialpals.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _posts = MutableLiveData<List<PostModel>>()
    val posts: LiveData<List<PostModel>> get() = _posts
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loadPosts() {
        _loading.value = true
        firestore.collection("posts").get()
            .addOnSuccessListener { result ->
                val posts = result.mapNotNull { it.toObject(PostModel::class.java) }
                _posts.value = posts
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
