package com.example.weatherforecast.data.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatherforecast.data.local.alerts.AlertsDao
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
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
class AlertsRepositoryTest{
    private lateinit var dataBase: AlertsDataBase
    private lateinit var dao: AlertsDao
    private lateinit var alertsLocalDataSource: AlertsLocalDataSource
    private lateinit var alertsRepository: AlertsRepository

    private var alertsData = AlertsData("23/0","1:30","loc",0.0,0.0, true)


    @Before
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AlertsDataBase::class.java).allowMainThreadQueries().build()
        dao = dataBase.getAlertsDao()

        alertsLocalDataSource = AlertsLocalDataSource(dao)
        alertsRepository = AlertsRepository(alertsLocalDataSource)

    }
    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun repoInsertAlert_Alert_LongForResult() = runTest{
        val result = dao.insert(alertsData)
        advanceUntilIdle()
        assertEquals(result,1L)

    }

    @Test
    fun repoDeleteAlert_AlertDateTimeLoc_IntForResult() = runTest{
        val result = dao.insert(alertsData)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.delete(alertsData.date,alertsData.time,alertsData.location)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

    @Test
    fun repoGetAlert_FlowListOfAlertData() = runTest{
        val result = dao.insert(alertsData)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.getAll().first()
        advanceUntilIdle()
        assertEquals(result2, listOf(alertsData))
    }

}