package com.example.pawtentialpals.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.R
import com.example.pawtentialpals.ui.adapters.MyItemRecyclerViewAdapter
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
            PostModel(1, "Jack", R.drawable.batman, "Mar 28, 2022", "My Doggy gave birth to 7 gorgeous pupps!! <3", R.drawable.batman, 7, 13),
            PostModel(2, "Jack", R.drawable.batman, "Mar 25, 2022", "My dog = Best dog :)", R.drawable.batman, 2, 3),
            PostModel(3, "Jack", R.drawable.batman, "Mar 25, 2022", "Me & U could be PawtentialPals", R.drawable.batman, 3, 5)
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = MyItemRecyclerViewAdapter(posts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
