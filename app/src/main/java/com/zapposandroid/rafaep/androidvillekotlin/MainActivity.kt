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
import com.pachesoft.androidville.R
import com.pachesoft.androidville.VScroll
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var mainApp: MainApp? = null
    private var houseDialogTextField: EditText? = null

    var vScroll: VScroll? = null
    var dialogLayout: ConstraintLayout? = null
    var nextHouseId = 1
    var selectedHouseName: TextView? = null

    private var villeMap: VilleMap? = null
    private var addEditButton: Button? = null
    private var deleteButton: Button? = null
    private var addEditDialogButton: Button? = null
    private var cancelDialogButton: Button? = null
    private var slideUpAnimation: AnimatorSet? = null
    private var slideDownAnimation: AnimatorSet? = null
    private var houseEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vScroll = findViewById(R.id.vScroll)
        villeMap = findViewById(R.id.mainVilleMap)
        dialogLayout = findViewById(R.id.ll_house_dialog)
        houseDialogTextField = findViewById(R.id.txt_input_house_name)
        addEditButton = findViewById(R.id.btn_add_house)
        deleteButton = findViewById(R.id.btn_delete_house)
        addEditDialogButton = findViewById(R.id.action_button)
        cancelDialogButton = findViewById(R.id.cancel_button)
        selectedHouseName = findViewById(R.id.txt_house_name)


        mainApp = application as MainApp
        mainApp?.setMainActivity(this)

        slideUpAnimation = AnimatorInflater.loadAnimator(this,
        R.animator.slide_up) as AnimatorSet
        slideUpAnimation?.addListener(Animator.AnimatorListener {
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
        slideDownAnimation?.addListener(Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }
            override fun onAnimationEnd(animator: Animator) {
                addEditButton?.setVisibility(View.VISIBLE)
                deleteButton?.setVisibility(View.VISIBLE)
            }
            override fun onAnimationCancel(animator: Animator) {
            }
            override fun void onAnimationRepeat(animator: Animator) {
            }
        })
        slideDownAnimation?.setTarget(dialogLayout)

        houseDialogTextField?.isFocusableInTouchMode = true

        setHouseEditMode(false)
    }

    override fun onStart() {
        super.onStart()
        mainApp?.getAllHouses()
    }

    fun getVilleMap() = villeMap

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
            mainApp?.serverComm?.deleteHouse(villeMap?.selectedHouse, Callback<AVHouse> {
                override fun onResponse(call: Call<AVHouse>, response: Response<AVHouse>) {
                    houseDialogTextField?.setText("")
                    selectedHouseName?.setText("")
                    villeMap?.selectedHouse = null
                    villeMap?.selectedSpotX = selectedSpotX
                    villeMap?.selectedSpotY = selectedSpotY
                    setHouseEditMode(false)
                    mainApp?.getAllHouses()
                }

                override onFailure(call: Call<AVHouse>, t: Throwable) {
                    selectedHouseName?.setText("** Operation failed **")
                }
            })
        }
    }

    fun onDismissHouseDialogBtnClick(v: View) {
        dismissSoftKeyboard()
        cancelDialogButton?.setVisibility(View.INVISIBLE)
        slideDownAnimation?.start()
    }

    fun onAddEditHouseBtnClick(v: View) {
        val houseName = houseDialogTextField?.text.toString()
        if ("" != houseName) {
            if (villeMap?.selectedHouse == null) {
                val houseId = nextHouseId++
                newHouse: AVHouse = AVHouse(houseId, houseName, new AVAddress(villeMap.selectedSpotX, villeMap.selectedSpotY))

                mainApp?.serverComm?.addHouse(newHouse, Callback<AVHouse> {
                    override fun onResponse(call: Call<AVHouse>, response: Response<AVHouse>) {
                        dismissSoftKeyboard()
                        cancelDialogButton?.setVisibility(View.INVISIBLE)
                        slideDownAnimation?.start()
                        houseDialogTextField?.setText("")
                        mainApp?.getAllHouses(houseId)
                        selectedHouseName?.text = houseName
                        villeMap?.selectedSpotX = -1
                        villeMap?.selectedSpotY = -1
                        setHouseEditMode(true)
                    }

                    override fun onFailure(call: Call<AVHouse>, t: Throwable) {
                        selectedHouseName?.setText("** Operation failed **")
                    }
                })
            }
            else {
                var editHouse = villeMap?.selectedHouse
                editHouse?.name = houseName

                mainApp?.serverComm?.updateHouse(editHouse, Callback<AVHouse> {
                    override fun onResponse(call: Call<AVHouse>, response: Response<AVHouse>) {
                        System.out.println(response?.message())
                        dismissSoftKeyboard()
                        slideDownAnimation?.start()
                        houseDialogTextField?.setText("")
                        selectedHouseName?.setText(houseName)
                    }

                    override fun onFailure(call: Call<AVHouse>, t: Throwable) {
                        selectedHouseName?.setText("** Operation failed **")
                    }
                })
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

    fun setHouseEditMode(editMode: Boolean) {
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
}
