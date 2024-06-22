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
import com.example.pawtentialpals.models.UserModel
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
                    if (document != null && document.exists()) {
                        val user = document.toObject(UserModel::class.java)
                        val posts = user?.posts ?: arrayListOf()
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)
                        binding.recyclerView.adapter = MyPostAdapter(posts)
                    } else {
                        Toast.makeText(context, "No posts found", Toast.LENGTH_LONG).show()
                    }
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
