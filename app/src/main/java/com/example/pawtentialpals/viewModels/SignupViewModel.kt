// SignupViewModel.kt
package com.example.pawtentialpals.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawtentialpals.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class SignupViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> get() = _signupResult

    fun registerUser(email: String, password: String, username: String) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _signupResult.value = false
            return
        }

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val userModel = UserModel(
                        uid = firebaseUser.uid,
                        name = username,
                        email = email,
                        password = hashedPassword
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(userModel)
                        .addOnSuccessListener {
                            _signupResult.value = true
                        }
                        .addOnFailureListener {
                            _signupResult.value = false
                        }
                } else {
                    _signupResult.value = false
                }
            } else {
                _signupResult.value = false
            }
        }
    }
}
