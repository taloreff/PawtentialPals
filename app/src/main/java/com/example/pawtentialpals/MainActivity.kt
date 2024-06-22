package com.example.pawtentialpals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pawtentialpals.databinding.ActivityMainBinding
import com.example.pawtentialpals.fragments.HomeFragment
import com.example.pawtentialpals.fragments.AddFragment
import com.example.pawtentialpals.fragments.MenuFragment
import com.example.pawtentialpals.fragments.MyPostsFragment
import com.example.pawtentialpals.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)

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

        // Setup drawer menu click listeners
        binding.logOut.setOnClickListener {
            // Handle log out
            firebaseAuth.signOut()
            // Optionally navigate to login screen or close the app
        }

        binding.myPosts.setOnClickListener {
            loadFragment(MyPostsFragment())
            drawerLayout.closeDrawer(binding.drawerMenu)
        }

        binding.myProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            drawerLayout.closeDrawer(binding.drawerMenu)
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
