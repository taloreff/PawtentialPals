package com.example.pawtentialpals.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtentialpals.MainActivity
import com.example.pawtentialpals.databinding.ActivityLoginBinding
import com.example.pawtentialpals.viewModels.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Email or password is not correct, try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
