package com.example.weatherforecast.viewModel

import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.repo.FavLocationsRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FavLocationsViewModelTest {

    private var location:Location = Location("city1","city2",2.0,2.0,"arabic")

    private lateinit var fakeRepo:FavLocationsRepository
    private lateinit var viewModel: FavLocationsViewModel


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp(){
        Dispatchers.setMain(StandardTestDispatcher())

        fakeRepo = mockk(relaxed = true)
        viewModel = FavLocationsViewModel(fakeRepo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() = Dispatchers.resetMain()

/*    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertLocation_Location_UpdateResponseToSuccess() = runTest{
        coEvery { fakeRepo.insertFav(location) } returns 1L

        viewModel.insertLocation(location)
        advanceUntilIdle()

        val result = viewModel.response.first() { it is Response.Success }
        assertEquals(Response.Success,result)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteLocation_LonLat_UpdateResponseToSuccess() = runTest{
        coEvery { fakeRepo.insertFav(location) } returns 1L

        viewModel.insertLocation(location)
        advanceUntilIdle()

        val result = viewModel.response.first() { it is Response.Success }
        assertEquals(Response.Success,result)

        coEvery { fakeRepo.deleteFav(location.lat, location.lon) } returns 1

        viewModel.deleteLocation(location.lat, location.lon)

        val result2 = viewModel.response.first() { it is Response.Success }
        assertEquals(Response.Success,result2)

    }*/


}