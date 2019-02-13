package com.zapposandroid.rafaep.androidvillekotlin

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AndroidvilleAPIService {
    @GET("/city")
    fun getAVCityAsync(): Deferred<Response<AVCity>>

    @GET("/houses")
    fun getAVHousesAsync(): Deferred<Response<List<AVHouse>>>

    @POST("/houses")
    fun postAVHouseAsync(@Body house: AVHouse): Deferred<Response<AVHouse>>

    @PUT("/houses/{id}")
    fun putAVHouseAsync(@Path("id") id: Int, @Body house: AVHouse): Deferred<Response<AVHouse>>

    @DELETE("/houses/{id}")
    fun deleteAVHouseAsync(@Path("id") id: Int): Deferred<Response<AVHouse>>
}
