package com.iyehuda.feelslike.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.Post
import java.io.IOException
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val posts = mutableListOf<Post>()
    private lateinit var searchView: SearchView
    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize geocoder
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        // Initialize search view
        searchView = view.findViewById(R.id.searchView)
        setupSearchView()

        // Initialize map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Add mock data
        addMockPosts()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchLocation(locationName: String) {
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)

                // Move camera to the searched location
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                    1000,
                    null
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error - maybe show a toast message
        }
    }

    private fun addMockPosts() {
        posts.addAll(listOf(
            Post("1", LatLng(40.7128, -74.0060), "New York Post", "Content 1"),
            Post("2", LatLng(34.0522, -118.2437), "LA Post", "Content 2"),
            Post("3", LatLng(51.5074, -0.1278), "London Post", "Content 3"),
            Post("4", LatLng(35.6762, 139.6503), "Tokyo Post", "Content 4"),
            Post("5", LatLng(32.0853, 34.7818), "Tel Aviv Post", "Welcome to Tel Aviv!")
        ))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        displayPosts()
        // Center on Tel Aviv with zoom level 12
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.0853, 34.7818), 12f))
    }

    private fun displayPosts() {
        posts.forEach { post ->
            // Create custom marker
            addCustomMarker(
                latLng = post.location,
                feelsLike = "Feels Hot",
                temperature = "24°C"
            )

            // Add click listener
            map.setOnMarkerClickListener { clickedMarker ->
                // Show post details
                val clickedPost = posts.find { it.location == clickedMarker.position }
                clickedPost?.let { showPostDetails(it) }
                true
            }
        }
    }

    private fun showPostDetails(post: Post) {
        // Show a bottom sheet or dialog with post details
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.post_details_bottom_sheet, null)

        view.findViewById<TextView>(R.id.titleText).text = post.title
        view.findViewById<TextView>(R.id.contentText).text = post.content

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addCustomMarker(
        latLng: LatLng,
        feelsLike: String,
        temperature: String,
        profileImageResource: Int? = null
    ) {
        val markerView = layoutInflater.inflate(R.layout.map_marker_layout, null)
        markerView.findViewById<TextView>(R.id.markerText).text = feelsLike
        markerView.findViewById<TextView>(R.id.temperatureText).text = temperature

        // Set profile image if provided, otherwise use default
        val profileImageView = markerView.findViewById<ImageView>(R.id.profileImage)
        if (profileImageResource != null) {
            profileImageView.setImageResource(profileImageResource)
        } else {
            // Use default account icon
            profileImageView.setImageResource(R.drawable.icon_account)
        }

        val bitmap = markerView.toBitmap()

        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            .anchor(0.5f, 1.0f)  // Anchor at the bottom center of the image

        map.addMarker(markerOptions)
    }

    // Helper extension function to convert View to Bitmap
    private fun View.toBitmap(): Bitmap {
        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        layout(0, 0, measuredWidth, measuredHeight)
        draw(canvas)
        return bitmap
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}