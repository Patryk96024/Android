package com.example.weather

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.animation.animateColorAsState

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsPage(onSettingsChanged = {
                setResult(RESULT_OK)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onSettingsChanged: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE) }

    var darkTheme by remember { mutableStateOf(prefs.getBoolean("darkTheme", false)) }
    val unitOptions = listOf("Celsius", "Fahrenheit", "Kelvin")
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember {
        mutableStateOf(prefs.getString("temperatureUnit", "Celsius") ?: "Celsius")
    }

    val targetTopColor = if (!darkTheme) "#d8cbf5" else "#2f2c36"
    val targetBgColor = if (!darkTheme) "#e3d9fa" else "#373242"
    val animatedBgColor by animateColorAsState(Color(targetBgColor.toColorInt()))
    val txtColor = if (!darkTheme) "#000000" else "#FFFFFF"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(targetTopColor.toColorInt()),
                    titleContentColor = if (darkTheme) Color.White else Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(animatedBgColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Theme", color = Color(txtColor.toColorInt()))
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = darkTheme,
                    onCheckedChange = {
                        darkTheme = it
                        prefs.edit { putBoolean("darkTheme", it) }
                        onSettingsChanged()
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Temperature Unit", color = Color(txtColor.toColorInt()))
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedUnit)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        unitOptions.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedUnit = unit
                                    prefs.edit {
                                        putString("temperatureUnit", unit)
                                    }
                                    expanded = false
                                    onSettingsChanged()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}