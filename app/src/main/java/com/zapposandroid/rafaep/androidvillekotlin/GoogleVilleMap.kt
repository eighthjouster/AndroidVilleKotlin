package com.zapposandroid.rafaep.androidvillekotlin

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

const val MARKER_WIDTH = 72
const val MARKER_HEIGHT = 72

enum class MarkerType {
    HOUSE, SELECTED_HOUSE, SELECTED_SPOT
}

data class MarkerInfo(val houseId: Int, val type: MarkerType)

class GoogleVilleMap(parentResources: Resources) {
    private lateinit var mGoogleMap: GoogleMap
    private val parentActivityResources = parentResources

    private val houseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.house_icon), MARKER_WIDTH, MARKER_HEIGHT, false);
    private val selectedHouseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.house_selected_icon), MARKER_WIDTH, MARKER_HEIGHT, false);
    private val selectedSpotBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.spot_selected_icon), MARKER_WIDTH, MARKER_HEIGHT, false);

    private var selectedSpotMarker: Marker? = null
    private var selectedHouseMarker: Marker? = null

    var txtHouseName: TextView? = null
    var houseActions: HouseActions? = null

    var houses: List<AVHouse>? = null
    var selectedHouse: AVHouse? = null

    var selectedSpotPosition: LatLng? = null

    private val onMapClickListener = GoogleMap.OnMapClickListener {
        if (selectedSpotMarker != null) {
            selectedSpotMarker?.remove()
            selectedSpotMarker = null
        }

        if (selectedHouseMarker != null) {
            selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.HOUSE))
            selectedHouseMarker = null
        }

        selectedSpotMarker = addMarker(it, MarkerType.SELECTED_SPOT, "Yo. Selected area!") //__RP change caption.

        selectedSpotPosition = selectedSpotMarker?.position

        houseActions?.setHouseEditMode(false)
    }

    fun unSelectSpot() {
        if (selectedSpotMarker != null) {
            selectedSpotMarker?.remove()
        }

        selectedSpotMarker = null
        selectedSpotPosition = null
    }

    private val onMarkerClickListener = GoogleMap.OnMarkerClickListener {
        val markerInfo = it.tag as MarkerInfo
        var letMapMarkIt = true
        when(markerInfo.type) {
            MarkerType.SELECTED_SPOT -> {
                it.remove()
                selectedSpotMarker = null
                letMapMarkIt = false
                houseActions?.setHouseEditMode(false)
            }
            MarkerType.HOUSE, MarkerType.SELECTED_HOUSE -> {
                if (selectedSpotMarker != null) {
                    selectedSpotMarker?.remove()
                    selectedSpotMarker = null
                }
                if (selectedHouseMarker != null) {
                    selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.HOUSE))
                    selectedHouseMarker = null
                }
                selectedHouseMarker = it
                selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.SELECTED_HOUSE))
                houseActions?.setHouseEditMode(true)


                txtHouseName?.text = ""
                val allHouses = houses.orEmpty()
                for (house in allHouses) {
                    selectedHouse?.selected = false
                    if (house.id == markerInfo.houseId) {
                        selectedHouse = house
                        selectedHouse?.selected = true
                        txtHouseName?.text = house.name
                    }
                }
            }
        }
        !letMapMarkIt
    }

    private fun getMarkerIconFromType(type: MarkerType): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(when(type) {
            MarkerType.HOUSE -> houseBitmap
            MarkerType.SELECTED_HOUSE -> selectedHouseBitmap
            else -> selectedSpotBitmap // Assumed MarkerType.SELECTED_SPOT
        })
    }

    private fun addMarker(latLng: LatLng, type: MarkerType, caption: String, houseId: Int = 0): Marker {
       val marker = mGoogleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(caption)
                .icon(getMarkerIconFromType(type)))
       marker.tag = MarkerInfo(houseId, type)
       return marker
    }

    fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        // Add a marker in Sydney and move the camera
        val lasVegasLocation = LatLng(36.1728546, -115.1390953)
        addMarker(lasVegasLocation, MarkerType.HOUSE, "Marker in Las Vegas!")

        val lasVegasLocationABitBelow = LatLng(36.1719972, -115.1390738)
        addMarker(lasVegasLocationABitBelow, MarkerType.HOUSE, "Marker in Las Vegas 2!")

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(lasVegasLocation))
        mGoogleMap.animateCamera( CameraUpdateFactory.zoomTo( 16.5f ) )
        mGoogleMap.setOnMapClickListener(onMapClickListener)
        mGoogleMap.setOnMarkerClickListener(onMarkerClickListener)
    }
}