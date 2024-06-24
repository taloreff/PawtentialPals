// PostAdapter.kt
package com.example.pawtentialpals.adapters

import ImageSliderAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.HomeItemPostBinding
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.viewModels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(private val postList: List<PostModel>, private val viewModel: HomeViewModel) :
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
            binding.username.text = post.userName
            binding.postTime.text = formatTimestamp(post.timestamp)
            binding.postContent.text = post.description
            binding.postLocation.text = post.location
            binding.likes.text = post.likes.toString()
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
                    viewModel.likePost(post, userId)
                }
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
