package com.example.pawtentialpals.ui

data class Post(
    val postId: String = "",
    val userId: String = "",
    val postText: String = "",
    val postImages: List<String> = listOf(),
    val postLikes: Int = 0,
    val postComments: Int = 0
) : java.io.Serializable