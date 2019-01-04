package com.retrofit.widget.pagerbottomtabstrip.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.ItemController
import com.retrofit.widget.pagerbottomtabstrip.item.BaseTabItem
import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener
import java.util.*

/**
 * 存放 Material Design 风格按钮的垂直布局
 */
class MaterialItemVerticalLayout : ViewGroup, ItemController {

    private val NAVIGATION_ITEM_SIZE: Int

    private var mItems: List<BaseTabItem>? = null
    private val mListeners = ArrayList<OnTabItemSelectedListener>()

    private var mSelected = -1

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        NAVIGATION_ITEM_SIZE = resources.getDimensionPixelSize(R.dimen.material_bottom_navigation_height)
    }

    fun initialize(items: List<BaseTabItem>) {
        mItems = items

        //添加按钮到布局，并注册点击事件
        val n = mItems!!.size
        for (i in 0 until n) {
            val v = mItems!![i]
            v.setChecked(false)
            this.addView(v)

            v.setOnClickListener { setSelect(i) }
        }

        //默认选中第一项
        mSelected = 0
        mItems!![0].setChecked(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val n = childCount

        val heightSpec = View.MeasureSpec.makeMeasureSpec(NAVIGATION_ITEM_SIZE, View.MeasureSpec.EXACTLY)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(NAVIGATION_ITEM_SIZE, View.MeasureSpec.EXACTLY)

        for (i in 0 until n) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            child.measure(widthSpec, heightSpec)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        //只支持top的padding
        var used = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }

            child.layout(0, used, child.measuredWidth, used + child.measuredHeight)

            used += child.measuredHeight
        }
    }

    override fun setSelect(index: Int) {

        //重复选择
        if (index == mSelected) {
            for (listener in mListeners) {
                listener.onRepeat(mSelected)
            }
            return
        }

        //记录前一个选中项和当前选中项
        val oldSelected = mSelected
        mSelected = index

        //前一个选中项必须不小于0才有效
        if (oldSelected >= 0) {
            mItems!![oldSelected].setChecked(false)
        }

        mItems!![mSelected].setChecked(true)

        //事件回调
        for (listener in mListeners) {
            listener.onSelected(mSelected, oldSelected)
        }
    }

    override fun setMessageNumber(index: Int, number: Int) {
        mItems!![index].setMessageNumber(number)
    }

    override fun setHasMessage(index: Int, hasMessage: Boolean) {
        mItems!![index].setHasMessage(hasMessage)
    }

    override fun addTabItemSelectedListener(listener: OnTabItemSelectedListener) {
        mListeners.add(listener)
    }

    override fun getSelected(): Int {
        return mSelected
    }

    override fun getItemCount(): Int {
        return mItems!!.size
    }

    override fun getItemTitle(index: Int): String {
        return mItems!![index].title
    }
}
