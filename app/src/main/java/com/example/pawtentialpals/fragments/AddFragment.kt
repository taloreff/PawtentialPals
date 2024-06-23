package com.example.pawtentialpals.fragments

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
import coil.load
import com.example.pawtentialpals.R
import ImageSliderAdapter
import com.example.pawtentialpals.databinding.FragmentAddBinding
import com.example.pawtentialpals.models.PostModel
import com.example.pawtentialpals.models.UserModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedImageUri: Uri? = null
    private var mapImageUrl: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                displayImages()
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

        binding.uploadPhotoButton.setOnClickListener { openGallery() }
        binding.postButton.setOnClickListener { postToFirestore() }

        setupAutocomplete()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun setupAutocomplete() {
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val location = place.name ?: ""
                binding.location.setText(location)
                mapImageUrl = getMapImageUrl(place.latLng)
                displayImages()
            }

            override fun onError(status: Status) {
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getMapImageUrl(latLng: LatLng?): String {
        val lat = latLng?.latitude ?: 0.0
        val lng = latLng?.longitude ?: 0.0
        return "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lng&zoom=15&size=600x300&markers=color:red|$lat,$lng&key=AIzaSyDZHE0jtrWArab41ZbQN5YPTJqYeJC-jrU"
    }

    private fun displayImages() {
        val imageUrls = mutableListOf<String>()
        selectedImageUri?.let { imageUrls.add(it.toString()) }
        mapImageUrl?.let { imageUrls.add(it) }

        if (imageUrls.isNotEmpty()) {
            binding.imageSlider.visibility = View.VISIBLE
            binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)
        } else {
            binding.imageSlider.visibility = View.GONE
        }

        // Always keep the upload button visible
        binding.uploadPhotoButton.visibility = View.VISIBLE
    }

    private fun postToFirestore() {
        val description = binding.description.text.toString().trim()
        val location = binding.location.text.toString().trim()

        if (description != null && selectedImageUri != null && mapImageUrl != null) {
            fetchUserDataAndSavePost(description, location)
        } else {
            Toast.makeText(requireContext(), "Please select an image and location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserDataAndSavePost(description: String, location: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserModel::class.java) ?: return@addOnSuccessListener
                val userName = user.name
                val userImage = user.image

                savePostToFirestore(userId, userName, Uri.parse(userImage), description, location)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePostToFirestore(userId: String, userName: String, userImage: Uri, description: String, location: String) {
        val postId = UUID.randomUUID().toString()
        val currentTimestamp = System.currentTimeMillis()

        val post = PostModel(
            id = postId,
            userId = userId,
            userName = userName,
            userImage = userImage.toString(),
            timestamp = currentTimestamp,
            description = description,
            location = location,
            postImage = selectedImageUri.toString(),
            mapImage = mapImageUrl ?: "",
            likes = 0,
            comments = emptyList()
        )

        firestore.collection("posts").document(postId).set(post)
            .addOnSuccessListener {
                firestore.collection("users").document(userId)
                    .update("posts", com.google.firebase.firestore.FieldValue.arrayUnion(post))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save post to user", Toast.LENGTH_SHORT).show()
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
}
