package com.example.pawtentialpals.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.MainActivity
import com.example.pawtentialpals.adapters.PostAdapter
import com.example.pawtentialpals.databinding.FragmentHomeBinding
import com.example.pawtentialpals.viewModels.HomeViewModel
import com.example.pawtentialpals.viewModels.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(requireActivity().application)
    }
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        homeViewModel.loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(mutableListOf(), { post ->
            (activity as? MainActivity)?.navigateToPostDetails(post)
        }, homeViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        homeViewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
        }

        homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
