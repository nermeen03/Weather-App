package com.example.weatherforecast.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecast.data.local.favorite.FavLocationsDao
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.repo.FavLocationsRepository
import io.mockk.mockk
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
class FavLocationsLocalTest {

    private lateinit var dataBase: FavLocationsDataBase
    private lateinit var dao: FavLocationsDao
    private lateinit var favLocationsLocalDataSource: FavLocationsLocalDataSource
    private lateinit var favLocationsRemoteDataSource: FavLocationsRemoteDataSource
    private lateinit var favLocationsRepository: FavLocationsRepository

    private var location: Location = Location("city1","city2",2.0,2.0,"arabic")

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dataBase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            FavLocationsDataBase::class.java).allowMainThreadQueries().build()
        dao = dataBase.getFavLocationsDao()

        favLocationsLocalDataSource = FavLocationsLocalDataSource(dao)
        favLocationsRemoteDataSource = mockk()

        favLocationsRepository = FavLocationsRepository(favLocationsLocalDataSource,favLocationsRemoteDataSource)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() = Dispatchers.resetMain()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun daoInsertLocation_Location_LongForResult() = runTest{
        val result = dao.insert(location)
        advanceUntilIdle()
        assertEquals(result,1L)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun daoDeleteLocation_LatLon_IntForResult() = runTest{
        val result = dao.insert(location)
        advanceUntilIdle()
        assertEquals(result,1L)

        val result2 = dao.delete(location.lon,location.lat)
        advanceUntilIdle()
        assertEquals(result2,1)
    }

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

}