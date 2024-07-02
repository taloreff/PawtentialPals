package com.example.pawtentialpals.fragments

import MyPostAdapter
import MyPostsViewModel
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawtentialpals.databinding.FragmentMyPostsBinding
import com.example.pawtentialpals.viewModels.MyPostsViewModelFactory

class MyPostsFragment : Fragment(), MyPostAdapter.ImagePickerListener {

    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var myPostsViewModel: MyPostsViewModel
    private lateinit var adapter: MyPostAdapter

    private var imagePickCallback: ((Uri) -> Unit)? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                imagePickCallback?.invoke(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myPostsViewModel = ViewModelProvider(this, MyPostsViewModelFactory(requireContext())).get(MyPostsViewModel::class.java)
        setupRecyclerView()
        observeViewModel()
        myPostsViewModel.loadUserPosts()
    }

    private fun setupRecyclerView() {
        adapter = MyPostAdapter(mutableListOf(), myPostsViewModel, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        myPostsViewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
        }

        myPostsViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        myPostsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun pickImage(callback: (Uri) -> Unit) {
        imagePickCallback = callback
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
