package com.iyehuda.feelslike.ui.newpost

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.fragment.app.viewModels
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.databinding.FragmentNewPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImagePicker
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostFragment : BaseFragment<FragmentNewPostBinding>() {
    private val viewModel: NewPostViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentNewPostBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.setPostImage(uri)
        }

        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.displayName
                ImageUtil.loadImage(this, binding.ivProfile, it.photoUrl, true)
            }
        }

        viewModel.location.observe(viewLifecycleOwner) { location ->
            binding.tvLocation.text = resolveLocation(location.latitude, location.longitude)
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            binding.tvWeatherInfo.text = getString(
                R.string.weather_description, weather.temperature.toInt(), weather.condition
            )
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                displayToast(it)
            }
        }

        viewModel.postImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let { updateImageView(it) }
        }

        binding.imagePlaceholder.setOnClickListener {
            imagePicker.pickSingleImage()
        }

        binding.btnUploadPost.setOnClickListener {
            submitPost()
        }

        binding.btnCancel.setOnClickListener {
            goBack()
        }

        checkLocationPermission()
    }

    private fun updateImageView(uri: Uri) {
        if (uri != Uri.EMPTY) {
            ImageUtil.loadImage(this, binding.imagePlaceholder, uri)
        }
    }

    private fun submitPost() {
        with(binding) {
            val postText = etPostText.text.toString()

            loadingProgressBar.visibility = View.VISIBLE

            viewModel.uploadPost(postText, onSuccess = {
                loadingProgressBar.visibility = View.GONE
                goBack()
            }, onError = { e ->
                loadingProgressBar.visibility = View.GONE
                displayToast("Error: ${e.message}")
            })
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onLocationPermissionGranted() {
        viewModel.fetchLocationAndWeather()
    }
}
