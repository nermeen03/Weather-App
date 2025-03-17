package com.example.weatherforecast.view.favorite

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.weatherforecast.MainActivity
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.*
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import java.net.URL

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapScreen()
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val apiKey = "rnzDDmeWlNQlDmXga0CZ"

    MapLibre.getInstance(context, apiKey, WellKnownTileServer.MapTiler)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("Select a location") }
    val currentLocation by MainActivity.LocationManager.locationState.observeAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(emptyList<Location>()) }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            SearchBar(searchQuery, onSearchChanged = {
                searchQuery = it
                coroutineScope.launch {
                    delay(300)
                }
            }, searchResults, onLocationSelected = { place ->
                selectedLocation = LatLng(place.lat, place.lon)
                locationName = place.name
                searchResults = emptyList()
            })

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val mapView = MapView(ctx)
                    mapView.onCreate(null)

                    mapView.getMapAsync { map: MapLibreMap ->
                        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=$apiKey"

                        map.setStyle(styleUrl) {
                            map.uiSettings.isZoomGesturesEnabled = true
                            map.uiSettings.isScrollGesturesEnabled = true

                            val defaultLocation = LatLng(currentLocation?.first ?: 0.0, currentLocation?.second ?: 0.0)
                            map.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(defaultLocation)
                                        .zoom(10.0)
                                        .build()
                                )
                            )

                            map.addOnMapClickListener { latLng ->
                                selectedLocation = latLng
                                Log.d("MapLibreView", "Clicked Location: ${latLng.latitude}, ${latLng.longitude}")

                                map.clear()
                                map.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title("Selected Location")
                                )



                                true
                            }
                        }
                    }
                    mapView
                }
            )

        }

        selectedLocation?.let {
            SelectedLocation(it.latitude, it.longitude)
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onSearchChanged: (String) -> Unit,
    searchResults: List<Location>,
    onLocationSelected: (Location) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Box {
            BasicTextField(
                value = query,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                        Spacer(Modifier.width(8.dp))
                        innerTextField()
                    }
                }
            )
        }

        if (searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                searchResults.forEach { place ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                            onLocationSelected(place)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                        Spacer(Modifier.width(8.dp))
                        Text(place.name)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedLocation(lat: Double, long: Double) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selected: ($lat, $long)")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { Log.d("MapLibreView", "Location Saved: $lat, $long") },
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Add to Favorites")
                Text("Save Location")
            }
        }
    }
}

suspend fun fetchCitySuggestions(query: String, countryCode: String = "EG"): List<String> {
    val apiKey = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    val url = "https://api.openweathermap.org/geo/1.0/direct?q=$query&limit=5&appid=$apiKey"

    return withContext(Dispatchers.IO) {
        try {
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)
            val cityList = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val city = jsonArray.getJSONObject(i)
                val country = city.optString("country")

                if (country == countryCode) {  // Filter by country
                    val cityName = city.getString("name")
                    cityList.add(cityName)
                }
            }
            cityList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
