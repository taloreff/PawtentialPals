package com.example.pawtentialpals.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.ItemPostBinding
import com.example.pawtentialpals.models.PostModel

class PostAdapter(private val postList: List<PostModel>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.bind(currentPost)
    }

    override fun getItemCount() = postList.size

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostModel) {
            binding.username.text = post.userName
            binding.postTime.text = post.timestamp
            binding.postContent.text = post.description
            binding.likes.text = post.likes.toString()
            binding.comments.text = post.comments.size.toString()

            binding.userImage.load(post.userImage ?: R.drawable.batman) {
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
            }

            binding.postImage.load(post.postImage ?: R.drawable.batman) {
                placeholder(R.drawable.ic_post_placeholder)
                error(R.drawable.ic_post_placeholder)
            }
        }
    }
}
