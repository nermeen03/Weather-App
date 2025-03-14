package com.example.weatherforecast.data.pojo

data class HourlyDetails(val time:String,val temp:Double,val feelLike:Double)

data class DailyDetails(val pressure:Int,val humidity:Int,val speed:Double,val cloud:Int)