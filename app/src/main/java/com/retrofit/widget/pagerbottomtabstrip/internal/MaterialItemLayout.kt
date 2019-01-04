package com.retrofit.widget.pagerbottomtabstrip.internal


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.ItemController
import com.retrofit.widget.pagerbottomtabstrip.MaterialMode
import com.retrofit.widget.pagerbottomtabstrip.item.MaterialItemView
import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener
import java.util.*


@Suppress("NAME_SHADOWING")
/**
 * 存放 Material Design 风格按钮的水平布局
 */
class MaterialItemLayout : ViewGroup, ItemController {

    private val DEFAULT_SELECTED = 0

    private val MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH: Int
    private val MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH: Int
    private val MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH: Int
    private val MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT: Int

    private var mItems: List<MaterialItemView>? = null

    private val mListeners = ArrayList<OnTabItemSelectedListener>()

    private val mTempChildWidths: IntArray
    private var mItemTotalWidth: Int = 0

    private var mSelected = -1
    private var mOldSelected = -1

    private var mHideTitle: Boolean = false

    //切换背景颜色时使用
    private val ANIM_TIME = 300
    private var mInterpolator: Interpolator? = null
    private var mChangeBackgroundMode: Boolean = false
    private var mColors: List<Int>? = null
    private var mOvals: MutableList<Oval>? = null
    private var mTempRectF: RectF? = null
    private var mPaint: Paint? = null

    //最后手指抬起的坐标
    private var mLastUpX: Float = 0.toFloat()
    private var mLastUpY: Float = 0.toFloat()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val res = resources

        MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH = res.getDimensionPixelSize(R.dimen.material_bottom_navigation_active_item_max_width)
        MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH = res.getDimensionPixelSize(R.dimen.material_bottom_navigation_item_max_width)
        MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH = res.getDimensionPixelSize(R.dimen.material_bottom_navigation_item_min_width)
        MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT = res.getDimensionPixelSize(R.dimen.material_bottom_navigation_height)

        //材料设计规范限制最多只能有5个导航按钮
        mTempChildWidths = IntArray(5)
    }

    /**
     * 初始化方法
     *
     * @param items 按钮集合
     * @param mode  [MaterialMode]
     */
    fun initialize(items: List<MaterialItemView>, checkedColors: List<Int>, mode: Int) {
        mItems = items

        //判断是否需要切换背景
        if (mode and MaterialMode.CHANGE_BACKGROUND_COLOR > 0) {
            //初始化一些成员变量
            mChangeBackgroundMode = true
            mOvals = ArrayList()
            mColors = checkedColors
            mInterpolator = AccelerateDecelerateInterpolator()
            mTempRectF = RectF()
            mPaint = Paint()

            //设置默认的背景
            setBackgroundColor(mColors!![DEFAULT_SELECTED])

        } else {
            //设置按钮点击效果
            for (i in mItems!!.indices) {
                val v = mItems!![i]
                if (Build.VERSION.SDK_INT >= 21) {
                    v.background = RippleDrawable(ColorStateList(arrayOf(intArrayOf()), intArrayOf(0xFFFFFF and checkedColors[i] or 0x56000000)), null, null)
                } else {
                    v.setBackgroundResource(R.drawable.material_item_background)
                }
            }
        }

        //判断是否隐藏文字
        if (mode and MaterialMode.HIDE_TEXT > 0) {
            mHideTitle = true
            for (v in mItems!!) {
                v.setHideTitle(true)
            }
        }

        //添加按钮到布局，并注册点击事件
        val n = mItems!!.size
        for (i in 0 until n) {
            val v = mItems!![i]
            v.setChecked(false)
            this.addView(v)

            v.setOnClickListener { setSelect(i, mLastUpX, mLastUpY) }
        }

        //默认选中第一项
        mSelected = DEFAULT_SELECTED
        mItems!![DEFAULT_SELECTED].setChecked(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //排除空状态
        if (mItems == null || mItems!!.size <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val count = childCount

        val heightSpec = View.MeasureSpec.makeMeasureSpec(MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT, View.MeasureSpec.EXACTLY)

        if (mHideTitle) {
            val inactiveCount = count - 1
            val activeMaxAvailable = width - inactiveCount * MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH
            val activeWidth = Math.min(activeMaxAvailable, MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH)
            val inactiveMaxAvailable = (width - activeWidth) / inactiveCount
            val inactiveWidth = Math.min(inactiveMaxAvailable, MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH)
            for (i in 0 until count) {
                if (i == mSelected) {
                    mTempChildWidths[i] = ((activeWidth - inactiveWidth) * mItems!![mSelected].animValue + inactiveWidth).toInt()
                } else if (i == mOldSelected) {
                    mTempChildWidths[i] = (activeWidth - (activeWidth - inactiveWidth) * mItems!![mSelected].animValue).toInt()
                } else {
                    mTempChildWidths[i] = inactiveWidth
                }
            }
        } else {
            val maxAvailable = width / if (count == 0) 1 else count
            val childWidth = Math.min(maxAvailable, MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH)
            for (i in 0 until count) {
                mTempChildWidths[i] = childWidth
            }
        }

        mItemTotalWidth = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            child.measure(View.MeasureSpec.makeMeasureSpec(mTempChildWidths[i], View.MeasureSpec.EXACTLY),
                    heightSpec)
            val params = child.layoutParams
            params.width = child.measuredWidth
            mItemTotalWidth += child.measuredWidth
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val width = right - left
        val height = bottom - top
        //只支持top、bottom的padding
        val padding_top = paddingTop
        val padding_bottom = paddingBottom
        var used = 0

        if (mItemTotalWidth > 0 && mItemTotalWidth < width) {
            used = (width - mItemTotalWidth) / 2
        }

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                child.layout(width - used - child.measuredWidth, padding_top, width - used, height - padding_bottom)
            } else {
                child.layout(used, padding_top, child.measuredWidth + used, height - padding_bottom)
            }
            used += child.measuredWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mChangeBackgroundMode) {
            val width = width
            val height = height

            val iterator = mOvals!!.iterator()
            while (iterator.hasNext()) {
                val oval = iterator.next()
                mPaint!!.color = oval.color
                if (oval.r < oval.maxR) {
                    mTempRectF!!.set(oval.left, oval.top, oval.right, oval.bottom)
                    canvas.drawOval(mTempRectF!!, mPaint!!)
                } else {
                    this.setBackgroundColor(oval.color)
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint!!)
                    iterator.remove()
                }
                invalidate()
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        if (ev.action == MotionEvent.ACTION_UP) {
            mLastUpX = ev.x
            mLastUpY = ev.y
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun setSelect(index: Int) {
        //不正常的选择项
        if (index >= mItems!!.size || index < 0) {
            return
        }

        val v = mItems!![index]
        setSelect(index, v.x + v.width / 2f, v.y + v.height / 2f)
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

    private fun setSelect(index: Int, x: Float, y: Float) {

        //重复选择
        if (index == mSelected) {
            for (listener in mListeners) {
                listener.onRepeat(mSelected)
            }
            return
        }

        //记录前一个选中项和当前选中项
        mOldSelected = mSelected
        mSelected = index

        //切换背景颜色
        if (mChangeBackgroundMode) {
            addOvalColor(mColors!![mSelected], x, y)
        }

        //前一个选中项必须不小于0才有效
        if (mOldSelected >= 0) {
            mItems!![mOldSelected].setChecked(false)
        }

        mItems!![mSelected].setChecked(true)

        //事件回调
        for (listener in mListeners) {
            listener.onSelected(mSelected, mOldSelected)
        }
    }

    /**
     * 添加一个圆形波纹动画
     * @param color 颜色
     * @param x X座标
     * @param y y座标
     */
    private fun addOvalColor(color: Int, x: Float, y: Float) {
        val oval = Oval(color, 2f, x, y)

        oval.maxR = getR(x, y)
        mOvals!!.add(oval)

        val valueAnimator = ValueAnimator.ofFloat(oval.r, oval.maxR)
        valueAnimator.interpolator = mInterpolator
        valueAnimator.duration = ANIM_TIME.toLong()
        valueAnimator.addUpdateListener { valueAnimator -> oval.r = valueAnimator.animatedValue as Float }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                invalidate()
            }
        })
        valueAnimator.start()
    }

    /**
     * 以矩形内一点为圆心画圆，覆盖矩形，求这个圆的最小半径
     * @param x 横坐标
     * @param y 纵坐标
     * @return  最小半径
     */
    private fun getR(x: Float, y: Float): Float {
        val width = width
        val height = height

        val r1_square = (x * x + y * y).toDouble()
        val r2_square = ((width - x) * (width - x) + y * y).toDouble()
        val r3_square = ((width - x) * (width - x) + (height - y) * (height - y)).toDouble()
        val r4_square = (x * x + (height - y) * (height - y)).toDouble()

        return Math.sqrt(Math.max(Math.max(r1_square, r2_square), Math.max(r3_square, r4_square))).toFloat()
    }

    private inner class Oval internal constructor(internal var color: Int, internal var r: Float, internal var x: Float, internal var y: Float) {
        internal var maxR: Float = 0.toFloat()

        internal val left: Float
            get() = x - r

        internal val top: Float
            get() = y - r

        internal val right: Float
            get() = x + r

        internal val bottom: Float
            get() = y + r
    }
}
