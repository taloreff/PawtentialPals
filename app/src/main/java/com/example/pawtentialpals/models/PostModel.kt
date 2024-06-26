package com.example.pawtentialpals.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
    var comments: List<Comment> = listOf()
) : Parcelable

@Parcelize
data class Comment(
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val comment: String = "",
    val timestamp: Long = 0L
) : Parcelable {
    companion object {
        fun fromString(commentString: String): Comment {
            val parts = commentString.split("|")
            return Comment(
                userId = parts.getOrNull(0) ?: "",
                userName = parts.getOrNull(1) ?: "",
                userImage = parts.getOrNull(2) ?: "",
                comment = parts.getOrNull(3) ?: "",
                timestamp = parts.getOrNull(4)?.toLongOrNull() ?: 0L
            )
        }

        fun toString(comment: Comment): String {
            return "${comment.userId}|${comment.userName}|${comment.userImage}|${comment.comment}|${comment.timestamp}"
        }
    }
}

