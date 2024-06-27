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
import androidx.fragment.app.activityViewModels
import coil.request.ImageRequest
import com.example.pawtentialpals.R
import ImageSliderAdapter
import com.example.pawtentialpals.databinding.FragmentAddBinding
import com.example.pawtentialpals.viewModels.AddPostViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val addPostViewModel: AddPostViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private var mapImageUrl: String? = null
    private var selectedLocation: String? = null

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

        binding.uploadPhotoButton.setOnClickListener { openGallery() }
        binding.postButton.setOnClickListener { postToFirestore() }

        setupAutocomplete()
        observeViewModel()
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
                selectedLocation = place.name ?: ""
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
        val apiKey = getString(R.string.google_maps_key)
        return "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lng&zoom=15&size=600x300&markers=color:red|$lat,$lng&key=$apiKey"
    }


    private fun displayImages() {
        val imageUrls = mutableListOf<String>()
        selectedImageUri?.let { imageUrls.add(it.toString()) }
        mapImageUrl?.let { imageUrls.add(it) }

        if (imageUrls.size == 2) {
            binding.imageSlider.visibility = View.VISIBLE
            binding.uploadPhotoButton.visibility = View.GONE
            showProgressBar()

            // Load the images with Coil and hide the progress bar when done
            val imageLoader = coil.ImageLoader(requireContext())
            val request = ImageRequest.Builder(requireContext())
                .data(imageUrls[1])
                .target { result ->
                    binding.imageSlider.adapter = ImageSliderAdapter(imageUrls)
                    hideProgressBar()
                }
                .build()
            imageLoader.enqueue(request)
        } else {
            binding.imageSlider.visibility = View.GONE
        }

        // Always keep the upload button visible
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun postToFirestore() {
        val description = binding.description.text.toString().trim()

        if (description.isNotEmpty() && selectedImageUri != null && mapImageUrl != null && selectedLocation != null) {
            addPostViewModel.postToFirestore(description, selectedImageUri!!, mapImageUrl!!, selectedLocation!!)
        } else {
            Toast.makeText(requireContext(), "Please select an image and location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        addPostViewModel.postCreationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToHome()
                addPostViewModel.resetPostCreationSuccess()
            }
        }

        addPostViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        addPostViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }
}
