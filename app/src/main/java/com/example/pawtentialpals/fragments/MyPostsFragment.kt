package com.example.pawtentialpals.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.adapters.MyPostAdapter
import com.example.pawtentialpals.databinding.FragmentMyPostsBinding
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPostsFragment : Fragment() {

    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserPosts()
    }

    private fun loadUserPosts() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val postMap = document?.get("posts") as? List<Map<String, Any>>
                    val posts = postMap?.mapNotNull { postMap ->
                        try {
                            PostModel(
                                id = postMap["id"] as String,
                                userId = postMap["userId"] as String,
                                userName = postMap["userName"] as String,
                                userImage = postMap["userImage"] as String,
                                timestamp = (postMap["timestamp"] as Long),
                                description = postMap["description"] as String,
                                location = postMap["location"] as String,
                                postImage = postMap["postImage"] as String,
                                likes = (postMap["likes"] as Long).toInt(),
                                comments = postMap["comments"] as ArrayList<String>
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    binding.recyclerView.layoutManager = LinearLayoutManager(context)
                    binding.recyclerView.adapter = posts?.let { MyPostAdapter(it.toMutableList()) }
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
