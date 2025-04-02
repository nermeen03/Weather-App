package com.example.weatherforecast.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.CountriesListItem
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.NameResponseItem
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.data.repo.IFavLocationsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import java.io.InputStreamReader

class FavLocationsViewModel(private val favLocationsRepository: IFavLocationsRepository):ViewModel(){


    private val mutableLocResponse = MutableStateFlow(Response.Loading as Response<*>)
    val locResponse = mutableLocResponse.asStateFlow()

    private val mutableDetailsResponse = MutableStateFlow(Response.Loading as Response<*>)
    val detailsResponse = mutableDetailsResponse.asStateFlow()

    private val mutableResponse = MutableStateFlow(Response.Loading as Response<*>)
    val response = mutableResponse.asStateFlow()

    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableResponse.value = Response.Failure(exception)
        Log.e("TAG", "Error inserting location", exception)
    }

    private val mutableFavDetails = MutableStateFlow<FavDetails?>(null)
    val favDetail =mutableFavDetails.asStateFlow()

    private val mutableFavLocations = MutableStateFlow<List<Location>>(emptyList())
    val favLocations =mutableFavLocations.asStateFlow()

    private val mutableLocationName = MutableStateFlow<NameResponseItem?>(null)
    val locationName =mutableLocationName.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    private val _countryList = MutableStateFlow<List<CountriesListItem>>(emptyList())

    private val _filteredCountries = MutableStateFlow<List<CountriesListItem>>(emptyList())
    val filteredCountries: StateFlow<List<CountriesListItem>> = _filteredCountries

     fun insertLocation(location: Location){
         viewModelScope.launch(Dispatchers.IO + handle) {
             try {
                 val result = favLocationsRepository.insertFav(location)

                 if (result > 0) {
                     mutableLocResponse.value = Response.Success
                 } else {
                     mutableLocResponse.value = Response.Failure(Exception("Insert failed"))
                 }
             } catch (e: Exception) {
                 Log.e("ViewModel", "Error inserting location", e)
                 mutableLocResponse.value = Response.Failure(e)
             }
         }
     }
    fun insertLocation(favDetails: FavDetails){
       viewModelScope.launch(Dispatchers.IO+handle) {
           val result = favLocationsRepository.insertFavDetail(favDetails)
           if (result > 0) {
               mutableDetailsResponse.value = Response.Success
           } else {
               mutableDetailsResponse.value = Response.Failure(Exception("Insert failed"))
           }
       }
    }


    fun deleteLocation(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO + handle) {
            val result = favLocationsRepository.deleteFav(lat, lon)
            if (result > 0) {
                mutableLocResponse.value = Response.Success
            } else {
                mutableLocResponse.value = Response.Failure(Exception("Failed to delete location"))
            }
        }
    }
    fun deleteLocationDetail(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO + handle) {
            val result2 = favLocationsRepository.deleteFavDetails(lon, lat)
            if (result2 > 0) {
                mutableDetailsResponse.value = Response.Success
            } else {
                mutableDetailsResponse.value = Response.Failure(Exception("Failed to delete details"))
            }
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

    fun getFavDetails(lon: Double, lat: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            favLocationsRepository.getFavDetail(lon, lat)
                .onStart {
                    Log.d("TAG", "Fetching details...")
                    mutableDetailsResponse.value = Response.Loading
                }
                .catch { e ->
                    Log.e("TAG", "Error fetching details: ${e.message}")
                    mutableDetailsResponse.value = Response.Failure(e)
                }
                .collect { details ->
                    Log.d("TAG", "Data retrieved successfully")
                    mutableFavDetails.value = details
                    mutableDetailsResponse.value = Response.Success
                }
        }
    }



    fun getLocationName(lat: Double, lon: Double) {
        mutableResponse.value = Response.Loading
        viewModelScope.launch(Dispatchers.IO + handle) {
            favLocationsRepository.getLocationName(lat, lon)
                .distinctUntilChanged()
                .retry(3)
                .catch { e -> mutableResponse.value = Response.Failure(e) }
                .collect { result ->
                    if (result.isNotEmpty()) {
                        mutableLocationName.value = result[0]
                        mutableResponse.value = Response.Success
                    } else {
                        mutableResponse.value = Response.Failure(Exception("No location found"))
                    }
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

    fun addMark(map: MapLibreMap?, latLng: LatLng) {
        _selectedLocation.value = latLng

        map?.let {
            it.clear()
            it.addMarker(
                org.maplibre.android.annotations.MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
            )
        }
    }



    fun loadCountries(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.lists)
                val reader = InputStreamReader(inputStream)
                val gson = Gson()

                val type = object : TypeToken<List<CountriesListItem>>() {}.type
                val countries: List<CountriesListItem> = gson.fromJson(reader, type)

                _countryList.value = countries

            } catch (e: Exception) {
                Log.e("TAG", "Error loading countries", e)
            }
        }
    }



    fun filterCountries(query: String) {
        _filteredCountries.value = if (query.isEmpty()) {
            emptyList()
        } else {
            _countryList.value.filter {
                it.name.contains(query, ignoreCase = true)
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