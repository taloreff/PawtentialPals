package com.example.pawtentialpals.services

import android.content.Context
import com.example.pawtentialpals.models.PostModel

class PostRepository(context: Context) {

    private val postDao = PostDao(context)

    fun insertPost(post: PostModel) {
        postDao.insertPost(post)
    }

    fun getAllPosts(): List<PostModel> {
        return postDao.getAllPosts()
    }

    fun deletePost(post: PostModel) {
        postDao.deletePost(post)
    }
}
