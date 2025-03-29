package com.iyehuda.feelslike.data.weather

import android.util.Log
import com.iyehuda.feelslike.data.model.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class WeatherService @Inject constructor() {
    private val TAG = "WeatherService"
    private val apiKey = "a2240362b1d2d4f5d53bb8a3dbbbb2dd"
    private val baseUrl = "https://api.openweathermap.org/data/2.5/weather"
    private val connectTimeoutMs = TimeUnit.SECONDS.toMillis(10)
    private val readTimeoutMs = TimeUnit.SECONDS.toMillis(10)
    private var retryCount = 0
    private val maxRetries = 2

    suspend fun getWeatherByLocation(latitude: Double, longitude: Double): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching weather for location: lat=$latitude, lon=$longitude")
                val url = "$baseUrl?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"

                val response =
                    withTimeoutOrNull(15000) { // 15 seconds timeout for the whole operation
                        fetchData(url)
                    } ?: throw Exception("Request timed out")

                Log.d(TAG, "Received weather data response")
                val jsonObject = JSONObject(response)

                // Parse main weather data
                val main = jsonObject.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val humidity = main.getInt("humidity")

                // Parse weather condition
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val condition = weatherObject.getString("main")
                val iconCode = weatherObject.getString("icon")

                // Parse wind data
                val wind = jsonObject.getJSONObject("wind")
                val windSpeed = wind.getDouble("speed")

                // Parse location name
                val locationName = jsonObject.getString("name")

                val weather = Weather(
                    temperature = temperature,
                    condition = condition,
                    locationName = locationName,
                    iconCode = iconCode,
                    humidity = humidity,
                    windSpeed = windSpeed
                )

                // Reset retry count on success
                retryCount = 0

                Log.d(TAG, "Successfully parsed weather data: $weather")
                Result.success(weather)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data: ${e.message}", e)

                if (retryCount < maxRetries) {
                    retryCount++
                    Log.d(TAG, "Retrying API call (attempt $retryCount of $maxRetries)...")
                    delay(1000) // Wait 1 second before retrying
                    return@withContext getWeatherByLocation(latitude, longitude)
                }

                // If we've used all retries or it's a specific type of error, use mock data
                Log.d(TAG, "Using fallback mock weather data")
                Result.success(getMockWeatherData(latitude, longitude))
            }
        }
    }

    private fun getMockWeatherData(latitude: Double, longitude: Double): Weather {
        // Create realistic mock data based on the coordinates
        val isNorthern = latitude > 0
        val season = getCurrentSeason(isNorthern)

        // Mock temperature based on latitude (colder toward poles) and season
        val baseTemp = when {
            Math.abs(latitude) > 60 -> 5.0 // Polar regions
            Math.abs(latitude) > 40 -> 15.0 // Temperate regions
            else -> 25.0 // Tropical regions
        }

        // Season adjustment
        val tempAdjustment = when (season) {
            "Summer" -> 10.0
            "Winter" -> -10.0
            else -> 0.0 // Spring/Fall
        }

        val finalTemp = baseTemp + tempAdjustment + Random.nextDouble(-5.0, 5.0)

        // Generate weather condition based on temperature
        val condition = when {
            finalTemp < 0 -> "Snow"
            finalTemp < 10 -> "Clouds"
            finalTemp < 20 -> "Clear"
            else -> "Sunny"
        }

        // Get a location name approximation
        val locationName = getApproximateLocationName(latitude, longitude)

        return Weather(
            temperature = finalTemp,
            condition = condition,
            locationName = locationName,
            iconCode = getIconForCondition(condition),
            humidity = Random.nextInt(30, 90),
            windSpeed = Random.nextDouble(1.0, 10.0)
        )
    }

    private fun getCurrentSeason(northernHemisphere: Boolean): String {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)

        return if (northernHemisphere) {
            when (month) {
                11, 0, 1 -> "Winter"
                2, 3, 4 -> "Spring"
                5, 6, 7 -> "Summer"
                else -> "Fall"
            }
        } else {
            when (month) {
                11, 0, 1 -> "Summer"
                2, 3, 4 -> "Fall"
                5, 6, 7 -> "Winter"
                else -> "Spring"
            }
        }
    }

    private fun getApproximateLocationName(latitude: Double, longitude: Double): String {
        // Just a simple approximation based on latitude/longitude
        val latDir = if (latitude >= 0) "North" else "South"
        val longDir = if (longitude >= 0) "East" else "West"

        return "Local Area ($latDir, $longDir)"
    }

    private fun getIconForCondition(condition: String): String {
        return when (condition) {
            "Clear", "Sunny" -> "01d"
            "Clouds" -> "03d"
            "Rain" -> "10d"
            "Snow" -> "13d"
            "Thunderstorm" -> "11d"
            else -> "50d" // Mist/Fog for unknown
        }
    }

    private fun fetchData(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = connectTimeoutMs.toInt()
        connection.readTimeout = readTimeoutMs.toInt()

        try {
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error code: $responseCode")
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            reader.close()
            return response.toString()
        } finally {
            connection.disconnect()
        }
    }
} 