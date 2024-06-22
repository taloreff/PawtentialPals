package com.example.pawtentialpals.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.adapters.PostAdapter
import com.example.pawtentialpals.databinding.FragmentHomeBinding
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPosts()
    }

    private fun loadPosts() {
        firestore.collection("posts").get()
            .addOnSuccessListener { result ->
                val posts = result.mapNotNull { it.toObject(PostModel::class.java) }
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = PostAdapter(posts)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
