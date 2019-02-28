package com.zapposandroid.rafaep.androidvillekotlin

import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var nextHouseId = 1
    var houseEditMode = false
    var houseToHighlight = -1
    var allHouses: List<AVHouse>? = null
    var cameraTargetLatitude: Double = 0.0
    var cameraTargetLongitude: Double = 0.0
    var cameraZoom: Float = 0f
}