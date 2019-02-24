package com.zapposandroid.rafaep.androidvillekotlin

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
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

class MainActivity : HouseActions, AppCompatActivity(), OnMapReadyCallback, CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var houseDialogTextField: EditText? = null
    private var googleVilleMap : GoogleVilleMap? = null

    private var dialogLayout: ConstraintLayout? = null
    private var nextHouseId = 1
    private var selectedHouseName: TextView? = null

    private var addEditButton: Button? = null
    private var deleteButton: Button? = null
    private var addEditDialogButton: Button? = null
    private var cancelDialogButton: Button? = null
    private var slideUpAnimation: AnimatorSet? = null
    private var slideDownAnimation: AnimatorSet? = null
    private var houseEditMode = false

    private var serverComm: ServerCommService? = null
    private var houseToHighlight = -1

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

    override fun onMapReady(googleMap: GoogleMap) {
        googleVilleMap = GoogleVilleMap(resources)
        googleVilleMap?.onMapReady(googleMap)
        googleVilleMap?.houseActions = this
        googleVilleMap?.txtHouseName = findViewById(R.id.txt_house_name)
        retrieveMapData()
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
                serverComm?.deleteHouse(googleVilleMap?.selectedHouse as AVHouse)
                houseDialogTextField?.setText("")
                selectedHouseName?.text = ""
                googleVilleMap?.unSelectHouse()
                setHouseEditMode(false)
                retrieveMapData()
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
                    System.out.println("ADDING A NEW HOUSE TO THE SERVER ====================")
                    serverComm?.addHouse(newHouse)

                    dismissSoftKeyboard()
                    cancelDialogButton?.visibility = View.INVISIBLE
                    slideDownAnimation?.start()
                    houseDialogTextField?.setText("")
                    retrieveMapData(houseId)
                    googleVilleMap?.unSelectSpot()
                    setHouseEditMode(true)
                }
            }
            else {
                val editHouse = googleVilleMap?.selectedHouse
                if (editHouse != null) {
                    editHouse.name = houseName

                    launch(Dispatchers.Main) {
                        serverComm?.updateHouse(editHouse)
                        dismissSoftKeyboard()
                        slideDownAnimation?.start()
                        houseDialogTextField?.setText("")
                        googleVilleMap?.unSelectHouse()
                        retrieveMapData(editHouse.id)
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
            deleteButton?.setTextColor(resources.getColor(R.color.red))
        }
        else {
            addEditButton?.text = "Add house"
            addEditDialogButton?.text = "Add house"
            deleteButton?.setTextColor(resources.getColor(R.color.softRed))
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

    private fun retrieveMapData(houseToHighlight: Int) {
        this.houseToHighlight = houseToHighlight
        retrieveMapData()
    }

    private fun retrieveMapData() {
        System.out.println("RETRIEVING MAP DATA!")//__RP
        launch(Dispatchers.Main) {
            System.out.println("BEFORE COMM")//__RP
            val houses = serverComm?.getAllHouses()
            System.out.println("AFTER COMM")//__RP
            googleVilleMap?.setHouses(houses)
            if (houseToHighlight != -1) {
                googleVilleMap?.highlightHouse(houseToHighlight)
                houseToHighlight = -1
            }

            val houseSize: Int = houses?.size ?: 0
            for (i in 0 until houseSize) {
                val house: AVHouse? = houses?.get(i)
                if (house?.id != null && nextHouseId <= house.id) {
                    nextHouseId = house.id + 1
                }
            }
        }
    }

}

interface HouseActions {
    fun setHouseEditMode(editMode: Boolean)
}
