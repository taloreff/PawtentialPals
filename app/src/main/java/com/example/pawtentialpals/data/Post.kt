package com.example.pawtentialpals.data
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirestoreUtilities

data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrls: List<String> = listOf(),
    val createdAt: String = ""
) {
    object FirestoreUtilities : FirestoreUtilities(id)
}