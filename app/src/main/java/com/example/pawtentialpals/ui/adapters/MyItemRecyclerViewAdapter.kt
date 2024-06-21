package com.example.pawtentialpals.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.ItemPostBinding
import com.example.pawtentialpals.models.PostModel

class MyItemRecyclerViewAdapter(private val postList: List<PostModel>) :
    RecyclerView.Adapter<MyItemRecyclerViewAdapter.PostViewHolder>() {

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
            binding.username.text = post.username
            binding.postTime.text = post.postTime
            binding.postContent.text = post.postContent
            binding.likes.text = post.likes.toString()
            binding.comments.text = post.comments.toString()
            binding.userImage.load(post.userImage) {
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
            }
            binding.postImage.load(post.postImage) {
                placeholder(R.drawable.ic_post_placeholder)
                error(R.drawable.ic_post_placeholder)
            }
        }
    }
}
