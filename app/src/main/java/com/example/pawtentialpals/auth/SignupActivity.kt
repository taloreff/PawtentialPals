package com.example.pawtentialpals.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtentialpals.databinding.ActivitySignupBinding
import com.example.pawtentialpals.viewModels.SignupViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val username = binding.username.text.toString().trim()
            signupViewModel.registerUser(email, password, username)
        }

        signupViewModel.signupResult.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
