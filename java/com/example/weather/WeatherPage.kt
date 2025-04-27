package com.example.weather

import android.graphics.Color.parseColor
import android.util.Log
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
import coil3.compose.AsyncImage
import com.example.weather.api.HourWeather
import com.example.weather.api.NetworkResponse
import com.example.weather.api.WeatherModel
import com.skydoves.landscapist.coil.CoilImage


@Composable
fun WeatherPage(viewModel: WeatherViewModel){

    var city by remember {
        mutableStateOf("")
    }

    val weatherResult = viewModel.weatherResult.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            onValueChange ={
            city = it
        },
            label = {
                Text(text="Search for any location")
                 }
            )
        IconButton(onClick = {
           viewModel.getData(city)
        }) {
Icon(imageVector = Icons.Default.Search,
    contentDescription ="Search for any location"
)
        }

        }

        when(val result = weatherResult.value){
            is NetworkResponse.Error -> {
                Text(text = result.message)
            }
            NetworkResponse.Loading -> {
                CircularProgressIndicator()
            }
            is NetworkResponse.Success -> {
                WeatherDetails(data = result.data)
            }
            null -> {}
        }
    }
}

@Composable
fun WeatherDetails(data : WeatherModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(parseColor("#e3d9fa"))).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
           Icon(imageVector = Icons.Default.LocationOn,
               contentDescription = "Location icon",
               modifier = Modifier.size(40.dp)
           )
            Text(text = data.location.name, fontSize = 30.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = data.location.country, fontSize = 18.sp,color = Color.Gray)
        }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = " ${data.current.temp_c} °C",
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    CoilImage(
        modifier = Modifier.size(160.dp),
        imageModel = {"https:${data.current.condition.icon}".replace("64x64","128x128")},
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
            modifier = Modifier.fillMaxWidth().background(Color(parseColor("#d8cbf5")))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherKeyVal("Humidity",data.current.humidity)
                WeatherKeyVal("Wind Speed",data.current.wind_kph+" km/h")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherKeyVal("UV",data.current.uv)
                WeatherKeyVal("Participation",data.current.precip_mm+" mm")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherKeyVal("Local Time",data.location.localtime.split(" ")[1])
                WeatherKeyVal("Local Date",data.location.localtime.split(" ")[0])
            }
        }
    }

    Text(
        text = "Hourly forecast",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(8.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))

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
                HourlyWeatherVal(hourData, isNow = index == 0)
            }
            }
        }
    }
}

@Composable
fun HourlyWeatherVal(hour: HourWeather, isNow: Boolean) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isNow) "NOW" else hour.time.split(" ")[1].substring(0, 5),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold
        )
        CoilImage(
            modifier = Modifier.size(48.dp),
            imageModel = { "https:${hour.condition.icon}".replace("64x64", "128x128") }
        )
        Text(
            text = "${hour.temp_c}°C", fontSize = 14.sp
        )
    }
}

@Composable
fun WeatherKeyVal(key : String, value : String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

