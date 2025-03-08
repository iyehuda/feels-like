package com.iyehuda.feelslike.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iyehuda.feelslike.data.model.Post

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    init {
        loadMockPosts()
    }

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> get() = _temperature

    private val _condition = MutableLiveData<String>()
    val condition: LiveData<String> get() = _condition

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    fun fetchWeather() {
        // Make a network call or use a repository to get weather data
        // ...
        // Then post values:
        _temperature.value = "23°C"
        _condition.value = "Sunny"
        _location.value = "Tel Aviv, Israel"
    }

    private fun loadMockPosts() {
        _posts.value = listOf(
            Post(
                id = "1",
                username = "SunnyUser",
                weather = "Sunny",
                temperature = 30.0,
                description = "Enjoying the bright day at the beach!"
            ),
            Post(
                id = "2",
                username = "CloudyCat",
                weather = "Cloudy",
                temperature = 18.0,
                description = "A perfect day for a cozy indoor movie marathon."
            ),
            Post(
                id = "3",
                username = "RainyDayDan",
                weather = "Rainy",
                temperature = 15.0,
                description = "Rainy days call for hot coffee and a good book."
            )
        )
    }
}