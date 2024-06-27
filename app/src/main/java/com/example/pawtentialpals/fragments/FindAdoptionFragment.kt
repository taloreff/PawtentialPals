package com.example.pawtentialpals.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pawtentialpals.adapters.CustomInfoWindowAdapter
import com.example.pawtentialpals.R
import com.example.pawtentialpals.models.PostModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class FindAdoptionFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore
    private lateinit var placesClient: PlacesClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_find_adoption, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        firestore = FirebaseFirestore.getInstance()
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(layoutInflater))
        loadPosts()
    }

    private fun loadPosts() {
        firestore.collection("posts").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val post = document.toObject(PostModel::class.java)
                    getLatLngFromAddress(post.location) { latLng ->
                        if (latLng != null) {
                            val marker = googleMap.addMarker(MarkerOptions().position(latLng).title(post.description))
                            marker?.tag = post
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun getLatLngFromAddress(address: String, callback: (LatLng?) -> Unit) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(address)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            if (response.autocompletePredictions.isNotEmpty()) {
                val prediction = response.autocompletePredictions.first()
                val placeId = prediction.placeId

                placesClient.fetchPlace(com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build())
                    .addOnSuccessListener { fetchPlaceResponse ->
                        callback(fetchPlaceResponse.place.latLng)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FindAdoptionFragment", "Place not found: ${exception.message}")
                        callback(null)
                    }
            } else {
                callback(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("FindAdoptionFragment", "Error getting place predictions: ${exception.message}")
            callback(null)
        }
    }
}
