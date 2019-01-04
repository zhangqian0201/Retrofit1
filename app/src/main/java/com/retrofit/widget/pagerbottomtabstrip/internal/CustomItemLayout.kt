package com.retrofit.widget.pagerbottomtabstrip.internal


import android.animation.LayoutTransition
import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.retrofit.widget.pagerbottomtabstrip.ItemController
import com.retrofit.widget.pagerbottomtabstrip.item.BaseTabItem
import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener
import java.util.*

/**
 * 存放自定义项的布局
 */
class CustomItemLayout : ViewGroup, ItemController {

    private lateinit var mItems: List<BaseTabItem>
    private val mListeners = ArrayList<OnTabItemSelectedListener>()

    private var mSelected = -1

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        layoutTransition = LayoutTransition()
    }

    fun initialize(items: List<BaseTabItem>) {
        mItems = items

        //添加按钮到布局，并注册点击事件
        val n = mItems.size
        for (i in 0 until n) {
            val v = mItems[i]
            v.setChecked(false)
            this.addView(v)

            v.setOnClickListener { setSelect(i) }
        }

        //默认选中第一项
        mSelected = 0
        mItems[0].setChecked(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val n = childCount
        val visibleChildCount = (0 until n).count { getChildAt(it).visibility != View.GONE }

        if (visibleChildCount == 0) {
            return
        }

        val childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(widthMeasureSpec) / visibleChildCount, View.MeasureSpec.EXACTLY)
        val childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(heightMeasureSpec) - paddingBottom - paddingTop), View.MeasureSpec.EXACTLY)

        (0 until n)
                .map { getChildAt(it) }
                .filter { it.visibility != View.GONE }
                .forEach { it.measure(childWidthMeasureSpec, childHeightMeasureSpec) }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val width = right - left
        val height = bottom - top
        //只支持top、bottom的padding
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        var used = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                child.layout(width - used - child.measuredWidth, paddingTop, width - used, height - paddingBottom)
            } else {
                child.layout(used, paddingTop, child.measuredWidth + used, height - paddingBottom)
            }
            used += child.measuredWidth
        }
    }

    override fun setSelect(index: Int) {

        //重复选择
        if (index == mSelected) {
            for (listener in mListeners) {
                mItems[mSelected].onRepeat()
                listener.onRepeat(mSelected)
            }
            return
        }

        //记录前一个选中项和当前选中项
        val oldSelected = mSelected
        mSelected = index

        //前一个选中项必须不小于0才有效
        if (oldSelected >= 0) {
            mItems[oldSelected].setChecked(false)
        }

        mItems[mSelected].setChecked(true)

        //事件回调
        for (listener in mListeners) {
            listener.onSelected(mSelected, oldSelected)
        }
    }

    override fun setMessageNumber(index: Int, number: Int) {
        mItems[index].setMessageNumber(number)
    }

    override fun setHasMessage(index: Int, hasMessage: Boolean) {
        mItems[index].setHasMessage(hasMessage)
    }

    override fun addTabItemSelectedListener(listener: OnTabItemSelectedListener) {
        mListeners.add(listener)
    }

    override fun getSelected(): Int {
        return mSelected
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun getItemTitle(index: Int): String {
        return mItems[index].title
    }
}