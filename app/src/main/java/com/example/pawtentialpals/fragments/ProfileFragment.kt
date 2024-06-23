package com.example.pawtentialpals.fragments

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
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.FragmentProfileBinding
import com.example.pawtentialpals.services.UploadService
import com.example.pawtentialpals.viewModels.ProfileUpdateViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileUpdateViewModel: ProfileUpdateViewModel by activityViewModels()

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
                        binding.profileImage.load(imageUrl) {
                            crossfade(true)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val newUsername = binding.username.text.toString().trim()

        val userUpdates = hashMapOf<String, Any>(
            "name" to newUsername
        )

        if (selectedImageUri != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val imageUrl = uploadImage(selectedImageUri!!)
                if (imageUrl != null) {
                    userUpdates["image"] = imageUrl
                    updateUserProfile(userId, userUpdates, newUsername)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            updateUserProfile(userId, userUpdates, newUsername)
        }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val filePath = getPathFromUri(uri) ?: return null
        return UploadService.uploadImg(filePath)
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun updateUserProfile(userId: String, userUpdates: HashMap<String, Any>, newUsername: String) {
        firestore.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                updatePreviousPosts(userId, newUsername, userUpdates["image"] as? String)
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                binding.username.isEnabled = false
                profileUpdateViewModel.setProfileUpdated(true)
                navigateToHome()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }

        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        firebaseAuth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(requireContext(), "Failed to update username in Auth", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePreviousPosts(userId: String, newUsername: String, newImageUri: String?) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()
                for (document in querySnapshot.documents) {
                    val postRef = document.reference
                    val updates = hashMapOf<String, Any>(
                        "userName" to newUsername
                    )
                    newImageUri?.let { uri ->
                        updates["userImage"] = uri
                    }
                    batch.update(postRef, updates)
                }
                batch.commit()
                    .addOnSuccessListener {
                        profileUpdateViewModel.setProfileUpdated(true)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update previous posts", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
    }
}
