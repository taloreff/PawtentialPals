package com.example.pawtentialpals.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.ActivityProfileBinding
import com.example.pawtentialpals.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
