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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : HouseActions, AppCompatActivity() {
    private var houseDialogTextField: EditText? = null

    var vScroll: VScroll? = null
    var dialogLayout: ConstraintLayout? = null
    var nextHouseId = 1
    var selectedHouseName: TextView? = null

    var villeMap: VilleMap? = null
    private var addEditButton: Button? = null
    private var deleteButton: Button? = null
    private var addEditDialogButton: Button? = null
    private var cancelDialogButton: Button? = null
    private var slideUpAnimation: AnimatorSet? = null
    private var slideDownAnimation: AnimatorSet? = null
    private var houseEditMode = false

    var serverComm: ServerCommService? = null
    var houseToHighlight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverComm = ServerCommService()

        setContentView(R.layout.activity_main)

        vScroll = findViewById(R.id.vScroll)
        villeMap = findViewById(R.id.mainVilleMap)
        villeMap?.txtHouseName = findViewById(R.id.txt_house_name)
        villeMap?.houseActions = this
        dialogLayout = findViewById(R.id.ll_house_dialog)
        houseDialogTextField = findViewById(R.id.txt_input_house_name)
        addEditButton = findViewById(R.id.btn_add_house)
        deleteButton = findViewById(R.id.btn_delete_house)
        addEditDialogButton = findViewById(R.id.action_button)
        cancelDialogButton = findViewById(R.id.cancel_button)
        selectedHouseName = findViewById(R.id.txt_house_name)

        vScroll?.hScroll = findViewById(R.id.hScroll)
        vScroll?.villeMap = villeMap

        slideUpAnimation = AnimatorInflater.loadAnimator(this,
        R.animator.slide_up) as AnimatorSet
        slideUpAnimation?.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }
            override fun onAnimationEnd(animator: Animator) {
                addEditButton?.setVisibility(View.INVISIBLE)
                deleteButton?.setVisibility(View.INVISIBLE)
                cancelDialogButton?.setVisibility(View.VISIBLE)
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
                addEditButton?.setVisibility(View.VISIBLE)
                deleteButton?.setVisibility(View.VISIBLE)
            }
            override fun onAnimationCancel(animator: Animator) {
            }
            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        slideDownAnimation?.setTarget(dialogLayout)

        houseDialogTextField?.isFocusableInTouchMode = true

        setHouseEditMode(false)
    }

    override fun onStart() {
        super.onStart()
        retrieveMapData()
    }

    fun addEditHouseBtnClick(v: View) {
        if (houseEditMode && villeMap?.selectedHouse != null) {
            houseDialogTextField?.setText(villeMap?.selectedHouse?.name)
            houseDialogTextField?.selectAll()
        }
        slideUpAnimation?.start()
    }

    fun deleteHouseBtnClick(v: View) {
        if (houseEditMode && villeMap?.selectedHouse != null) {
            val selectedSpotX = villeMap?.selectedHouse?.address?.x
            val selectedSpotY = villeMap?.selectedHouse?.address?.y
            villeMap?.selectedSpotY = -1
            GlobalScope.launch(Dispatchers.Main) {
                serverComm?.deleteHouse(villeMap?.selectedHouse as AVHouse)
                houseDialogTextField?.setText("")
                selectedHouseName?.setText("")
                villeMap?.selectedHouse = null
                villeMap?.selectedSpotX = selectedSpotX ?: 0
                villeMap?.selectedSpotY = selectedSpotY ?: 0
                setHouseEditMode(false)
                retrieveMapData()

//__RP                override fun onFailure(call: Call<AVHouse>, t: Throwable) {
//                    selectedHouseName?.setText("** Operation failed **")
//                }

            }
        }
    }

    fun onDismissHouseDialogBtnClick(v: View?) {
        dismissSoftKeyboard()
        cancelDialogButton?.setVisibility(View.INVISIBLE)
        slideDownAnimation?.start()
    }

    fun onAddEditHouseBtnClick(v: View) {
        val houseName = houseDialogTextField?.text.toString()
        if ("" != houseName) {
            if (villeMap?.selectedHouse == null) {
                val houseId = nextHouseId++
                val newHouse = AVHouse(houseId, houseName, AVAddress(villeMap?.selectedSpotX ?: 0, villeMap?.selectedSpotY ?: 0), false)

                GlobalScope.launch(Dispatchers.Main) {
                    serverComm?.addHouse(newHouse)

                    dismissSoftKeyboard()
                    cancelDialogButton?.setVisibility(View.INVISIBLE)
                    slideDownAnimation?.start()
                    houseDialogTextField?.setText("")
                    retrieveMapData(houseId)
                    selectedHouseName?.text = houseName
                    villeMap?.selectedSpotX = -1
                    villeMap?.selectedSpotY = -1
                    setHouseEditMode(true)

//__RP                    override fun onFailure(call: Call<AVHouse>, t: Throwable) {
//                        selectedHouseName?.setText("** Operation failed **")
//                    }
                }
            }
            else {
                var editHouse = villeMap?.selectedHouse
                if (editHouse != null) {
                    editHouse?.name = houseName

                    GlobalScope.launch(Dispatchers.Main) {
                        val response = serverComm?.updateHouse(editHouse)
                            //System.out.println(response?.message()) //__RP
                            dismissSoftKeyboard()
                            slideDownAnimation?.start()
                            houseDialogTextField?.setText("")
                            selectedHouseName?.setText(houseName)

//__RP                        override fun onFailure(call: Call<AVHouse>, t: Throwable) {
//                            selectedHouseName?.setText("** Operation failed **")
//                        }
                    }
                }
            }
        }
    }

    fun showSoftKeyboard() {
        var imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
    }

    fun dismissSoftKeyboard() {
        var imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0)
    }

    override fun setHouseEditMode(editMode: Boolean) {
        houseEditMode = editMode

        if (houseEditMode) {
            addEditButton?.setText("Edit house")
            addEditDialogButton?.setText("Edit house")
            deleteButton?.setTextColor(getResources().getColor(R.color.red))
        }
        else {
            addEditButton?.setText("Add house")
            addEditDialogButton?.setText("Add house")
            deleteButton?.setTextColor(getResources().getColor(R.color.softRed))
        }
        deleteButton?.isEnabled = houseEditMode
    }

    override fun onBackPressed() {
        if (cancelDialogButton?.getVisibility() == View.VISIBLE) {
            onDismissHouseDialogBtnClick(null)
        }
        else {
            super.onBackPressed()
        }
    }

    fun retrieveMapData(houseToHighlight: Int) {
        this.houseToHighlight = houseToHighlight
        retrieveMapData()
    }

    fun retrieveMapData() {
        GlobalScope.launch(Dispatchers.Main) {
            val houses = serverComm?.getAllHouses()
            villeMap?.setHouses(houses)
            if (houseToHighlight != -1) {
                villeMap?.highlightHouse(houseToHighlight)
                houseToHighlight = -1
            }

            val houseSize: Int = houses?.size ?: 0
            for (i in 0 until houseSize) {
                val house: AVHouse? = houses?.get(i)
                if (house?.id != null && nextHouseId != null && nextHouseId as Int <= house?.id) {
                    nextHouseId = house?.id + 1
                }
            }
        }
    }

}

interface HouseActions {
    fun setHouseEditMode(editMode: Boolean)
}
