package com.iyehuda.feelslike.ui.base

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.iyehuda.feelslike.R
import java.util.Locale

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        checkLocationPermission()
    }
    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }

    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun goBack() {
        findNavController().popBackStack()
    }

    protected fun displayToast(message: String) {
        context?.applicationContext?.let { appContext ->
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun displayToast(@StringRes message: Int) {
        context?.applicationContext?.let { appContext ->
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected open fun onLocationPermissionGranted() {
    }

    protected fun checkLocationPermission() {
        when {
            checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED -> {
                onLocationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                displayToast("Location permission is needed to display weather information")
                requestLocationPermission()
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    fun resolveLocation(latitude: Double, longitude: Double): String =
        geocoder.getFromLocation(latitude, longitude, 1)?.let {
            if (it.size > 0) {
                it.last()?.let { address ->
                    getString(R.string.location, address.locality, address.countryName)
                }
            } else {
                null
            }
        } ?: "Local Area"

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
