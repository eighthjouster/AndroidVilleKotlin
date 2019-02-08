package com.zapposandroid.rafaep.androidvillekotlin

import android.app.Application

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.lang.reflect.Array

class MainApp : Application() {
    var mainActivity: MainActivity = null;
    var serverComm: ServerCommService = null;
    var houseToHighlight = -1;

    override fun onCreate() {
        super.onCreate();

        serverComm = ServerCommService();
    }

    fun getAllHouses(houseToHighlight: Int) {
        this.houseToHighlight = houseToHighlight;
        getAllHouses()
    }

    fun getAllHouses() {
        serverComm.getAllHouses(Callback {
            override fun onResponse(call: Call<ArrayList<AVHouse>>, response: Response<ArrayList<AVHouse>>) {
                val houses = response.body();
                mainActivity.getVilleMap().setHouses(houses);
                if (houseToHighlight != -1) {
                    mainActivity.getVilleMap().highlightHouse(houseToHighlight);
                    houseToHighlight = -1;
                }

                val houseSize: Int = houses?.size ?: 0
                for (i in 0..houseSize) {
                  val house: AVHouse? = houses?.get(i)
                  if (mainActivity.nextHouseId <= house?.id) {
                      mainActivity.nextHouseId = house?.id + 1;
                  }
                }
            }


            override fun onFailure(call: Call<ArrayList<AVHouse>>, throwable: Throwable) {
                System.out.println(throwable);
            }
        })
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity;
        this.mainActivity.getVilleMap().setMainApp(this);
        this.mainActivity.vScroll.setMainActivity(mainActivity);
    }
}
