package com.example.weatherforecast.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.AlertsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class AlertsViewModelTest{
    private lateinit var fakeRepo: AlertsRepository
    private lateinit var viewModel: AlertsViewModel
    val alert = AlertsData("","","",0.0,0.0,true)


    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp(){
        Dispatchers.setMain(StandardTestDispatcher())

        fakeRepo = mockk(relaxed = true)
        viewModel = AlertsViewModel(fakeRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun viewModelInsertAlert_Alert_Success() = runTest {
        coEvery { fakeRepo.insertAlert(alert) } returns 1L

        viewModel.insertAlert(alert)
        advanceUntilIdle()

        val response = viewModel.response.first()
        assertEquals(Response.Success, response)
    }

    @Test
    fun viewModelGetAllAlerts_Success() = runTest {
        coEvery { fakeRepo.getAllAlerts() } returns flowOf(listOf(alert))

        viewModel.getAllAlerts()

        val response = viewModel.response.first { it == Response.Success }
        assertEquals(Response.Success, response)
    }


}