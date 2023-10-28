package com.github.sahibmobdev.myweathernew

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import com.github.sahibmobdev.myweathernew.Constants.API_KEY
import com.github.sahibmobdev.myweathernew.Constants.BASE_URL
import com.github.sahibmobdev.myweathernew.api.ApiInterface
import com.github.sahibmobdev.myweathernew.databinding.ActivityMainBinding
import com.github.sahibmobdev.myweathernew.model.WeatherApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchWeatherData("Baku")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, API_KEY, "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.apply {
                        temp.text = "${temperature.toInt()} °C"
                        weather.text = condition
                        maxTemperature.text = "Max Temp: ${maxTemp.toInt()} °C"
                        minTemperature.text = "Min Temp: ${minTemp.toInt()} °C"
                        tvHumidity.text = "$humidity %"
                        tvWindSpeed.text = "$windSpeed m/s"
                        tvSunrise.text = "${time(sunRise)}"
                        tvSunset.text = "${time(sunSet)}"
                        tvSea.text = "$seaLevel hPa"
                        tvCondition.text = condition
                        tvDay.text = capitalizeFirstLetter(dayName(System.currentTimeMillis()))
                            tvDate.text = date()
                            tvCityName.text = capitalizeFirstLetter(cityName)
                    }
                    // Log.d("MyLog", "onResponse: $temperature")

                    changeBackgroundAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.d("MyLog", "onFailure: ${t.message}")
            }

        })

    }

    private fun changeBackgroundAccordingToWeatherCondition(conditions: String) {
        when (conditions) {
            "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Fog" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard", "Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            } else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    fun capitalizeFirstLetter(text: String): String {
        if (text.isEmpty()) {
            return text
        }

        val firstChar = text[0].uppercase()
        val restOfString = text.substring(1).lowercase()

        return "$firstChar$restOfString"
    }
}