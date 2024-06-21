package com.example.pawtentialpals.models

data class PostModel(
    val id: Int,
    val username: String,
    val userImage: Int,
    val postTime: String,
    val postContent: String,
    val postImage: Int,
    val likes: Int,
    val comments: Int
)
