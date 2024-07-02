package com.example.pawtentialpals

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.pawtentialpals.databinding.ActivityMainBinding
import com.example.pawtentialpals.fragments.DrawerCloseListener
import com.example.pawtentialpals.fragments.MenuFragment
import com.example.pawtentialpals.services.PostRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), DrawerCloseListener {

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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(navView, navController)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                    closeDrawer()
                    true
                }
                R.id.nav_add -> {
                    navController.navigate(R.id.nav_add)
                    closeDrawer()
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

    override fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(binding.drawerMenu)) {
            drawerLayout.closeDrawer(binding.drawerMenu)
        }
    }
}
