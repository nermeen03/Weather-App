package com.example.weatherforecast.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.repo.FavLocationsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavLocationsViewModelTest{
    private lateinit var fakeRepo: FavLocationsRepository
    private lateinit var viewModel: FavLocationsViewModel

    private val location = Location("","",0.0,0.0,"")
    private val favDetails :FavDetails = FavDetails(
        WeatherDetails(0.0,0.0,"", Country("",""),0,0,0.0,0,""),
        emptyList(), emptyList(),0.0,0.0,"", emptyList())

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp(){
        Dispatchers.setMain(StandardTestDispatcher())

        fakeRepo = mockk(relaxed = true)
        viewModel = FavLocationsViewModel(fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun viewModelInsertLocation_Location_Success() = runTest {
        coEvery { fakeRepo.insertFav(location) } returns 1L

        viewModel.insertLocation(location)
        advanceUntilIdle()

        val response = viewModel.locResponse.first { it == Response.Success }
        assertEquals(Response.Success, response)
    }

    @Test
    fun viewModelDeleteLocation_LatLon_Success() = runTest {
        coEvery { fakeRepo.deleteFav(location.lat,location.lon) } returns 1

        viewModel.deleteLocation(location.lat,location.lon)
        advanceUntilIdle()

        val response = viewModel.locResponse.first { it == Response.Success }
        assertEquals(Response.Success, response)
    }

    @Test
    fun viewModelInsertDetail_Location_Success() = runTest {
        coEvery { fakeRepo.insertFavDetail(favDetails) } returns 1L

        viewModel.insertLocation(favDetails)
        advanceUntilIdle()

        val response = viewModel.detailsResponse.first { it == Response.Success }
        assertEquals(Response.Success, response)
    }
}