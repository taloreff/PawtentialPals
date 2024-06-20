package com.example.pawtentialpals.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawtentialpals.R

class HomeActivity : AppCompatActivity() {

    private lateinit var postsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch post data from the database
        val postsList = fetchPostsFromDatabase()

        // Create and set the adapter
        val postAdapter = PostAdapter(postsList)
        postsRecyclerView.adapter = postAdapter
    }

    // Function to fetch post data from the database
    private fun fetchPostsFromDatabase(): List<Post> {
        // Implement your logic to fetch post data from the database
        // and return a list of Post objects
        return listOf()
    }
}