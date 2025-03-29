package com.iyehuda.feelslike.data.weather


import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.annotation.RequiresPermission
import com.iyehuda.feelslike.data.location.LocationRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val locationRepository: LocationRepository, private val weatherService: WeatherService
) {
    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun getWeatherUpdates() = locationRepository.getLocationUpdates().map {
        it.let { location ->
            weatherService.getWeatherByLocation(
                location.latitude, location.longitude
            )
        }
    }
}
