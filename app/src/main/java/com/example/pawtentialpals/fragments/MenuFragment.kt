package com.example.pawtentialpals.fragments

import MyPostsFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.example.pawtentialpals.MainActivity
import com.example.pawtentialpals.R
import com.example.pawtentialpals.auth.LoginActivity
import com.example.pawtentialpals.databinding.FragmentMenuBinding
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()

        binding.logOut.setOnClickListener {
            logOut()
        }

        binding.myPosts.setOnClickListener {
            loadFragment(MyPostsFragment())
        }

        binding.myProfile.setOnClickListener {
            loadFragment(ProfileFragment())
        }
    }

    private fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(UserModel::class.java)
                    user?.let {
                        binding.profileImage.load(it.image) {
                            placeholder(R.drawable.ic_user_placeholder)
                            error(R.drawable.ic_user_placeholder)
                            transformations(coil.transform.CircleCropTransformation())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logOut() {
        firebaseAuth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        (requireActivity() as? MainActivity)?.binding?.drawerLayout?.closeDrawers()
    }

    fun navigateToPostDetails(post: PostModel) {
        (activity as? MainActivity)?.navigateToPostDetails(post)
        (requireActivity() as? MainActivity)?.binding?.drawerLayout?.closeDrawers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
