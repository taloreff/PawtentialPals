package com.example.pawtentialpals.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pawtentialpals.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                binding.profileImage.setImageURI(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.profileImage.setOnClickListener { openImageSelector() }
        binding.editEmail.setOnClickListener { binding.email.isEnabled = true }
        binding.editUsername.setOnClickListener { binding.username.isEnabled = true }
        binding.saveButton.setOnClickListener { saveChanges() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openImageSelector() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val imageUrl = document.getString("image") ?: ""

                    binding.profileName.text = userName
                    binding.email.setText(email)
                    binding.username.setText(userName)
                    if (imageUrl.isNotEmpty()) {
                        binding.profileImage.setImageURI(Uri.parse(imageUrl))
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val newEmail = binding.email.text.toString().trim()
        val newUsername = binding.username.text.toString().trim()

        val userUpdates = hashMapOf<String, Any>(
            "email" to newEmail,
            "name" to newUsername
        )

        selectedImageUri?.let {
            userUpdates["image"] = it.toString()
        }

        firestore.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                binding.email.isEnabled = false
                binding.username.isEnabled = false
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
    }
}
