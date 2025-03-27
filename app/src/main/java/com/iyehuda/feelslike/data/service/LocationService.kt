package com.iyehuda.feelslike.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "LocationService"
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        return try {
            if (isGooglePlayServicesAvailable()) {
                // Try Google Play Services first
                try {
                    Log.d(TAG, "Attempting to get location via Google Play Services")
                    withTimeoutOrNull(5000) { // 5 seconds timeout
                        fusedLocationClient.lastLocation.await()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting location from Google Play Services: ${e.message}")
                    null
                }
            } else {
                Log.d(TAG, "Google Play Services not available")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Google Play Services availability: ${e.message}")
            null
        }?.also {
            Log.d(TAG, "Retrieved location from Google Play Services")
        } ?: getLocationUsingAndroidAPI()
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun getLocationUsingAndroidAPI(): Location? = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Falling back to Android Location API")
        
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        
        // First try to get last known location from available providers
        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                bestLocation = location
            }
        }
        
        if (bestLocation != null) {
            Log.d(TAG, "Got last known location from Android API")
            continuation.resume(bestLocation)
            return@suspendCancellableCoroutine
        }
        
        // If no last known location, request a single update
        if (providers.isNotEmpty()) {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.d(TAG, "Received location update from Android API")
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                
                override fun onProviderEnabled(provider: String) {}
                
                override fun onProviderDisabled(provider: String) {}
            }
            
            try {
                // Request updates from the network provider as it's usually faster
                val provider = providers.find { it == LocationManager.NETWORK_PROVIDER } 
                    ?: providers.first()
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    locationListener
                )
                
                // Cancel the listener if the coroutine is cancelled
                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(locationListener)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting location updates: ${e.message}")
                continuation.resume(null)
            }
        } else {
            Log.e(TAG, "No location providers available")
            continuation.resume(null)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 10000): Flow<Location> = callbackFlow {
        if (isGooglePlayServicesAvailable()) {
            val locationRequest = LocationRequest.Builder(intervalMs)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
                
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach { location ->
                        trySend(location)
                    }
                }
            }
            
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                
                awaitClose {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } catch (e: Exception) {
                // Fallback to Android location services
                Log.e(TAG, "Error with Google Play Services location updates: ${e.message}")
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        trySend(location)
                    }
                    
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                
                val providers = locationManager.getProviders(true)
                if (providers.isNotEmpty()) {
                    val provider = providers.find { it == LocationManager.NETWORK_PROVIDER } 
                        ?: providers.first()
                    locationManager.requestLocationUpdates(
                        provider,
                        intervalMs,
                        10f, // Minimum distance in meters
                        listener
                    )
                    
                    awaitClose {
                        locationManager.removeUpdates(listener)
                    }
                } else {
                    close()
                }
            }
        } else {
            // Use Android location API
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trySend(location)
                }
                
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            
            val providers = locationManager.getProviders(true)
            if (providers.isNotEmpty()) {
                val provider = providers.find { it == LocationManager.NETWORK_PROVIDER } 
                    ?: providers.first()
                locationManager.requestLocationUpdates(
                    provider,
                    intervalMs,
                    10f, // Minimum distance in meters
                    listener
                )
                
                awaitClose {
                    locationManager.removeUpdates(listener)
                }
            } else {
                close()
            }
        }
    }
    
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
} 