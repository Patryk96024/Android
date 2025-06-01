package com.example.weather

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.api.HourWeather
import com.example.weather.api.NetworkResponse
import com.example.weather.api.WeatherModel
import com.skydoves.landscapist.coil.CoilImage
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.LocalTextStyle
import androidx.core.graphics.toColorInt
import com.example.weather.api.Current
import kotlin.math.round
import kotlin.text.toDouble

@Composable
fun WeatherPage(viewModel: WeatherViewModel) {

    var city by remember { mutableStateOf("") }
    val weatherResult = viewModel.weatherResult.observeAsState()

    val context = LocalContext.current
    var reload by remember { mutableStateOf(0) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            reload++
        }
    }

    val bgColor = if (!isDarkTheme(reload)) "#FFFFFF" else "#333333"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(bgColor.toColorInt())),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                settingsLauncher.launch(Intent(context, SettingsActivity::class.java))
            }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = city,
                textStyle = LocalTextStyle.current.copy(color = getTextColor(reload)),
                onValueChange = {
                    city = it
                },
                label = {
                    Text(
                        text = "Search for any location",
                        color = getTextColor(reload)
                    )
                }
            )
            IconButton(onClick = {
                viewModel.getData(city)
            }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search for any location"
                )
            }
        }

        when (val result = weatherResult.value) {
            is NetworkResponse.Error -> {
                Text(
                    text = result.message,
                    color = getTextColor(reload)
                )
            }
            NetworkResponse.Loading -> {
                CircularProgressIndicator()
            }
            is NetworkResponse.Success -> {
                WeatherDetails(data = result.data, reload = reload)
            }
            null -> {}
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel, reload: Int) {
    val bgColor = if (!isDarkTheme(reload)) "#e3d9fa" else "#373242"
    val infoColor = if (!isDarkTheme(reload)) "#d8cbf5" else "#2f2c36"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(bgColor.toColorInt()))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon",
                modifier = Modifier.size(40.dp)
            )
            Text(text = data.location.name, fontSize = 30.sp, color = getTextColor(reload))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = data.location.country, fontSize = 18.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = getCurTempUnit(data.current, reload),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = getTextColor(reload)
        )
        CoilImage(
            modifier = Modifier.size(140.dp),
            imageModel = { "https:${data.current.condition.icon}".replace("64x64", "128x128") },
        )

        Text(
            text = data.current.condition.text,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(infoColor.toColorInt()))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Humidity", data.current.humidity, reload)
                    WeatherKeyVal("Wind Speed", getWindSpeedUnit(data.current.wind_kph.toDouble(), reload), reload)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("UV", data.current.uv, reload)
                    WeatherKeyVal("Participation", data.current.precip_mm + " mm", reload)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherKeyVal("Local Time", data.location.localtime.split(" ")[1], reload)
                    WeatherKeyVal("Local Date", data.location.localtime.split(" ")[0], reload)
                }
            }
        }

        Text(
            text = "Hourly forecast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            color = getTextColor(reload)
        )

        val currentHour = data.location.localtime.split(" ")[1].split(":")[0]

        val filteredHours = data.forecast.forecastday.firstOrNull()?.hour
            ?.filter { hourData ->
                val hour = hourData.time.split(" ")[1].split(":")[0]
                hour >= currentHour
            }
            ?.take(5)

        filteredHours?.let { hours ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                hours.forEachIndexed { index, hourData ->
                    HourlyWeatherVal(hourData, isNow = index == 0, reload = reload)
                }
            }
        }
    }
}


@Composable
fun HourlyWeatherVal(hour: HourWeather, isNow: Boolean, reload: Int) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isNow) "NOW" else hour.time.split(" ")[1].substring(0, 5),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = getTextColor(reload)
        )
        CoilImage(
            modifier = Modifier.size(48.dp),
            imageModel = { "https:${hour.condition.icon}".replace("64x64", "128x128") }
        )
        Text(
            text = getHourlyTempUnit(hour, reload), fontSize = 14.sp,
            color = getTextColor(reload),
            softWrap = false
        )
    }
}

@Composable
fun WeatherKeyVal(key : String, value : String, reload: Int) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color=getTextColor(reload))
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun getPrefs(reload: Int): SharedPreferences {
    val context = LocalContext.current
    return remember(reload) {
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    }
}

@Composable
fun isDarkTheme(reload: Int): Boolean {
    val prefs = getPrefs(reload)
    return prefs.getBoolean("darkTheme", true)
}

@Composable
fun getTextColor(reload: Int): Color {
    val txtColor = if (!isDarkTheme(reload)) "#000000" else "#FFFFFF"
    return Color(txtColor.toColorInt())
}

@Composable
fun getWindSpeedUnit(windKph: Double, reload: Int): String {
    val prefs = getPrefs(reload)
    return when (prefs.getString("windSpeedUnit", "km/h")) {
        "km/h" -> "${windKph} km/h"
        "mph" -> "${(windKph * 0.621371).let { round(it * 100) / 100 }} mph"
        "m/s" -> "${(windKph / 3.6).let { round(it * 100) / 100 }} m/s"
        else -> "${windKph} km/h"
    }
}

@Composable
fun getCurTempUnit(data: Current, reload: Int): String {
    val prefs = getPrefs(reload)
    when (prefs.getString("temperatureUnit", "Celsius")) {
        "Celsius" -> return data.temp_c+"°C"
        "Fahrenheit" -> return data.temp_f+"°F"
        "Kelvin" -> return ((round(data.temp_c.toDouble() + 273.15 * 100) / 100).toString())+"K"
        else -> return data.temp_c+"°C"
    }
}

@Composable
fun getHourlyTempUnit(data: HourWeather, reload: Int): String {
    val prefs = getPrefs(reload)
    when (prefs.getString("temperatureUnit", "Celsius")) {
        "Celsius" -> return data.temp_c.toString()+"°C"
        "Fahrenheit" -> return data.temp_f.toString()+"°F"
        "Kelvin" -> return (round(data.temp_c.toDouble() + 273.15 * 100) / 100).toString()+"K"
        else -> return data.temp_c.toString()+"°C"
    }
}