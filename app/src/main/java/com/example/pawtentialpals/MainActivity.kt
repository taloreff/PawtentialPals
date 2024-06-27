package com.example.pawtentialpals

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pawtentialpals.databinding.ActivityMainBinding
import com.example.pawtentialpals.fragments.HomeFragment
import com.example.pawtentialpals.fragments.AddFragment
import com.example.pawtentialpals.fragments.MenuFragment
import com.example.pawtentialpals.fragments.PostDetailsFragment
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.services.PostRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.UUID

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var postRepository: PostRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        postRepository = PostRepository(this)

        // Query and log data
        val posts = postRepository.getAllPosts()
        for (post in posts) {
            Log.d("SQLiteTest", "Post ID: ${post.id}, Description: ${post.description}")
        }
        // Setup Bottom Navigation
        val navView: BottomNavigationView = binding.bottomNavView

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_add -> {
                    loadFragment(AddFragment())
                    true
                }
                R.id.nav_menu -> {
                    drawerLayout.openDrawer(binding.drawerMenu)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.nav_home
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.drawer_menu, MenuFragment())
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun navigateToPostDetails(post: PostModel) {
        val fragment = PostDetailsFragment.newInstance(post)
        loadFragment(fragment)
    }
}
