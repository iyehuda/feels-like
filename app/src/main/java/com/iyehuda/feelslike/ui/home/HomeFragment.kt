package com.iyehuda.feelslike.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.databinding.FragmentHomeBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val TAG = "HomeFragment"
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Location permission result: $permissions")
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                Log.d(TAG, "Precise location permission granted")
                viewModel.fetchWeatherData()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Approximate location access granted
                Log.d(TAG, "Approximate location permission granted")
                viewModel.fetchWeatherData()
            }
            else -> {
                // No location access granted
                Log.e(TAG, "Location permission denied")
                showErrorState("Location permission is needed for weather information")
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Fragment view created")

        setupRecyclerView()
        setupFabButton()
        setupWeatherCard()
        observeViewModel()
        checkLocationPermission()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupFabButton() {
        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_newPostFragment)
        }
    }

    private fun setupWeatherCard() {
        binding.weatherCard.setOnClickListener {
            Log.d(TAG, "Weather card clicked, refreshing weather data")
            viewModel.fetchWeatherData()
        }
    }

    private fun observeViewModel() {
        // Observe the posts LiveData
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        // Observe weather data
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            Log.d(TAG, "Weather updated: $weather")
            with(binding) {
                progressBar.isVisible = false
                errorMessage.isVisible = false
                
                weatherContentLayout.isVisible = true
                tvTemperature.text = "${weather.temperature.toInt()}°C"
                tvWeatherCondition.text = weather.condition
                tvLocation.text = weather.locationName
                
                // Show info icon if using estimated data
                infoIcon.isVisible = weather.locationName.contains("Local Area")
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "Loading state: $isLoading")
            if (isLoading) {
                showLoadingState()
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "Error displayed: $it")
                showErrorState(it)
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe notification messages
        viewModel.notificationMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Log.d(TAG, "Notification displayed: $it")
                Snackbar.make(binding.coordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            }
        }

        // Observe location enabled status
        viewModel.isLocationEnabled.observe(viewLifecycleOwner) { isEnabled ->
            if (!isEnabled) {
                Log.e(TAG, "Location services are disabled")
                showErrorState("Please enable location services")
            }
        }
    }

    private fun showLoadingState() {
        with(binding) {
            weatherContentLayout.isVisible = false
            errorMessage.isVisible = false
            progressBar.isVisible = true
        }
    }

    private fun showErrorState(errorMsg: String) {
        with(binding) {
            weatherContentLayout.isVisible = false
            progressBar.isVisible = false
            errorMessage.isVisible = true
            errorMessage.text = errorMsg
        }
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "Checking location permission")
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You have permission
                Log.d(TAG, "Already have location permission")
                viewModel.fetchWeatherData()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain why you need permission
                Log.d(TAG, "Should show permission rationale")
                Toast.makeText(
                    requireContext(),
                    "Location permission is needed to display weather information",
                    Toast.LENGTH_LONG
                ).show()
                requestLocationPermission()
            }
            else -> {
                // Request permission
                Log.d(TAG, "Requesting location permission for the first time")
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
