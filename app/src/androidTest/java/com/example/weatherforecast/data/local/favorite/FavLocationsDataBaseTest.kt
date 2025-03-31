package com.example.weatherforecast.data.local.favorite

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatherforecast.data.pojo.Location
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
class FavLocationsDataBaseTest{
    private lateinit var dataBase: FavLocationsDataBase
    private lateinit var dao: FavLocationsDao
    private var location: Location = Location("city1","city2",2.0,2.0,"arabic")

    @Before
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavLocationsDataBase::class.java).allowMainThreadQueries().build()
        dao = dataBase.getFavLocationsDao()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun daoInsertLocation_Location_LongForResult() = runTest{
        val result = dao.insert(location)
        advanceUntilIdle()
        assertEquals(result,1L)
    }

    @Test
    fun daoDeleteLocation_LatLon_IntForResult() = runTest{
        val result = dao.insert(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.delete(location.lon,location.lat)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

    @Test
    fun daoGetLocation_LatLon_FlowListOfLocationData() = runTest{
        val result = dao.insert(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.getAll().first()
        advanceUntilIdle()
        assertEquals(listOf(location), result2)
    }

}