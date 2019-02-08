package com.zapposandroid.rafaep.androidvillekotlin

import android.app.Application

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                ArrayList<AVHouse> houses = response.body();
                mainActivity.getVilleMap().setHouses(houses);
                if (houseToHighlight != -1) {
                    mainActivity.getVilleMap().highlightHouse(houseToHighlight);
                    houseToHighlight = -1;
                }

                for (int i = 0; i < houses.size(); i++) {
                AVHouse house = houses.get(i);
                if (mainActivity.nextHouseId <= house.id) {
                    mainActivity.nextHouseId = house.id + 1;
                }
            }
            }

            @Override
            public void onFailure(Call<ArrayList<AVHouse>> call, Throwable throwable) {
                System.out.println(throwable);
            }
        });
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mainActivity.getVilleMap().setMainApp(this);
        this.mainActivity.vScroll.setMainActivity(mainActivity);
    }
}
