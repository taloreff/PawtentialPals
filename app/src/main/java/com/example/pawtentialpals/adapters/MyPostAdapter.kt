package com.example.pawtentialpals.adapters

import ImageSliderAdapter
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.MyItemPostBinding
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.example.pawtentialpals.viewModels.MyPostsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyPostAdapter(private val postList: MutableList<PostModel>, private val viewModel: MyPostsViewModel) :
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

    @SuppressLint("NotifyDataSetChanged")
    fun updatePosts(newPosts: List<PostModel>) {
        postList.clear()
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }

    inner class PostViewHolder(private val binding: MyItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostModel) {
            FirebaseFirestore.getInstance().collection("users").document(post.userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(UserModel::class.java)
                    binding.username.text = user?.name ?: post.userName
                    binding.userImage.load(user?.image ?: post.userImage) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.batman)
                        error(R.drawable.batman)
                    }
                }

            binding.postTime.text = formatTimestamp(post.timestamp)
            binding.postContent.text = post.description
            binding.postLocation.text = post.location
            binding.likes.text = post.likes.toString()
            binding.comments.text = post.comments.size.toString()

            val imageUrls = listOf(post.postImage, post.mapImage)
            binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)

            binding.editButton.setOnClickListener {
                if (binding.editButton.text == "Edit") {
                    enableEditing(true)
                    binding.editButton.text = "Save"
                } else {
                    enableEditing(false)
                    binding.editButton.text = "Edit"
                    viewModel.updatePost(post, binding.postContent.text.toString())
                }
            }

            binding.deleteButton.setOnClickListener {
                viewModel.deletePost(post)
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

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
