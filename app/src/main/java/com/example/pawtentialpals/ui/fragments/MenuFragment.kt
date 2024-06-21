package com.example.pawtentialpals.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOut.setOnClickListener {
            // Handle log out
        }

        binding.myPosts.setOnClickListener {
            // Navigate to MyPostsFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyPostsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.myProfile.setOnClickListener {
            // Navigate to ProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
