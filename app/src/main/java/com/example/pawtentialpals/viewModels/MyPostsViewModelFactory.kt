package com.example.pawtentialpals.viewModels

import MyPostsViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyPostsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPostsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPostsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
