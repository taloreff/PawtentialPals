package com.example.pawtentialpals.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.pawtentialpals.databinding.ActivitySignupBinding
import com.example.pawtentialpals.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class SignupActivity : ComponentActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.goSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.signUpButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val username = binding.username.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = firebaseAuth.currentUser
                Toast.makeText(this, firebaseUser.toString(), Toast.LENGTH_LONG).show()
                if (firebaseUser != null) {
                    val userModel = UserModel(
                        uid = firebaseUser.uid,
                        name = username,
                        email = email,
                        password = hashedPassword
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(userModel)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignupActivity", "Error saving user data: ${e.message}")
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show()
                            // Re-enable the register button and hide the progress bar
                            binding.signUpButton.isEnabled = true
                        }
                } else {
                    Log.e("SignupActivity", "Firebase user is null after registration")
                }
            } else {
                Log.e("SignupActivity", "Failed to create user: ${task.exception?.message}", task.exception)
                Toast.makeText(this, "Failed to create user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                // Re-enable the register button and hide the progress bar
                binding.signUpButton.isEnabled = true
            }
        }
    }
}
