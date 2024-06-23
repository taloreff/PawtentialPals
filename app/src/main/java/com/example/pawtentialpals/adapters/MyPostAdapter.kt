package com.example.pawtentialpals.adapters

import ImageSliderAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.MyItemPostBinding
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyPostAdapter(private val postList: MutableList<PostModel>) :
    RecyclerView.Adapter<MyPostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = MyItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.bind(currentPost)
    }

    override fun getItemCount() = postList.size

    inner class PostViewHolder(private val binding: MyItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostModel) {
            binding.username.text = post.userName
            binding.postTime.text = formatTimestamp(post.timestamp)
            binding.postContent.setText(post.description)
            binding.likes.text = post.likes.toString()
            binding.comments.text = post.comments.size.toString()
            binding.userImage.load(post.userImage) {
                placeholder(R.drawable.batman)
                error(R.drawable.batman)
            }

            val imageUrls = listOf(post.postImage, post.mapImage)
            binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)

            binding.editButton.setOnClickListener {
                if (binding.editButton.text == "Edit") {
                    enableEditing(true)
                    binding.editButton.text = "Save"
                } else {
                    enableEditing(false)
                    binding.editButton.text = "Edit"
                    updatePost(post)
                }
            }

            binding.deleteButton.setOnClickListener {
                deletePost(post)
            }
        }

        private fun enableEditing(enable: Boolean) {
            binding.postContent.isEnabled = enable
            binding.postContent.isFocusable = enable
            binding.postContent.isFocusableInTouchMode = enable
            binding.postContent.isCursorVisible = enable
            if (enable) {
                binding.postContent.requestFocus()
            }
        }

        private fun updatePost(post: PostModel) {
            val updatedDescription = binding.postContent.text.toString()
            val db = FirebaseFirestore.getInstance()

            db.collection("posts").document(post.id)
                .update("description", updatedDescription)
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "Post updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "Failed to update post", Toast.LENGTH_SHORT).show()
                }

            db.collection("users").document(post.userId).get()
                .addOnSuccessListener { document ->
                    val posts = document.toObject(User::class.java)?.posts ?: return@addOnSuccessListener
                    val updatedPosts = posts.map {
                        if (it.id == post.id) it.copy(description = updatedDescription) else it
                    }

                    db.collection("users").document(post.userId)
                        .update("posts", updatedPosts)
                        .addOnSuccessListener {
                            Toast.makeText(binding.root.context, "User post updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(binding.root.context, "Failed to update user post", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        private fun deletePost(post: PostModel) {
            val db = FirebaseFirestore.getInstance()
            val postId = post.id
            val userId = post.userId

            // First, remove the post from the user's posts array
            val userRef = db.collection("users").document(userId)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    val updatedPosts = it.posts.filter { it.id != postId }
                    userRef.update("posts", updatedPosts)
                        .addOnSuccessListener {
                            // If successful, delete the post from the posts collection
                            db.collection("posts").document(postId).delete()
                                .addOnSuccessListener {
                                    onPostDeleted(post)
                                    Toast.makeText(binding.root.context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(binding.root.context, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(binding.root.context, "Failed to update user posts: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(binding.root.context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun onPostDeleted(post: PostModel) {
            val position = postList.indexOf(post)
            if (position != -1) {
                postList.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val posts: List<PostModel> = listOf()
)
