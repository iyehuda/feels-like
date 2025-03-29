package com.iyehuda.feelslike.ui.home

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.model.Weather
import com.iyehuda.feelslike.data.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val tag = "HomeViewModel"
    private val generalError = "Unable to get location"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts
    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> get() = _weather
    private val _weatherLoading = MutableLiveData<Boolean>()
    val weatherLoading: LiveData<Boolean> get() = _weatherLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        firestore.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error fetching posts: ${error.message}", error)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    _posts.value = it.toObjects(Post::class.java)
                }
            }
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun fetchWeatherData() {
        _weatherLoading.value = true
        viewModelScope.launch {
            weatherRepository.getWeatherUpdates().collect { weatherResult ->
                try {
                    _weather.value = weatherResult.getOrThrow()
                    _errorMessage.value = null
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
