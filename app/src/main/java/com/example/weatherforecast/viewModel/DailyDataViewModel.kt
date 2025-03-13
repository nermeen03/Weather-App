package com.example.weatherforecast.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.repo.IDailyDataRepository
import kotlinx.coroutines.launch

class DailyDataViewModel(private val dataRepository: IDailyDataRepository):ViewModel(){
    private val mutableDailyData:MutableLiveData<ForecastDataResponse> = MutableLiveData()
    val dailyData:LiveData<ForecastDataResponse> = mutableDailyData

    fun getDailyData(){
        viewModelScope.launch {
            dataRepository.getDailyData()?.let {
                mutableDailyData.value = it
            }
        }

    }
}
class DailyDataViewModelFactory(private val dataRepository: IDailyDataRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DailyDataViewModel(dataRepository) as T
    }
}
