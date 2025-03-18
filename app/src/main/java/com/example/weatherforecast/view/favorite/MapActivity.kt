import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.data.local.FavLocationsDao
import com.example.weatherforecast.data.local.FavLocationsDataBase
import com.example.weatherforecast.data.local.FavLocationsLocalDataSource
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import com.example.weatherforecast.viewModel.FavLocationsViewModel
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.*
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

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
    val apiKey = "2fc5f5f3f6a9b61df9391d8ae569f5e0"

    val viewModel:FavLocationsViewModel = viewModel(factory = FavLocationsViewModel.FavLocationsViewModelFactory(
        FavLocationsRepository.getRepository(FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
            FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
        )))

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val mapView = MapView(ctx).apply { onCreate(null) }
                mapView.getMapAsync { map ->
                    viewModel.onMapReady(map, apiKey)
                }
                mapView
            }
        )

        selectedLocation?.let {
            SelectedLocation(it.latitude, it.longitude)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(onClick = {
                mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn())
            }) {
                Text("+")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(onClick = {
                mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut())
            }) {
                Text("-")
            }
        }
    }
}


@Composable
fun SelectedLocation(lat: Double, long: Double) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(colorResource(R.color.blue)),
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
