package com.iyehuda.feelslike.ui.newpost

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.data.location.LocationRepository
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.model.Weather
import com.iyehuda.feelslike.data.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class NewPostViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    val userDetails = authRepository.userDetails
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> get() = _location
    private val _locationLoading = MutableLiveData<Boolean>()
    val locationLoading: LiveData<Boolean> get() = _locationLoading
    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> get() = _weather
    private val _weatherLoading = MutableLiveData<Boolean>()
    val weatherLoading: LiveData<Boolean> get() = _weatherLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val _postImageUri = MutableLiveData<Uri?>()
    val postImageUri: LiveData<Uri?> get() = _postImageUri

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun fetchLocationAndWeather() {
        fetchLocationData()
        fetchWeatherData()
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun fetchLocationData() {
        _locationLoading.value = true
        viewModelScope.launch {
            locationRepository.getLocationUpdates().collect { location ->
                _location.value = location
                _locationLoading.value = false
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
                    _errorMessage.postValue(e.message ?: "Unable to fetch weather data")
                } finally {
                    _weatherLoading.value = false
                }
            }
        }
    }

    fun setPostImage(uri: Uri) {
        _postImageUri.value = uri
    }

    fun uploadPost(text: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = userDetails.value!!
        val postId = UUID.randomUUID().toString()
        val userId = user.userId
        val username = user.displayName
        val weather = _weather.value!!.condition
        val temperature = _weather.value!!.temperature
        val location = _location.value!!

        viewModelScope.launch {
            val imageUrl = _postImageUri.value?.let {
                val imageRef = Firebase.storage.reference.child("posts/$postId.jpg")
                imageRef.putFile(it).await()
                val downloadUrl = imageRef.downloadUrl.await()
                downloadUrl.toString()
            }
            val post = Post(
                id = postId,
                username = username,
                weather = weather,
                temperature = temperature,
                description = text,
                imageUrl = imageUrl,
                latitude = location.latitude,
                longitude = location.longitude,
                userId = userId
            )

            savePostToFirestore(post, onSuccess, onError)
        }
    }

    private suspend fun savePostToFirestore(
        post: Post, onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            Firebase.firestore.collection("posts").document(post.id).set(post).await()
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }
}
