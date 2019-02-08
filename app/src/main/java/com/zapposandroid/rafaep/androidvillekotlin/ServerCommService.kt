package com.zapposandroid.rafaep.androidvillekotlin

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ServerCommService
{
    var service: AndroidvilleAPIService = null

    init {
      val retrofit = Retrofit.Builder()
        //.baseUrl("http://10.0.2.2:3010") // Emulator's host machine (localhost parent.)
        .baseUrl("http://androidville.rppalencia.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    service = retrofit.create(AndroidvilleAPIService.class);
}

    fun getAllHouses(responseHandler: Callback<ArrayList<AVHouse>>) {
        val callAsync = service.getAVHouses();
        callAsync.enqueue(responseHandler);
}

    fun addHouse(house: AVHouse, responseHandler: Callback<AVHouse>) {
      val callAsync = service.postAVHouse(house);
      callAsync.enqueue(responseHandler);
}

    fun updateHouse(house: AVHouse, responseHandler: Callback<AVHouse>) {
      val callAsync = service.putAVHouse(house.id, house);
      callAsync.enqueue(responseHandler);
    }

    fun deleteHouse(house: AVHouse, responseHandler: Callback<AVHouse>) {
        val callAsync = service.deleteAVHouse(house.id);
        callAsync.enqueue(responseHandler);
    }
}
