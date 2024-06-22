package com.example.pawtentialpals.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.HomeItemPostBinding
import com.example.pawtentialpals.models.PostModel
import java.text.SimpleDateFormat
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
            binding.username.text = post.userName
            binding.postTime.text = formatTimestamp(post.timestamp)
            binding.postContent.setText(post.description)
            binding.likes.text = post.likes.toString()
            binding.comments.text = post.comments.size.toString()
            binding.userImage.load(post.userImage) {
                placeholder(R.drawable.batman)
                error(R.drawable.batman)
            }
            binding.postImage.load(post.postImage) {
                placeholder(R.drawable.batman)
                error(R.drawable.batman)
            }


        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
