package com.retrofit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * 使ViewPager不能滑动
 */
class NoTouchViewPager : ViewPager {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}