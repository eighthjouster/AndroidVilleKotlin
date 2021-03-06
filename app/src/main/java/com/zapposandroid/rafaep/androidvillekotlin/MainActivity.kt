package com.zapposandroid.rafaep.androidvillekotlin

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import com.google.android.gms.maps.CameraUpdateFactory

class MainActivity : HouseActions, AppCompatActivity(), OnMapReadyCallback, CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var houseDialogTextField: EditText? = null
    private var googleVilleMap : GoogleVilleMap? = null

    private var dialogLayout: ConstraintLayout? = null

    private var addEditButton: Button? = null
    private var deleteButton: Button? = null
    private var addEditDialogButton: Button? = null
    private var cancelDialogButton: Button? = null
    private var slideUpAnimation: AnimatorSet? = null
    private var slideDownAnimation: AnimatorSet? = null

    private var nextHouseId = 1
    private var selectedHouseName: TextView? = null
    private var houseEditMode = false
    private var houseToHighlight = -1
    private var mAllHouses: MutableList<AVHouse>? = null

    private var serverComm: ServerCommService? = null

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serverComm = ServerCommService()

        setContentView(R.layout.activity_main)

        dialogLayout = findViewById(R.id.ll_house_dialog)
        houseDialogTextField = findViewById(R.id.txt_input_house_name)
        addEditButton = findViewById(R.id.btn_add_house)
        deleteButton = findViewById(R.id.btn_delete_house)
        addEditDialogButton = findViewById(R.id.action_button)
        cancelDialogButton = findViewById(R.id.cancel_button)
        selectedHouseName = findViewById(R.id.txt_house_name)

        slideUpAnimation = AnimatorInflater.loadAnimator(this,
        R.animator.slide_up) as AnimatorSet
        slideUpAnimation?.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }
            override fun onAnimationEnd(animator: Animator) {
                addEditButton?.visibility = View.INVISIBLE
                deleteButton?.visibility = View.INVISIBLE
                cancelDialogButton?.visibility = View.VISIBLE
                houseDialogTextField?.requestFocus()
                showSoftKeyboard()
            }
            override fun onAnimationCancel(animator: Animator) {
            }
            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        slideUpAnimation?.setTarget(dialogLayout)

        slideDownAnimation = AnimatorInflater.loadAnimator(this,
        R.animator.slide_down) as AnimatorSet
        slideDownAnimation?.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }
            override fun onAnimationEnd(animator: Animator) {
                addEditButton?.visibility = View.VISIBLE
                deleteButton?.visibility = View.VISIBLE
            }
            override fun onAnimationCancel(animator: Animator) {
            }
            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        slideDownAnimation?.setTarget(dialogLayout)

        houseDialogTextField?.isFocusableInTouchMode = true

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.googleVilleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHouseEditMode(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.houseToHighlight = googleVilleMap?.selectedHouse?.id ?: -1
        mainViewModel.cameraTargetLatitude = googleVilleMap?.mGoogleMap?.cameraPosition?.target?.latitude ?: 0.0
        mainViewModel.cameraTargetLongitude = googleVilleMap?.mGoogleMap?.cameraPosition?.target?.longitude ?: 0.0
        mainViewModel.cameraZoom = googleVilleMap?.mGoogleMap?.cameraPosition?.zoom ?: 0f

        mainViewModel.houseEditMode = houseEditMode

        mainViewModel.selectedSpotLatitude = googleVilleMap?.selectedSpotPosition?.latitude ?: -1.0
        mainViewModel.selectedSpotLongitude = googleVilleMap?.selectedSpotPosition?.longitude ?: -1.0
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleVilleMap = GoogleVilleMap(resources)
        googleVilleMap?.onMapReady(googleMap, mainViewModel)
        googleVilleMap?.houseActions = this
        googleVilleMap?.txtHouseName = findViewById(R.id.txt_house_name)

        val cameraZoom = mainViewModel.cameraZoom
        if (cameraZoom != 0f) {
            googleVilleMap?.mGoogleMap?.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        mainViewModel.cameraTargetLatitude,
                        mainViewModel.cameraTargetLongitude
                    )
                )
            )
            googleVilleMap?.mGoogleMap?.animateCamera(CameraUpdateFactory.zoomTo(cameraZoom))
        }

        launch(Dispatchers.Main) {
            retrieveMapData()
        }
    }

    fun addEditHouseBtnClick(v: View) {
        if ((googleVilleMap?.selectedHouse == null) && (googleVilleMap?.selectedSpotPosition == null)) {
            txt_house_name.text = "Choose a spot in the map first."
            return
        }

        if (houseEditMode && googleVilleMap?.selectedHouse != null) {
            houseDialogTextField?.setText(googleVilleMap?.selectedHouse?.name)
            houseDialogTextField?.selectAll()
        }
        slideUpAnimation?.start()
    }

    fun deleteHouseBtnClick(v: View) {
        if (houseEditMode && googleVilleMap?.selectedHouse != null) {
            launch(Dispatchers.Main) {
                val deletedHouse = serverComm?.deleteHouse(googleVilleMap?.selectedHouse as AVHouse)

                if (deletedHouse != null) {
                    houseDialogTextField?.setText("")
                    selectedHouseName?.text = ""
                    mAllHouses?.remove(googleVilleMap?.selectedHouse as AVHouse)
                    googleVilleMap?.deleteHouse((googleVilleMap?.selectedHouse as AVHouse).id)
                    googleVilleMap?.unSelectHouse()
                    setHouseEditMode(false)
                }
            }
        }
    }

    fun onDismissHouseDialogBtnClick(v: View?) {
        dismissSoftKeyboard()
        cancelDialogButton?.visibility = View.INVISIBLE
        slideDownAnimation?.start()
    }

    fun onAddEditHouseBtnClick(v: View) {
        val houseName = houseDialogTextField?.text.toString()
        if ("" != houseName) {
            if (googleVilleMap?.selectedHouse == null) {
                val houseId = nextHouseId++
                val newHouse = AVHouse(houseId, houseName, AVAddress(googleVilleMap?.selectedSpotPosition ?: LatLng(0.0, 0.0)), false, null)

                launch(Dispatchers.Main) {
                    val newHouse = serverComm?.addHouse(newHouse)

                    if (newHouse != null) {
                        dismissSoftKeyboard()
                        cancelDialogButton?.visibility = View.INVISIBLE
                        slideDownAnimation?.start()
                        houseDialogTextField?.setText("")
                        mAllHouses?.add(newHouse)
                        googleVilleMap?.addHouse(newHouse, true)
                        setHouseEditMode(googleVilleMap?.highlightHouse(newHouse.id) ?: false)
                        googleVilleMap?.unSelectSpot()
                        setHouseEditMode(true)
                    }
                }
            }
            else {
                val editHouse = googleVilleMap?.selectedHouse
                if (editHouse != null) {
                    editHouse.name = houseName

                    launch(Dispatchers.Main) {
                        val editedHouse = serverComm?.updateHouse(editHouse)

                        if (editedHouse != null) {
                            dismissSoftKeyboard()
                            slideDownAnimation?.start()
                            houseDialogTextField?.setText("")
                            googleVilleMap?.updateHouse(editHouse, editedHouse)
                        }
                    }
                }
            }
        }
    }

    fun showSoftKeyboard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
    }

    private fun dismissSoftKeyboard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun setHouseEditMode(editMode: Boolean) {
        houseEditMode = editMode

        if (houseEditMode) {
            addEditButton?.text = "Edit house"
            addEditDialogButton?.text = "Edit house"
            deleteButton?.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
        else {
            addEditButton?.text = "Add house"
            addEditDialogButton?.text = "Add house"
            deleteButton?.setTextColor(ContextCompat.getColor(this, R.color.softRed))
        }
        deleteButton?.isEnabled = houseEditMode
    }

    override fun onBackPressed() {
        if (cancelDialogButton?.visibility == View.VISIBLE) {
            onDismissHouseDialogBtnClick(null)
        }
        else {
            super.onBackPressed()
        }
    }

    private suspend fun retrieveMapData() {
        mAllHouses = mainViewModel?.allHouses ?: serverComm?.getAllHouses()

        houseToHighlight = mainViewModel.houseToHighlight

        mainViewModel.allHouses = mAllHouses
        googleVilleMap?.setHouses(mAllHouses)

        if (mainViewModel.selectedSpotLatitude != -1.0) {
            googleVilleMap?.setSelectedPosition(LatLng(mainViewModel.selectedSpotLatitude, mainViewModel.selectedSpotLongitude))
        }

        if (houseToHighlight != -1) {
            setHouseEditMode(googleVilleMap?.highlightHouse(houseToHighlight) ?: false)
            houseToHighlight = -1
        }

        val houseSize: Int = mAllHouses?.size ?: 0
        for (i in 0 until houseSize) {
            val house: AVHouse? = mAllHouses?.get(i)
            if (house?.id != null && nextHouseId <= house.id) {
                nextHouseId = house.id + 1
            }
        }
    }
}

interface HouseActions {
    fun setHouseEditMode(editMode: Boolean)
}
