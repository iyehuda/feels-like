package com.iyehuda.feelslike.ui.home

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.databinding.FragmentHomeBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun createBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFabButton()
        observeViewModel()
        checkLocationPermission()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(::resolveLocation, viewModel::getUserProfilePicture)
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

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            with(binding) {
                progressBar.isVisible = false
                errorMessage.isVisible = false
                weatherContentLayout.isVisible = true
                tvTemperature.text = getString(R.string.temperature, weather.temperature.toInt())
                tvWeatherCondition.text = weather.condition
                tvLocation.text = weather.locationName
                infoIcon.isVisible = weather.locationName.contains("Local Area")
            }
        }

        viewModel.weatherLoading.observe(viewLifecycleOwner) { isLoading ->
            with(binding) {
                weatherContentLayout.isVisible = !isLoading
                progressBar.isVisible = isLoading

                if (isLoading) {
                    errorMessage.isVisible = false
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showErrorState(it)
                displayToast(it)
            }
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onLocationPermissionGranted() {
        viewModel.fetchWeatherData()
    }
}
