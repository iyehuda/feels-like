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
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.databinding.FragmentMapBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bumptech.glide.Glide

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private val viewModel: MapViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentMapBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geocoder = Geocoder(requireContext())
        setupSearchView()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupObservers()
        
        // Setup custom location button
        binding.myLocationButton.setOnClickListener {
            if (::map.isInitialized && hasLocationPermission()) {
                getCurrentLocation()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
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
                    CameraUpdateFactory.newLatLngZoom(it, 15f), 1000, null
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    binding.searchView.clearFocus()
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocationName(searchQuery, 1)
                            }

                            if (!addresses.isNullOrEmpty() && ::map.isInitialized) {
                                val address = addresses[0]
                                val location = LatLng(address.latitude, address.longitude)
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(location, 15f), 1000, null
                                )
                            } else {
                                displayToast("Location not found")
                            }
                        } catch (e: Exception) {
                            displayToast("Unable to search location")
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
        
        // Disable the default location button, we'll use our custom button
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.setAllGesturesEnabled(true)
        
        enableMyLocation()

        viewModel.posts.value?.let { displayPosts(it) }

        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            val defaultLocation = viewModel.getDefaultLocation()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }
    }

    private fun displayPosts(posts: List<Post>) {
        map.clear()

        val postsByLocation = posts.groupBy { post ->
            "${post.latitude},${post.longitude}"
        }

        postsByLocation.forEach { (_, locationPosts) ->
            locationPosts.forEachIndexed { index, post ->
                val offsetLatLng = viewModel.calculateOffsetPosition(
                    post.getLocation(), index, locationPosts.size
                )
                addCustomMarker(
                    latLng = offsetLatLng,
                    feelsLike = post.weather,
                    temperature = "${post.temperature.toInt()}°C",
                    post = post
                )
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
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.post_details_bottom_sheet, null)

        // Set username and location
        view.findViewById<TextView>(R.id.usernameText).text = post.username
        view.findViewById<TextView>(R.id.locationText).text =
            resolveLocation(post.latitude, post.longitude)

        // Set weather in chip
        val weatherChip = view.findViewById<Chip>(R.id.weatherChip)
        weatherChip.text =
            getString(R.string.weather_description, post.temperature.toInt(), post.weather)

        // Set description
        view.findViewById<TextView>(R.id.descriptionText).text = post.description

        // Format and set timestamp
        val timestamp = formatTimestamp(post.createdAt)
        view.findViewById<TextView>(R.id.timestampText).text = timestamp

        // Load profile image
        val profileImageView = view.findViewById<ImageView>(R.id.profileImage)
        if (post.userId.isNotEmpty()) {
            // Try to load user profile image using userId
            viewModel.getUserProfilePicture(post.userId) { profileImageUrl ->
                if (!profileImageUrl.isNullOrEmpty()) {
                    ImageUtil.loadImage(this, profileImageView, profileImageUrl.toUri(), true)
                } else {
                    profileImageView.setImageResource(R.drawable.icon_account)
                }
            }
        } else {
            profileImageView.setImageResource(R.drawable.icon_account)
        }

        val postImageView = view.findViewById<ImageView>(R.id.postImage)
        post.imageUrl?.toUri()?.let {
            ImageUtil.loadImage(this, postImageView, it, false)
            postImageView.visibility = View.VISIBLE
        } ?: {
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
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCurrentLocation() {
        try {
            val locationManager =
                requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled =
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

            if (isGpsEnabled) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true

                    // Get last known location
                    val fusedLocationClient =
                        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(
                            requireActivity()
                        )
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
                Toast.makeText(
                    requireContext(), "GPS is disabled. Using default location.", Toast.LENGTH_SHORT
                ).show()
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
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
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
        post: Post
    ) {
        val markerView = layoutInflater.inflate(R.layout.map_marker_layout, null)
        markerView.findViewById<TextView>(R.id.markerText).text = feelsLike
        markerView.findViewById<TextView>(R.id.temperatureText).text = temperature

        // Set profile image if provided, otherwise use default
        val profileImageView = markerView.findViewById<ImageView>(R.id.profileImage)
        
        // Create marker after profile image is loaded
        val createMarkerWithBitmap = { bitmap: Bitmap ->
            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .anchor(0.5f, 1.0f)  // Anchor at the bottom center of the image

            val marker = map.addMarker(markerOptions)
            marker?.tag = post
        }
        
        // Convert view to bitmap after image is loaded
        val createBitmapAndMarker = {
            val bitmap = markerView.toBitmap()
            createMarkerWithBitmap(bitmap)
        }
        
        // Use userId to get profile image if available
        if (post.userId.isNotEmpty()) {
            viewModel.getUserProfilePicture(post.userId) { profileImageUrl ->
                if (!profileImageUrl.isNullOrEmpty()) {
                    // Load image and then create marker when ready
                    Glide.with(this)
                        .asBitmap()
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(object : com.bumptech.glide.request.target.SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                profileImageView.setImageBitmap(resource)
                                createBitmapAndMarker()
                            }
                        })
                } else if (post.imageUrl != null) {
                    // Fall back to post image if profile image not found
                    Glide.with(this)
                        .asBitmap()
                        .load(post.imageUrl)
                        .circleCrop()
                        .into(object : com.bumptech.glide.request.target.SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                profileImageView.setImageBitmap(resource)
                                createBitmapAndMarker()
                            }
                        })
                } else {
                    profileImageView.setImageResource(R.drawable.icon_account)
                    createBitmapAndMarker()
                }
            }
        } else if (post.imageUrl != null) {
            // Use post image if userId not available
            Glide.with(this)
                .asBitmap()
                .load(post.imageUrl)
                .circleCrop()
                .into(object : com.bumptech.glide.request.target.SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        profileImageView.setImageBitmap(resource)
                        createBitmapAndMarker()
                    }
                })
        } else {
            // Use default account icon
            profileImageView.setImageResource(R.drawable.icon_account)
            createBitmapAndMarker()
        }
    }

    // Helper extension function to convert View to Bitmap
    private fun View.toBitmap(): Bitmap {
        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = createBitmap(measuredWidth, measuredHeight)
        val canvas = Canvas(bitmap)
        layout(0, 0, measuredWidth, measuredHeight)
        draw(canvas)
        return bitmap
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
