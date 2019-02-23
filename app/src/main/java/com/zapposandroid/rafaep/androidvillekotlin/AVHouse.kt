package com.zapposandroid.rafaep.androidvillekotlin

import com.google.android.gms.maps.model.Marker

data class AVHouse(val id: Int, var name: String, val address: AVAddress, @Transient var selected: Boolean?, @Transient var associatedMapMarker: Marker?)
