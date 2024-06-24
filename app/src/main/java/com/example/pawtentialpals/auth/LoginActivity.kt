package com.example.pawtentialpals.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtentialpals.MainActivity
import com.example.pawtentialpals.databinding.ActivityLoginBinding
import com.example.pawtentialpals.viewModels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navigateToMainActivity()
        }

        binding.goSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.signInButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            loginViewModel.login(email, password)
        }

        loginViewModel.loginResult.observe(this) { isSuccessful ->
            if (isSuccessful) {
                navigateToMainActivity()
            } else {
                Toast.makeText(
                    this,
                    "Email or password is not correct, try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
