package com.example.pawtentialpals.models

data class UserModel(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var image: String = DEFAULT_IMAGE,
    var posts: ArrayList<PostModel> = ArrayList(),
    var likedPosts: ArrayList<String> = ArrayList(),
) {
    companion object {
        private const val DEFAULT_IMAGE = "drawable/batman.png"
    }
}
