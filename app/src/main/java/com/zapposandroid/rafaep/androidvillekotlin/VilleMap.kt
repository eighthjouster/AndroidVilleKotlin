package com.zapposandroid.rafaep.androidvillekotlin

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

class VilleMap(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var txtHouseName: TextView? = null
    var houseActions: HouseActions? = null


    private var mVilleName = ""

    private var mTextPaint: Paint? = null
    private var mGridPaint: Paint? = null

    private var houseBitmap: Bitmap? = null
    private var houseSelectedBitmap: Bitmap? = null
    private var spotSelectedBitmap: Bitmap? = null

    private var houseBitmapSize: RectF? = null

    var selectedHouse: AVHouse? = null

    private var houses: List<AVHouse>? = null

    var isScrolling: Boolean = false

    var selectedSpotX = -1
    var selectedSpotY= -1

    init {
        val myAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.VilleMap, 0, 0)

        try {
            mVilleName = myAttrs.getString(R.styleable.VilleMap_villeName)
        } finally {
            myAttrs.recycle()
        }

        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint?.textSize = 100.0f
        mTextPaint?.color = ContextCompat.getColor(context, R.color.white)

        mGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mGridPaint?.color = ContextCompat.getColor(context, R.color.yellow)
        mGridPaint?.strokeWidth = 2f

        houseBitmap = BitmapFactory.decodeResource(resources, R.drawable.house_icon)
        houseSelectedBitmap = BitmapFactory.decodeResource(resources, R.drawable.house_selected_icon)
        spotSelectedBitmap = BitmapFactory.decodeResource(resources, R.drawable.spot_selected_icon)

        houseBitmapSize = RectF(0f, 0f, 100f, 100f)

    }

    fun getVilleName() = mVilleName

    fun setVilleName(name: String) {
        mVilleName = name
        invalidate()
        requestLayout()
    }

    fun highlightHouse(houseId: Int) {
        var doInvalidate = false
        if (selectedHouse != null) {
            selectedHouse?.selected = false
            selectedHouse = null
            doInvalidate = true
        }

        val allHouses = houses.orEmpty()
        for (house in allHouses) {
          if (house.id == houseId) {
            selectedHouse = house
            selectedHouse?.selected = true
            txtHouseName?.text = house.name
            doInvalidate = true
        }
    }
        if (doInvalidate) {
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(2400,3600)
    }

    override fun performClick() = super.performClick()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventAction: Int = event.action
        when (eventAction) {
            MotionEvent.ACTION_DOWN -> {
                val x: Int = (event.x * 0.01f).toInt()
                val y: Int = (event.y * 0.01f).toInt()

                val countDownTimer: CountDownTimer = object : CountDownTimer(250, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (isScrolling) {
                            return
                        }
                        var houseSelected = false
                        txtHouseName?.text = ""

                        if (houses != null) {
                            if (selectedHouse != null) {
                                selectedHouse?.selected = false
                                selectedHouse = null
                            }
                            val allHouses = houses.orEmpty()
                            for (house in allHouses) {
                                if ((house.address.x == x) && (house.address.y == y)) {
                                    selectedHouse = house
                                    selectedHouse?.selected = true

                                    txtHouseName?.text = house.name

                                    houseSelected = true
                                }
                            }
                        }

                        if (houseSelected) {
                            if (selectedSpotX != -1) {
                                selectedSpotX = -1
                                selectedSpotY = -1
                            }
                        }
                        else {
                            if ((selectedSpotX == x) && (selectedSpotY == y)) {
                                selectedSpotX = -1
                                selectedSpotY = -1
                            }
                            else {
                                selectedSpotX = x
                                selectedSpotY = y
                            }
                        }

                        houseActions?.setHouseEditMode(houseSelected)

                        invalidate()
                    }
                }

                countDownTimer.start()

                this.performClick()
            }
        }

        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(ContextCompat.getColor(context, R.color.darkBlue))

        canvas.drawText(mVilleName, 0f, 100f, mTextPaint)

        for (i in 0..3600 step 100) {
            canvas.drawLine(0f, i.toFloat(), 2400f, i.toFloat(), mGridPaint)
        }

        for (i in 0..2400 step 100) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 3600f, mGridPaint)
        }

        if (houses != null) {
            val allHouses = houses.orEmpty()
            for (house in allHouses) {
                canvas.save()
                canvas.translate((house.address.x * 100).toFloat(),(house.address.y * 100).toFloat())
                canvas.drawBitmap(houseBitmap, null, houseBitmapSize, null)

                if (house.selected) {
                    canvas.drawBitmap(houseSelectedBitmap, null, houseBitmapSize, null)
                }

                canvas.restore()
            }
        }

        if (selectedSpotX != -1) {
            canvas.save()
            canvas.translate((selectedSpotX * 100).toFloat(),(selectedSpotY * 100).toFloat())
            canvas.drawBitmap(spotSelectedBitmap, null, houseBitmapSize, null)
            canvas.restore()
        }
    }

    fun setHouses(houses: List<AVHouse>?) {
        this.houses = houses
        this.invalidate()
    }
}
