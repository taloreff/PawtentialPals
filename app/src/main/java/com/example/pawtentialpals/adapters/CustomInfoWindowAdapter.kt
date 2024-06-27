package com.example.pawtentialpals.adapters

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.pawtentialpals.R
import com.example.pawtentialpals.models.PostModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso

class CustomInfoWindowAdapter(private val inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {

    private val window: View = inflater.inflate(R.layout.custom_info_window, null)

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val post = marker.tag as? PostModel
        post?.let {
            val imageView: ImageView = window.findViewById(R.id.info_window_image)
            val descriptionView: TextView = window.findViewById(R.id.info_window_description)

            Picasso.get().load(post.postImage).into(imageView)
            descriptionView.text = post.description
        }
        return window
    }
}
