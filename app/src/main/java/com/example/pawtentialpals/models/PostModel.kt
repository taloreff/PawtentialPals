package com.example.pawtentialpals.models

import android.net.Uri

data class PostModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val timestamp: String = "",
    val description: String = "",
    val location: String = "",
    val postImage: String = "",
    val likes: Int = 0,
    val comments: ArrayList<String> = ArrayList()
)
