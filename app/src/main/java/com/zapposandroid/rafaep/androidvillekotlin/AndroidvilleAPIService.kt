package com.zapposandroid.rafaep.androidvillekotlin

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AndroidvilleAPIService {
    @GET("/city")
    Call<AVCity> getAVCity();

    @GET("/houses")
    Call<ArrayList<AVHouse>> getAVHouses();

    @POST("/houses")
    Call<AVHouse> postAVHouse(@Body AVHouse house);

    @PUT("/houses/{id}")
    Call<AVHouse> putAVHouse(@Path("id") int id, @Body AVHouse house);

    @DELETE("/houses/{id}")
    Call<AVHouse> deleteAVHouse(@Path("id") int id);
}
