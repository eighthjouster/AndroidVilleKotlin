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
    lateinit var mGoogleMap: GoogleMap
    private val parentActivityResources = parentResources

    private val houseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.house_icon), MARKER_WIDTH, MARKER_HEIGHT, false)
    private val selectedHouseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.house_selected_icon), MARKER_WIDTH, MARKER_HEIGHT, false)
    private val selectedSpotBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.spot_selected_icon), MARKER_WIDTH, MARKER_HEIGHT, false)

    private var selectedSpotMarker: Marker? = null
    private var selectedHouseMarker: Marker? = null

    var mainViewModel: MainViewModel? = null

    var txtHouseName: TextView? = null
    var houseActions: HouseActions? = null

    private var allMarkers: MutableList<Marker>? = null

    private var mHouses: MutableList<AVHouse>? = null
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

        selectedHouse = null

        setSelectedPosition(it)

        houseActions?.setHouseEditMode(false)
    }

    fun unSelectHouse() {
        selectedHouse = null
        selectedHouseMarker = null
    }

    fun unSelectSpot() {
        if (selectedSpotMarker != null) {
            selectedSpotMarker?.remove()
            mainViewModel?.selectedSpotLatitude = -1.0
            mainViewModel?.selectedSpotLongitude = -1.0
        }

        selectedSpotMarker = null
        selectedSpotPosition = null
    }

    private val onMarkerClickListener = GoogleMap.OnMarkerClickListener {
        val markerInfo = it.tag as MarkerInfo
        var letMapMarkIt = true
        unSelectSpot()
        when(markerInfo.type) {
            MarkerType.SELECTED_SPOT -> {
                letMapMarkIt = false
                houseActions?.setHouseEditMode(false)
            }
            MarkerType.HOUSE, MarkerType.SELECTED_HOUSE -> {
                if (selectedHouseMarker != null) {
                    selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.HOUSE))
                    selectedHouseMarker = null
                }
                selectedHouseMarker = it
                selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.SELECTED_HOUSE))
                houseActions?.setHouseEditMode(true)


                txtHouseName?.text = ""
                for (house in mHouses.orEmpty()) {
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

    fun setSelectedPosition(latLng: LatLng) {
        selectedSpotMarker = addMarker(latLng, MarkerType.SELECTED_SPOT, "")
        selectedSpotPosition = selectedSpotMarker?.position
    }

    private fun addMarker(latLng: LatLng, type: MarkerType, caption: String, houseId: Int = 0): Marker? {
       val marker = mGoogleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(caption)
                .icon(getMarkerIconFromType(type)))
       marker.tag = MarkerInfo(houseId, type)
       allMarkers?.add(marker)
       return marker
    }

    fun onMapReady(googleMap: GoogleMap, mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
        mGoogleMap = googleMap
        allMarkers = mutableListOf()

        // Add a marker in Las Vegas and move the camera
        val lasVegasLocation = LatLng(36.1728546, -115.1390953)

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(lasVegasLocation))
        mGoogleMap.animateCamera( CameraUpdateFactory.zoomTo( 16.5f ) )
        mGoogleMap.setOnMapClickListener(onMapClickListener)
        mGoogleMap.setOnMarkerClickListener(onMarkerClickListener)
    }

    fun setHouses(houses: MutableList<AVHouse>?) {
        for (thisMarker: Marker in allMarkers.orEmpty()) {
            thisMarker.remove()
        }
        for (thisHouse: AVHouse in houses.orEmpty()) {
            addHouse(thisHouse)
        }
    }

    fun addHouse(thisHouse: AVHouse, showInfoWindow: Boolean = false) {
        if (mHouses == null) {
            mHouses = mutableListOf()
        }
        mHouses?.add(thisHouse)
        thisHouse?.associatedMapMarker = addMarker(thisHouse?.address.position, MarkerType.HOUSE, thisHouse.name, thisHouse.id)
        if (showInfoWindow) {
            thisHouse?.associatedMapMarker?.showInfoWindow()
        }
    }

    fun updateHouse(thisHouse: AVHouse, updatedHouse: AVHouse) {
        thisHouse?.name = updatedHouse?.name

        thisHouse?.associatedMapMarker?.title = thisHouse.name
        thisHouse?.associatedMapMarker?.showInfoWindow()
        txtHouseName?.text = thisHouse.name
    }

    fun deleteHouse(houseId: Int) {
        for (house in mHouses.orEmpty()) {
            if (house.id == houseId) {
                allMarkers?.remove(house.associatedMapMarker)
                house.associatedMapMarker?.remove()
                house.associatedMapMarker = null
            }
        }
    }

    fun highlightHouse(houseId: Int): Boolean {
        if (selectedHouse != null) {
            selectedHouse?.selected = false
            selectedHouse = null
        }

        for (house in mHouses.orEmpty()) {
            if (house.id == houseId) {
                house.associatedMapMarker?.setIcon(getMarkerIconFromType(MarkerType.SELECTED_HOUSE))
                selectedHouse = house
                selectedHouse?.selected = true

                if (selectedHouseMarker != null) {
                    selectedHouseMarker?.setIcon(getMarkerIconFromType(MarkerType.HOUSE))
                    selectedHouseMarker = null
                }

                selectedHouseMarker = selectedHouse?.associatedMapMarker
                selectedHouseMarker?.showInfoWindow()
                txtHouseName?.text = house.name

                break
            }
        }

        return (selectedHouseMarker != null)
    }
}