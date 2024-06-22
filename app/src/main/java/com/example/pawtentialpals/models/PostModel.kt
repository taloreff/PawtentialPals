package com.example.pawtentialpals.models

data class PostModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    var timestamp: Long = 0L,
    val description: String = "",
    val location: String = "",
    val postImage: String = "",
    val likes: Int = 0,
    val comments: ArrayList<String> = ArrayList()
)
