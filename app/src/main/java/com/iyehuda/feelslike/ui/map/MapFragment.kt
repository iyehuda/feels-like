package com.iyehuda.feelslike.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.iyehuda.feelslike.databinding.FragmentMapBinding
import com.iyehuda.feelslike.ui.utils.ImageUtil
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Initialize geocoder
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        // Initialize search view
        setupSearchView()

        // Initialize map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observe ViewModel data
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { postsList ->
            if (::map.isInitialized) {
                displayPosts(postsList)
            }
        }

        viewModel.searchResult.observe(viewLifecycleOwner) { location ->
            location?.let {
                // Move camera to the searched location
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(it, 15f),
                    1000,
                    null
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    binding.searchView.clearFocus() // Hide keyboard

                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocationName(searchQuery, 1)
                            }

                            if (!addresses.isNullOrEmpty() && ::map.isInitialized) {
                                val address = addresses[0]
                                val location = LatLng(address.latitude, address.longitude)
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(location, 15f),
                                    1000,
                                    null
                                )
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Location not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Unable to search location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        // Observe posts if they're already loaded
        viewModel.posts.value?.let { displayPosts(it) }

        // Try to get current location, otherwise use default
        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            // Center on default location with zoom level 12
            val defaultLocation = viewModel.getDefaultLocation()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }
    }

    private fun displayPosts(posts: List<Post>) {
        // Clear existing markers
        map.clear()

        // Group posts by location
        val postsByLocation = posts.groupBy { post ->
            if (post.latitude == 0.0 && post.longitude == 0.0 && post.locationString != null) {
                // For posts with string locations, use the string as the key
                post.locationString
            } else {
                // For posts with coordinates, use the coordinates as the key
                "${post.latitude},${post.longitude}"
            }
        }

        postsByLocation.forEach { (locationKey, locationPosts) ->
            locationPosts.forEachIndexed { index, post ->
                val latLng = if (post.latitude == 0.0 && post.longitude == 0.0 && post.locationString != null) {
                    // If the post has a string location but no coordinates, geocode it
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocationName(post.locationString, 1)
                            }
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val originalLatLng = LatLng(address.latitude, address.longitude)
                                // Calculate offset position
                                val offsetLatLng = viewModel.calculateOffsetPosition(
                                    originalLatLng,
                                    index,
                                    locationPosts.size
                                )
                                // Create custom marker with weather information
                                addCustomMarker(
                                    latLng = offsetLatLng,
                                    feelsLike = post.weather,
                                    temperature = "${post.temperature}°C",
                                    profileImageUri = post.imageUri,
                                    post = post // Pass the post to store with the marker
                                )
                            }
                        } catch (e: Exception) {
                            // If geocoding fails, use default location
                            addCustomMarker(
                                latLng = post.location,
                                feelsLike = post.weather,
                                temperature = "${post.temperature}°C",
                                profileImageUri = post.imageUri,
                                post = post // Pass the post to store with the marker
                            )
                        }
                    }
                    null // Return null for async geocoding case
                } else {
                    // For posts with coordinates, calculate offset position
                    val offsetLatLng = viewModel.calculateOffsetPosition(
                        post.location,
                        index,
                        locationPosts.size
                    )
                    // Create custom marker with weather information
                    addCustomMarker(
                        latLng = offsetLatLng,
                        feelsLike = post.weather,
                        temperature = "${post.temperature}°C",
                        profileImageUri = post.imageUri,
                        post = post // Pass the post to store with the marker
                    )
                    offsetLatLng
                }
            }
        }

        // Add click listener
        map.setOnMarkerClickListener { clickedMarker ->
            // Get the post stored with the marker
            val post = clickedMarker.tag as? Post
            post?.let { showPostDetails(it) }
            true
        }
    }

    private fun showPostDetails(post: Post) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.post_details_bottom_sheet, null)

        // Set username and location
        view.findViewById<TextView>(R.id.usernameText).text = post.username ?: "Anonymous"
        view.findViewById<TextView>(R.id.locationText).text = post.locationString ?: "Unknown Location"
        
        // Set weather in chip
        val weatherChip = view.findViewById<Chip>(R.id.weatherChip)
        weatherChip.text = "${post.weather}, ${post.temperature}°C"
        
        // Set description
        view.findViewById<TextView>(R.id.descriptionText).text = post.description ?: "No description"
        
        // Format and set timestamp
        val timestamp = formatTimestamp(post.createdAt ?: System.currentTimeMillis())
        view.findViewById<TextView>(R.id.timestampText).text = timestamp

        // Load profile image
        val profileImageView = view.findViewById<ImageView>(R.id.profileImage)
        if (post.userId.isNotEmpty()) {
            // Try to load user profile image using userId
            viewModel.getUserProfilePicture(post.userId) { profileImageUrl ->
                if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                    ImageUtil.loadImage(this, profileImageView, Uri.parse(profileImageUrl), true)
                } else {
                    profileImageView.setImageResource(R.drawable.icon_account)
                }
            }
        } else {
            profileImageView.setImageResource(R.drawable.icon_account)
        }

        // Load post image
        val postImageView = view.findViewById<ImageView>(R.id.postImage)
        if (post.imageUrl != null && post.imageUrl.isNotEmpty()) {
            ImageUtil.loadImage(this, postImageView, Uri.parse(post.imageUrl), false)
            postImageView.visibility = View.VISIBLE
        } else {
            postImageView.visibility = View.GONE
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    // Helper function to format timestamp similarly to post feed
    private fun formatTimestamp(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val difference = currentTime - timestamp
        
        return when {
            difference < 60 * 1000 -> "Just now"
            difference < 60 * 60 * 1000 -> "${difference / (60 * 1000)}m ago"
            difference < 24 * 60 * 60 * 1000 -> "${difference / (60 * 60 * 1000)}h ago"
            difference < 7 * 24 * 60 * 60 * 1000 -> "${difference / (24 * 60 * 60 * 1000)}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        try {
            val locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

            if (isGpsEnabled) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true

                    // Get last known location
                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity())
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        } else {
                            // If no last location, use default
                            val defaultLocation = viewModel.getDefaultLocation()
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                        }
                    }
                }
            } else {
                // GPS is not enabled, use default location
                val defaultLocation = viewModel.getDefaultLocation()
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                Toast.makeText(requireContext(), "GPS is disabled. Using default location.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // In case of any error, fall back to default location
            val defaultLocation = viewModel.getDefaultLocation()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }
    }

    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            try {
                map.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // Handle exception
            }
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
        profileImageUri: Uri? = null,
        post: Post // Add post parameter
    ) {
        val markerView = layoutInflater.inflate(R.layout.map_marker_layout, null)
        markerView.findViewById<TextView>(R.id.markerText).text = feelsLike
        markerView.findViewById<TextView>(R.id.temperatureText).text = temperature

        // Set profile image if provided, otherwise use default
        val profileImageView = markerView.findViewById<ImageView>(R.id.profileImage)
        if (profileImageUri != null) {
            // Use ImageUtil to load the image from Firebase
            ImageUtil.loadImage(this, profileImageView, profileImageUri, true)
        } else {
            // Use default account icon
            profileImageView.setImageResource(R.drawable.icon_account)
        }

        val bitmap = markerView.toBitmap()

        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            .anchor(0.5f, 1.0f)  // Anchor at the bottom center of the image

        val marker = map.addMarker(markerOptions)
        marker?.tag = post // Store the post with the marker
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

    // Helper function to calculate distance between two LatLng points
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            point1.latitude,
            point1.longitude,
            point2.latitude,
            point2.longitude,
            results
        )
        return results[0].toDouble()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}