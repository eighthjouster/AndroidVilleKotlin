package com.zapposandroid.rafaep.androidvillekotlin

import android.app.Application

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainApp : Application() {
    var storedMainActivity: MainActivity? = null
    var serverComm: ServerCommService? = null
    var houseToHighlight = -1

    override fun onCreate() {
        super.onCreate()

        serverComm = ServerCommService()
    }

    fun getAllHouses(houseToHighlight: Int) {
        this.houseToHighlight = houseToHighlight
        getAllHouses()
    }

    fun getAllHouses() {
        serverComm.getAllHouses(Callback {
            override fun onResponse(call: Call<ArrayList<AVHouse>>, response: Response<ArrayList<AVHouse>>) {
                val houses = response.body()
                storedMainActivity.getVilleMap()?.setHouses(houses)
                if (houseToHighlight != -1) {
                    storedMainActivity.getVilleMap()?.highlightHouse(houseToHighlight)
                    houseToHighlight = -1
                }

                val houseSize: Int = houses?.size ?: 0
                for (i in 0..houseSize) {
                  val house: AVHouse? = houses?.get(i)
                  if (storedMainActivity.nextHouseId <= house?.id) {
                      storedMainActivity.nextHouseId = house?.id + 1
                  }
                }
            }


            override fun onFailure(call: Call<ArrayList<AVHouse>>, throwable: Throwable) {
                System.out.println(throwable)
            }
        })
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.storedMainActivity = mainActivity
        this.storedMainActivity.getVilleMap()?.setMainApp(this)
        this.storedMainActivity.vScroll?.setMainActivity(mainActivity)
    }
}
