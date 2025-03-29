package com.iyehuda.feelslike.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.model.Weather
import com.iyehuda.feelslike.data.repository.PostRepository
import com.iyehuda.feelslike.data.service.LocationService
import com.iyehuda.feelslike.data.service.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val locationService: LocationService,
    private val weatherService: WeatherService
) : ViewModel() {
    private val tag = "HomeViewModel"
    private val generalError = "Unable to get location"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts
    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> get() = _weather
    private val _locationEnabled = MutableLiveData<Boolean>()
    val locationEnabled: LiveData<Boolean> get() = _locationEnabled
    private val _weatherLoading = MutableLiveData<Boolean>()
    val weatherLoading: LiveData<Boolean> get() = _weatherLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        fetchPosts()
        checkLocationEnabled()
        fetchWeatherData()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            postRepository.getAllPosts()
                .collect { posts ->
                    _posts.value = posts
                }
        }
    }

    private fun checkLocationEnabled() {
        try {
            _locationEnabled.value = locationService.isLocationEnabled()
        } catch (e: Exception) {
            Log.e(tag, "Error checking if location is enabled: ${e.message}", e)
            _locationEnabled.value = false
        }
    }

    fun fetchWeatherData() {
        _weatherLoading.value = true
        viewModelScope.launch {
            locationService.getLocationUpdates().filterNotNull().distinctUntilChanged()
                .collect { location ->
                    _errorMessage.value = null

                    try {
                        _weather.value = with(location) {
                            weatherService.getWeatherByLocation(latitude, longitude).getOrThrow()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        setLocationError(e.message ?: generalError, e)
                    } finally {
                        _weatherLoading.value = false
                    }
                }
        }
    }

    private fun setLocationError(error: String, e: Throwable? = null) {
        Log.e(tag, error, e)
        _errorMessage.value = error
    }
}
