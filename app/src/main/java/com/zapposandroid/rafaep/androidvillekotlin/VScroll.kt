package com.zapposandroid.rafaep.androidvillekotlin

import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class VScroll(context: Context, attrs: AttributeSet?, defStyle: Int) : ScrollView(context, attrs, defStyle) {
    private var mx: Float = 0F
    private var my: Float = 0F
    var hScroll: HorizontalScrollView? = null
    private var villeMap: VilleMap? = null
    private var mainActivity: MainActivity? = null

    init {
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    fun setMainActivity(mainActivity : MainActivity) {
        this.mainActivity = mainActivity
        hScroll = mainActivity.findViewById(R.id.hScroll)
        villeMap = mainActivity.villeMap
    }

    override fun performClick() : Boolean = super.performClick()

    override fun onTouchEvent(event: MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                villeMap?.isScrolling = false
                mx = event.x
                my = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                villeMap?.isScrolling = true
                val curX = event.x
                val curY = event.y
                this.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                hScroll?.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                mx = curX
                my = curY
            }
            MotionEvent.ACTION_UP -> {
                if (villeMap?.isScrolling == true) {
                    this.performClick()
                }
                villeMap?.isScrolling = false
                val curX = event.x
                val curY = event.y
                this.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                hScroll?.scrollBy((mx - curX).toInt(), (my - curY).toInt())
            }
        }

        return true
    }
}