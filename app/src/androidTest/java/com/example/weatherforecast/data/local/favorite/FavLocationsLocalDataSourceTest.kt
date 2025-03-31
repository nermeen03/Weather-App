package com.example.weatherforecast.data.local.favorite

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavLocationsLocalDataSourceTest{
    private lateinit var dataBase: FavLocationsDataBase
    private lateinit var dao: FavLocationsDao
    private lateinit var favLocationsLocalDataSource: FavLocationsLocalDataSource

    private var location: FavDetails = FavDetails(
        WeatherDetails(0.0,0.0,"", Country("",""),0,0,0.0,0,""),
        emptyList(), emptyList(),0.0,0.0,"", emptyList())


    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavLocationsDataBase::class.java
        ).allowMainThreadQueries().build()
        dao = dataBase.getFavLocationsDao()

        favLocationsLocalDataSource = FavLocationsLocalDataSource(dao)
    }
    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun localDataSourceInsertFavDetail_Location_LongForResult() = runTest{
        val result = favLocationsLocalDataSource.insertFavDetail(location)
        advanceUntilIdle()
        assertEquals(result,1L)
    }

    @Test
    fun localDataSourceDeleteFavDetail_LatLon_IntForResult() = runTest{
        val result = favLocationsLocalDataSource.insertFavDetail(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = favLocationsLocalDataSource.deleteFavDetails(location.lon,location.lat)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

    @Test
    fun localDataSourceGetFavDetail_LatLon_FlowOfLocationData() = runTest{
        val result = favLocationsLocalDataSource.insertFavDetail(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = favLocationsLocalDataSource.getFavDetail(location.lon,location.lat).first()
        advanceUntilIdle()
        assertEquals(result2, location)
    }
}