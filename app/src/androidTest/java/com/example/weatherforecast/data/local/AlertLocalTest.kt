package com.example.weatherforecast.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecast.data.local.alerts.AlertsDao
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.AlertsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertLocalTest {

    private lateinit var dataBase: AlertsDataBase
    private lateinit var dao: AlertsDao
    private lateinit var alertsLocalDataSource: AlertsLocalDataSource
    private lateinit var alertsRepository: AlertsRepository

    private var alertsData = AlertsData("23/0","1:30","loc",0.0,0.0, true)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dataBase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            AlertsDataBase::class.java).allowMainThreadQueries().build()
        dao = dataBase.getAlertsDao()

        alertsLocalDataSource = AlertsLocalDataSource(dao)
        alertsRepository = AlertsRepository(alertsLocalDataSource)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() = Dispatchers.resetMain()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun daoInsertLocation_Location_LongForResult() = runTest{
        val result = dao.insert(alertsData)
        advanceUntilIdle()
        assertEquals(result,1L)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun daoDeleteLocation_LatLon_IntForResult() = runTest{
        val result = dao.insert(alertsData)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.delete(alertsData.date,alertsData.time,alertsData.location)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

/*
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun localDataSourceInsert_Location_LongForResult() = runTest{
        val result = favLocationsLocalDataSource.insertFav(location)
        advanceUntilIdle()
        assertEquals(result,1L)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun localDataSourceDelete_LatLon_IntForResult() = runTest{
        val result = favLocationsLocalDataSource.insertFav(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = favLocationsLocalDataSource.deleteFav(location.lon,location.lat)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun repoInsert_Location_LongForResult() = runTest{
        val result = favLocationsRepository.insertFav(location)
        advanceUntilIdle()
        assertEquals(result,1L)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun repoDelete_Location_IntForResult() = runTest{
        val result = favLocationsRepository.insertFav(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = favLocationsRepository.deleteFav(location.lat,location.lon)
        advanceUntilIdle()
        assertEquals(result2,1)
    }
*/

}