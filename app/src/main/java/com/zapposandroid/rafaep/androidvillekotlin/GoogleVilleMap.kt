package com.zapposandroid.rafaep.androidvillekotlin

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

const val MARKER_WIDTH = 72
const val MARKER_HEIGHT = 72

enum class MarkerType {
    HOUSE, SELECTED_SPOT
}

class GoogleVilleMap(parentResources: Resources) : OnMapReadyCallback {
    private lateinit var mGoogleMap: GoogleMap
    private val parentActivityResources = parentResources

    private val houseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.house_icon), MARKER_WIDTH, MARKER_HEIGHT, false);
    private val selectedSpotBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(parentActivityResources, R.drawable.spot_selected_icon), MARKER_WIDTH, MARKER_HEIGHT, false);

    private var selectedSpotMarker: Marker? = null;

    private val onMapClickListener = GoogleMap.OnMapClickListener {
        if (selectedSpotMarker != null) {
            selectedSpotMarker?.remove()
        }

        selectedSpotMarker = addMarker(it, MarkerType.SELECTED_SPOT, "Yo. Selected area!")
    }

    private val onMarkerClickListener = GoogleMap.OnMarkerClickListener {
        val markerType = it.tag as MarkerType
        when(markerType) {
            MarkerType.SELECTED_SPOT -> it.remove()
        }
        true
    }

    private fun addMarker(latLng: LatLng, type: MarkerType, caption: String): Marker {
       val marker = mGoogleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(caption)
                .icon(BitmapDescriptorFactory.fromBitmap(when(type) {
                    MarkerType.HOUSE -> houseBitmap
                    else -> selectedSpotBitmap // Assumed MarkerType.SELECTED_SPOT
                })))
       marker.tag = type
       return marker
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        // Add a marker in Sydney and move the camera
        val lasVegasLocation = LatLng(36.1728546, -115.1390953)
        addMarker(lasVegasLocation, MarkerType.HOUSE, "Marker in Las Vegas!")

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(lasVegasLocation))
        mGoogleMap.animateCamera( CameraUpdateFactory.zoomTo( 16.5f ) )
        mGoogleMap.setOnMapClickListener(onMapClickListener)
        mGoogleMap.setOnMarkerClickListener(onMarkerClickListener)
    }

}