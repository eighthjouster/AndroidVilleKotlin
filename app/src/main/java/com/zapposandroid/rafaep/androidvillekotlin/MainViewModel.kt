package com.zapposandroid.rafaep.androidvillekotlin

import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var nextHouseId = 1
    var houseEditMode = false
    var houseToHighlight = -1
    var allHouses: List<AVHouse>? = null
}