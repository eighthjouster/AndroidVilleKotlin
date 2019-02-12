package com.zapposandroid.rafaep.androidvillekotlin

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import java.util.ArrayList

import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ServerCommService : SafeApiCall() {
    private var service: AndroidvilleAPIService? = null

    init {
        val retrofit = Retrofit.Builder()
        //.baseUrl("http://10.0.2.2:3010") // Emulator's host machine (localhost parent.)
        .baseUrl("http://androidville.rppalencia.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        service = retrofit.create(AndroidvilleAPIService::class.java)
    }

    suspend fun getAllHouses(): AVHouseList? {
        return safeCall(
            call = { service?.getAVHousesAsync()?.await() },
            errorMessage = "Error fetching all houses"
        )
    }

    suspend fun addHouse(house: AVHouse): AVHouse? {
        return safeCall(
            call = { service?.postAVHouseAsync(house)?.await() },
            errorMessage = "Error fetching house"
        )
    }

    suspend fun updateHouse(house: AVHouse): AVHouse? {
        return safeCall(
            call = { service?.postAVHouseAsync(house)?.await() },
            errorMessage = "Error updating house"
        )
    }

    suspend fun deleteHouse(house: AVHouse): AVHouse? {
        return safeCall(
            call = { service?.deleteAVHouseAsync(house.id)?.await() },
            errorMessage = "Error deleting house"
        )
    }
}
