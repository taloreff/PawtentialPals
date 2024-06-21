package com.example.pawtentialpals.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawtentialpals.R
import com.example.pawtentialpals.adapters.PostAdapter
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: ArrayList<PostModel>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        postList = arrayListOf()
        postAdapter = PostAdapter(postList)
        recyclerView.adapter = postAdapter

        firestore = FirebaseFirestore.getInstance()
        loadPosts()

        return view
    }

    private fun loadPosts() {
        firestore.collection("posts").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val post = document.toObject(PostModel::class.java)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }
}
