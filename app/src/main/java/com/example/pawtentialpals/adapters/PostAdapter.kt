package com.example.pawtentialpals.adapters

import ImageSliderAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.HomeItemPostBinding
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import coil.transform.CircleCropTransformation
import java.util.*

class PostAdapter(private val postList: List<PostModel>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = HomeItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.bind(currentPost)
    }

    override fun getItemCount() = postList.size

    inner class PostViewHolder(private val binding: HomeItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostModel) {
            var currentLikes = post.likes

            binding.username.text = post.userName
            binding.postTime.text = formatTimestamp(post.timestamp)
            binding.postContent.text = post.description
            binding.postLocation.text = post.location
            binding.likes.text = currentLikes.toString()
            binding.comments.text = post.comments.size.toString()
            binding.userImage.load(post.userImage) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
            }

            val imageUrls = listOf(post.postImage, post.mapImage)
            binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)

            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get().addOnSuccessListener { document ->
                        val likedPosts = document.get("likedPosts") as? List<String> ?: emptyList()
                        if (likedPosts.contains(post.id)) {
                            binding.likeIcon.setImageResource(R.drawable.full_like)
                        } else {
                            binding.likeIcon.setImageResource(R.drawable.like)
                        }
                    }
            }

            binding.likesContainer.setOnClickListener {
                if (userId != null) {
                    handleLike(post, userId, currentLikes)
                }
            }
        }

        private fun handleLike(post: PostModel, userId: String, currentLikes: Int) {
            val db = FirebaseFirestore.getInstance()
            val postRef = db.collection("posts").document(post.id)
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes") ?: 0

                val userSnapshot = transaction.get(userRef)
                val likedPosts = userSnapshot.get("likedPosts") as? List<String> ?: emptyList()

                if (likedPosts.contains(post.id)) {
                    transaction.update(postRef, "likes", currentLikes - 1)
                    transaction.update(userRef, "likedPosts", com.google.firebase.firestore.FieldValue.arrayRemove(post.id))
                    currentLikes - 1
                } else {
                    transaction.update(postRef, "likes", currentLikes + 1)
                    transaction.update(userRef, "likedPosts", com.google.firebase.firestore.FieldValue.arrayUnion(post.id))
                    currentLikes + 1
                }
            }.addOnSuccessListener { newLikes ->
                binding.likes.text = newLikes.toString()
                updateLikeIcon(post, userId)
            }.addOnFailureListener { e ->
                Toast.makeText(binding.root.context, "Failed to update like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun updateLikeIcon(post: PostModel, userId: String) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener { document ->
                    val likedPosts = document.get("likedPosts") as? List<String> ?: emptyList()
                    if (likedPosts.contains(post.id)) {
                        binding.likeIcon.setImageResource(R.drawable.full_like)
                    } else {
                        binding.likeIcon.setImageResource(R.drawable.like)
                    }
                }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
