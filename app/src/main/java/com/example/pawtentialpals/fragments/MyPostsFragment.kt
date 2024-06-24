package com.example.pawtentialpals.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.adapters.MyPostAdapter
import com.example.pawtentialpals.databinding.FragmentMyPostsBinding
import com.example.pawtentialpals.viewModels.MyPostsViewModel

class MyPostsFragment : Fragment() {

    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private val myPostsViewModel: MyPostsViewModel by activityViewModels()
    private lateinit var adapter: MyPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        myPostsViewModel.loadUserPosts()
    }

    private fun setupRecyclerView() {
        adapter = MyPostAdapter(mutableListOf(), myPostsViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        myPostsViewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
            if (posts.isEmpty()) {
                Toast.makeText(context, "No posts found", Toast.LENGTH_LONG).show()
            }
        }

        myPostsViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        myPostsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
