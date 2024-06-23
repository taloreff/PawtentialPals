package com.example.pawtentialpals.models

data class PostModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val timestamp: Long = 0L,
    val description: String = "",
    val location: String = "",
    val postImage: String = "",
    val mapImage: String = "",
    val likes: Int = 0,
    val comments: List<Comment> = listOf()
)

data class Comment(
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val comment: String = "",
    val timestamp: Long = 0L
)
