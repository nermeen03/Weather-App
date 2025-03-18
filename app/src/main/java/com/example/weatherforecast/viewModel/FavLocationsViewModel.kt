package com.example.weatherforecast.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.data.repo.IDailyDataRepository
import com.example.weatherforecast.data.repo.Response
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

class FavLocationsViewModel(private val favLocationsRepository: FavLocationsRepository):ViewModel(){

    private val mutableResponse = MutableStateFlow(Response.Loading as Response<*>)
    val response = mutableResponse.asStateFlow()

    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableResponse.value = Response.Failure(exception)
    }

    private val mutableFavLocations = MutableStateFlow<List<Location>>(emptyList())
    private val favLocations =mutableFavLocations.asStateFlow()

    private val mutableLocationName = MutableStateFlow<Country?>(null)
    private val locationName =mutableLocationName.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

     fun insertLocation(location: Location){
       viewModelScope.launch(Dispatchers.IO+handle) {
            val result = favLocationsRepository.insertFav(location)
            if(result>=0){ mutableResponse.value = Response.Success }
            else{ mutableResponse.value = Response.Failure(Exception("an error occurred")) }
        }
    }
    fun deleteLocation(lat: Double, lon: Double){
        viewModelScope.launch(Dispatchers.IO+handle){
            val result = favLocationsRepository.deleteFav(lat, lon)
            if(result>=0){ mutableResponse.value = Response.Success }
            else{ mutableResponse.value = Response.Failure(Exception("an error occurred")) }
        }
    }
    fun getAllFav(){
        viewModelScope.launch(Dispatchers.IO+handle){
            favLocationsRepository.getAllFav().distinctUntilChanged().retry(3)
                .catch { e -> mutableResponse.value = Response.Failure(e) }
                .collect {
                    mutableFavLocations.value = it
                    mutableResponse.value = Response.Success
                }
        }
    }

    fun getLocationName(lat: Double, lon: Double){
        viewModelScope.launch(Dispatchers.IO+handle) {
            favLocationsRepository.getLocationName(lat, lon).distinctUntilChanged().retry(3)
                .catch { e -> mutableResponse.value = Response.Failure(e) }
                .collect {
                    mutableLocationName.value = it
                    mutableResponse.value = Response.Success
                }
        }
    }

    fun onMapReady(map: MapLibreMap, apiKey: String) {
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true

        map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
            val weatherSource = org.maplibre.android.style.sources.RasterSource(
                "weather",
                org.maplibre.android.style.sources.TileSet(
                    "2.0.0",
                    "https://tile.openweathermap.org/map/clouds/{z}/{x}/{y}.png?appid=$apiKey"
                )
            )
            val weatherLayer = org.maplibre.android.style.layers.RasterLayer("weather-layer", "weather")

            style.addSource(weatherSource)
            style.addLayer(weatherLayer)

            val defaultLocation = LatLng(30.0444, 31.2357)
            map.moveCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(defaultLocation)
                        .zoom(5.0)
                        .build()
                )
            )

            map.addOnMapClickListener { latLng ->
                _selectedLocation.value = latLng
                Log.d("MapViewModel", "Clicked Location: ${latLng.latitude}, ${latLng.longitude}")

                map.clear()
                map.addMarker(
                    org.maplibre.android.annotations.MarkerOptions()
                        .position(latLng)
                        .title("Selected Location")
                )
                true
            }
        }
    }
    class FavLocationsViewModelFactory(private val favLocationsRepository: FavLocationsRepository):
        ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavLocationsViewModel(favLocationsRepository) as T
        }
    }
}