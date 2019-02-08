package com.zapposandroid.rafaep.androidvillekotlin

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class HScroll(context: Context, attrs: AttributeSet?, defStyle: Int) : HorizontalScrollView(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    override fun onTouchEvent(ev: MotionEvent) = false

    override fun performClick() : Boolean = super.performClick()
}