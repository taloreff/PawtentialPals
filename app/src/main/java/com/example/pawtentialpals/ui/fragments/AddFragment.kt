package com.example.pawtentialpals.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.example.pawtentialpals.R
import com.example.pawtentialpals.databinding.FragmentAddBinding
import com.example.pawtentialpals.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                selectedImageBitmap = it
                binding.previewImageView.setImageBitmap(it)
                binding.previewImageView.visibility = View.VISIBLE
                binding.uploadPhotoButton.visibility = View.GONE
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                binding.previewImageView.setImageURI(it)
                binding.previewImageView.visibility = View.VISIBLE
                binding.uploadPhotoButton.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.uploadPhotoButton.setOnClickListener { openImageSelector() }
        binding.postButton.setOnClickListener { postToFirestore() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openImageSelector() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                }
            }
        builder.show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun postToFirestore() {
        val description = binding.description.text.toString().trim()
        val location = binding.location.text.toString().trim()

        if (selectedImageUri != null || selectedImageBitmap != null) {
            fetchUserDataAndSavePost(description, location)
        } else {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserDataAndSavePost(description: String, location: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Anonymous"
                val userImage = document.getString("image") ?: "drawable/batman.png"

                savePostToFirestore(userId, userName, Uri.parse(userImage), description, location)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePostToFirestore(userId: String, userName: String, userImage: Uri, description: String, location: String) {
        val postId = UUID.randomUUID().toString()

        // Format the current date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val post = PostModel(
            id = postId,
            userId = userId,
            userName = userName,
            userImage = userImage.toString(),
            timestamp = currentDate,
            description = description,
            location = location,
            postImage = selectedImageUri.toString(),
            likes = 0,
            comments = ArrayList()
        )

        firestore.collection("posts").document(postId).set(post)
            .addOnSuccessListener {
                firestore.collection("users").document(userId)
                    .collection("posts").document(postId).set(post)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save post", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
