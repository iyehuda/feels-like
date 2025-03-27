package com.iyehuda.feelslike.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.model.Weather
import com.iyehuda.feelslike.data.service.LocationService
import com.iyehuda.feelslike.data.service.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationService: LocationService,
    private val weatherService: WeatherService
) : ViewModel() {
    private val TAG = "HomeViewModel"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData for the list of posts
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    // LiveData for weather information
    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> get() = _weather

    // LiveData for location enabled status
    private val _isLocationEnabled = MutableLiveData<Boolean>()
    val isLocationEnabled: LiveData<Boolean> get() = _isLocationEnabled

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error state
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // LiveData for notification message (like using fallback data)
    private val _notificationMessage = MutableLiveData<String?>()
    val notificationMessage: LiveData<String?> get() = _notificationMessage
    
    // Flag to track if we're using mock data
    private var usingMockData = false

    init {
        fetchPosts()
        checkLocationEnabled()
        fetchWeatherData()
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching posts: ${error.message}", error)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val postsList = it.toObjects(Post::class.java)
                    _posts.value = postsList
                }
            }
    }

    private fun checkLocationEnabled() {
        try {
            _isLocationEnabled.value = locationService.isLocationEnabled()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if location is enabled: ${e.message}", e)
            _isLocationEnabled.value = false
        }
    }

    fun fetchWeatherData() {
        if (_isLoading.value == true) {
            Log.d(TAG, "Weather data fetch already in progress, ignoring request")
            return
        }
        
        // Reset notification message
        _notificationMessage.value = null
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "Starting weather data fetch process")
                val location = locationService.getLastLocation()
                
                if (location != null) {
                    Log.d(TAG, "Got location: ${location.latitude}, ${location.longitude}")
                    
                    // Check if we have an active internet connection
                    val result = weatherService.getWeatherByLocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    
                    result.fold(
                        onSuccess = { weather ->
                            Log.d(TAG, "Successfully fetched weather data")
                            _weather.value = weather
                            
                            // Check if locationName contains "Local Area" which indicates mock data
                            if (weather.locationName.contains("Local Area")) {
                                usingMockData = true
                                _notificationMessage.value = "Using estimated weather data. Network connectivity to weather service unavailable."
                            } else {
                                usingMockData = false
                            }
                        },
                        onFailure = { error ->
                            val errorMsg = "Error fetching weather: ${error.message}"
                            Log.e(TAG, errorMsg, error)
                            _errorMessage.value = errorMsg
                        }
                    )
                } else {
                    val errorMsg = "Unable to get location"
                    Log.e(TAG, errorMsg)
                    _errorMessage.value = errorMsg
                }
            } catch (e: CancellationException) {
                // Don't catch cancellation exceptions, just rethrow them
                throw e
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _errorMessage.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
