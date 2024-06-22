package com.example.pawtentialpals.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.R
import com.example.pawtentialpals.adapters.PostAdapter
import com.example.pawtentialpals.databinding.FragmentMyPostsBinding
import com.example.pawtentialpals.models.PostModel

class MyPostsFragment : Fragment() {

    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val posts = listOf(
            PostModel(
                id = "1",
                userId = "userId1",
                userName = "Jack",
                userImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                timestamp = "Mar 28, 2024",
                description = "My Doggy gave birth to 7 gorgeous pupps!! <3",
                location = "Paris",
                postImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                likes = 1,
                comments = arrayListOf()
            ),
            PostModel(
                id = "2",
                userId = "userId2",
                userName = "John",
                userImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                timestamp = "Mar 25, 2024",
                description = "My dog = Best dog :)",
                location = "Location1",
                postImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                likes = 2,
                comments = arrayListOf()
            ),
            PostModel(
                id = "3",
                userId = "userId3",
                userName = "Julie",
                userImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                timestamp = "Mar 25, 2024",
                description = "Me & U could be PawtentialPals",
                location = "Location2",
                postImage = Uri.parse("android.resource://com.example.pawtentialpals/drawable/batman").toString(),
                likes = 3,
                comments = arrayListOf()
            )
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = PostAdapter(posts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
