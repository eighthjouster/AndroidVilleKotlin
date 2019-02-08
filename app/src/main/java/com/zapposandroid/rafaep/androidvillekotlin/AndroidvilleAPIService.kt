package com.zapposandroid.rafaep.androidvillekotlin

import java.util.ArrayList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AndroidvilleAPIService {
    @GET("/city")
    fun getAVCity(): Call<AVCity>

    @GET("/houses")
    fun getAVHouses(): Call<ArrayList<AVHouse>>

    @POST("/houses")
    fun postAVHouse(@Body house: AVHouse): Call<AVHouse>

    @PUT("/houses/{id}")
    fun putAVHouse(@Path("id") id: Int, @Body house: AVHouse): Call<AVHouse>

    @DELETE("/houses/{id}")
    fun deleteAVHouse(@Path("id") id: Int): Call<AVHouse>
}
