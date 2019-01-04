package com.retrofit.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.support.v4.view.NestedScrollingParentHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.Scroller
import android.widget.TextView
import com.retrofit.R

/**
 * SmartPullableLayout
 * 一个通用的，支持上下拉动并触发相应事件的ViewGroup。
 * 在使用时，应该通过 @setOnPullListener 来设置组件的滑动监听。
 *
 * onPullDown 会在组件被成功下拉时回调；
 * onPullUp 会在组件成功进行上拉时回调；
 * 可以在这里做SmartPullableLayout上、下拉动被成功触发时的事件处理。
 *
 * Created by zq on 2018/7/25
 */
@Suppress("DEPRECATION")
@SuppressLint("HandlerLeak")
class SmartPullableLayout : ViewGroup {
    private val DECELERATE_INTERPOLATION_FACTOR = 2f // 滑动阻力因子
    private val mNestedScrollingParentHelper: NestedScrollingParentHelper

    private var mChildScrollUpCallback: OnChildScrollUpCallback? = null // 自定义mTarget是否还能进行上拉的判断依据
    private var mChildScrollDownCallback: OnChildScrollDownCallback? = null // 自定义mTarget是否还能进行下拉的判断依据

    private var mEnabled = true // 当前是否允许视图滑动
    private var pullUpEnabled: Boolean = false  // 是否启用上拉功能
    private var pullDownEnabled: Boolean = false// 是否启用下拉功能

    private var currentState: Int = 0 // 视图当前状态
    private var mListener: OnPullListener? = null // 滑动回调监听

    private var mTarget: View? = null // 触发滑动手势的目标View
    private lateinit var mPullableHeader: View // 滑动头部
    private lateinit var mPullableFooter: View // 滑动尾部

    // 拉动部分背景(color|drawable)
    private var mBackground: Drawable? = null

    private lateinit var ivArrowPullDown: ImageView     // 下拉状态指示器(箭头)
    private lateinit var ivProgressPullDown: ImageView  // 下拉加载进度条(圆形)
    private lateinit var ivProgressPullUp: ImageView    // 上拉加载进度条(圆形)

    private lateinit var tvHintPullDown: TextView // 下拉状态文本指示
    private lateinit var tvHintPullUp: TextView   // 上拉状态文本指示

    private var upProgressAnimation: AnimationDrawable? = null   // 上拉加载进度条帧动画
    private var downProgressAnimation: AnimationDrawable? = null // 下拉加载进度条帧动画

    private val mLayoutScroller: Scroller  // 用于平滑滑动的Scroller对象
    private val effectivePullRange: Int    // 使拉动回调生效(触发)的滑动距离
    private val ignorablePullRange: Int    // 可以忽略的拉动距离(超过此滑动距离，将不再允许parent view拦截触摸事件)

    private val STOP_PULL = 0X0502
    private val LOAD_OVER = 0X0501

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                STOP_PULL -> updateState(State.NORMAL)
                LOAD_OVER -> updateState(State.LOAD_OVER)
            }
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SmartPullableLayout)
        try {
            pullDownEnabled = array.getBoolean(R.styleable.SmartPullableLayout_smart_ui_enable_pull_down, true)
            pullUpEnabled = array.getBoolean(R.styleable.SmartPullableLayout_smart_ui_enable_pull_up, true)
            mBackground = array.getDrawable(R.styleable.SmartPullableLayout_smart_ui_background)
        } finally {
            array.recycle()
        }

        // NestedScrollingParentHelper
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)

        mLayoutScroller = Scroller(context)

        effectivePullRange = resources.getDimension(R.dimen.padding_dp_65).toInt()
        ignorablePullRange = resources.getDimension(R.dimen.padding_dp_5).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        preparePullablePortion()
    }

    @SuppressLint("InflateParams")
    private fun preparePullablePortion() {
        if (pullDownEnabled) {
            mPullableHeader = LayoutInflater.from(context).inflate(R.layout.smart_ui_pullable_layout_header, null)
            if (mBackground != null) {
                mPullableHeader.setBackgroundDrawable(mBackground)
            }
            ivArrowPullDown = mPullableHeader.findViewById(R.id.smart_ui_iv_pull_down_arrow)
            ivProgressPullDown = mPullableHeader.findViewById(R.id.smart_ui_iv_pull_down_loading)
            downProgressAnimation = ivProgressPullDown.background as AnimationDrawable
            tvHintPullDown = mPullableHeader.findViewById(R.id.smart_ui_tv_pull_down_des)
            this.addView(mPullableHeader, 0)
        }

        if (pullUpEnabled) {
            mPullableFooter = LayoutInflater.from(context).inflate(R.layout.smart_ui_pullable_layout_footer, null)
            if (mBackground != null) {
                mPullableFooter.setBackgroundDrawable(mBackground)
            }
            ivProgressPullUp = mPullableFooter.findViewById(R.id.smart_ui_iv_pull_up_loading)
            upProgressAnimation = ivProgressPullUp.background as AnimationDrawable
            tvHintPullUp = mPullableFooter.findViewById(R.id.smart_ui_tv_pull_up_des)
            this.addView(mPullableFooter, childCount)
        }
    }

    private fun rotateArrow() {
        val offset = (ivArrowPullDown.rotation + 180) % 180
        val arrowAnimator = ObjectAnimator.ofFloat(ivArrowPullDown, "rotation",
                ivArrowPullDown.rotation, ivArrowPullDown.rotation + 180 - offset)
        arrowAnimator.duration = 135
        arrowAnimator.start()
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.mEnabled = enabled
    }

    fun setOnPullListener(listener: OnPullListener) {
        this.mListener = listener
    }

    fun setOnChildScrollUpCallback(childScrollUpCallback: OnChildScrollUpCallback) {
        this.mChildScrollUpCallback = childScrollUpCallback
    }

    fun setOnChildScrollDownCallback(childScrollDownCallback: OnChildScrollDownCallback) {
        this.mChildScrollDownCallback = childScrollDownCallback
    }

    private fun ensureTarget() {
        if (mTarget == null) {
            mTarget = if (pullDownEnabled) {
                getChildAt(1)
            } else {
                getChildAt(0)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        ensureTarget()
        if (mTarget == null) {
            return
        }

        for (i in 0 until childCount) {
            getChildAt(i).measure(
                    View.MeasureSpec.makeMeasureSpec(
                            measuredWidth - paddingLeft - paddingRight,
                            View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(
                            measuredHeight - paddingTop - paddingBottom,
                            View.MeasureSpec.EXACTLY))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    when {
                        it === mPullableHeader -> // 头视图隐藏在顶端
                            it.layout(0, 0 - it.measuredHeight, it.measuredWidth, 0)
                        it === mPullableFooter -> // 尾视图隐藏在末端
                            mTarget?.apply {
                                it.layout(0, measuredHeight, it.measuredWidth, measuredHeight + it.measuredHeight)
                            }
                        else -> it.layout(0, 0, it.measuredWidth, it.measuredHeight)
                    }
                }
    }

    private var mLastMoveY: Int = 0

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var intercept = false
        val y = event.y.toInt()
        // 判断触摸事件类型
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                intercept = false
            }
            MotionEvent.ACTION_MOVE -> {
                mTarget?.let {
                    if (!ViewCompat.isNestedScrollingEnabled(it)) {
                        if (y > mLastMoveY) { // 下滑操作
                            intercept = !canChildScrollUp()
                        } else if (y < mLastMoveY) { // 上拉操作
                            intercept = !canChildScrollDown()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                intercept = false
            }
        }
        mLastMoveY = y
        return intercept
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(scrollY) > ignorablePullRange) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (mEnabled) {
                    doScroll(mLastMoveY - y)
                }
            }
            MotionEvent.ACTION_UP -> {
                onStopScroll()
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        mLastMoveY = y

        return true
    }

    private fun doScroll(y: Int) {
        var dy = y
        if (dy > 0) { // 上拉操作
            if (scrollY < 0) { // 此判断意味是在进行下拉操作的过程中，进行的上拉操作(可能代表用户视图取消此次下拉)
                if (Math.abs(scrollY) < effectivePullRange) {
                    if (currentState != State.PULL_DOWN) {
                        updateState(State.PULL_DOWN)
                    }

                    if (dy > Math.abs(scrollY)) {
                        dy = -scrollY
                    }
                }
            } else {
                if (!pullUpEnabled) {
                    return
                }
                if (currentState < State.PULL_UP && currentState != State.NORMAL) {
                    return
                }

                if (scrollY > effectivePullRange) { // 当下拉已经达到有效距离，则为滑动添加阻力
                    dy /= DECELERATE_INTERPOLATION_FACTOR.toInt()
                    if (currentState != State.PULL_UP_RELEASEABLE) {
                        updateState(State.PULL_UP_RELEASEABLE)
                    }
                }
            }
        } else if (dy < 0) { // 下拉操作
            if (scrollY > 0) {  // 此判断意味是在进行上拉操作的过程中，进行的下拉操作(可能代表用户视图取消此次上拉)
                if (scrollY < effectivePullRange) {
                    if (currentState != State.PULL_UP) {
                        updateState(State.PULL_UP)
                    }

                    if (Math.abs(dy) > scrollY) {
                        dy = -scrollY
                    }
                }
            } else {
                if (!pullDownEnabled) {
                    return
                }
                if (currentState > State.PULL_DOWN_RELEASEABLE) {
                    return
                }

                if (Math.abs(scrollY) >= effectivePullRange) { // 当下拉已经达到有效距离，则为滑动添加阻力
                    dy /= DECELERATE_INTERPOLATION_FACTOR.toInt()
                    if (currentState != State.PULL_DOWN_RELEASEABLE) {
                        updateState(State.PULL_DOWN_RELEASEABLE)
                    }
                }
            }
        }

        dy /= DECELERATE_INTERPOLATION_FACTOR.toInt()
        scrollBy(0, dy)
    }

    private fun onStopScroll() {
        if (Math.abs(scrollY) >= effectivePullRange) { // 有效拉动行为
            if (scrollY < 0) { // 有效下拉行为
                mLayoutScroller.startScroll(0, scrollY, 0, -(scrollY + effectivePullRange))
                updateState(State.PULL_DOWN_RELEASE)
            } else if (scrollY > 0) { // 有效上拉行为
                updateState(State.PULL_UP_RELEASE)
                mLayoutScroller.startScroll(0, scrollY, 0, -(scrollY - effectivePullRange))
            }
        } else { // 无效拉动行为
            updateState(State.NORMAL)
        }
    }

    private fun updateState(state: Int) {
        when (state) {
            State.NORMAL -> reset()
            State.PULL_DOWN -> {
                if (currentState != State.NORMAL) {
                    rotateArrow()
                }

                tvHintPullDown.setText(R.string.smart_ui_pull_down_normal)
            }
            State.PULL_DOWN_RELEASEABLE -> {
                rotateArrow()
                tvHintPullDown.setText(R.string.smart_ui_pull_down_release_able)
            }
            State.PULL_DOWN_RELEASE -> {
                isEnabled = false

                ivArrowPullDown.visibility = View.INVISIBLE

                ivProgressPullDown.visibility = View.VISIBLE
                downProgressAnimation?.start()

                tvHintPullDown.setText(R.string.smart_ui_pull_down_release)

                mListener?.onPullDown()
            }
            State.PULL_UP -> tvHintPullUp.setText(R.string.smart_ui_pull_up_normal)
            State.PULL_UP_RELEASEABLE -> tvHintPullUp.setText(R.string.smart_ui_pull_up_release_able)
            State.PULL_UP_RELEASE -> {
                isEnabled = false

                ivProgressPullUp.visibility = View.VISIBLE
                upProgressAnimation?.start()
                tvHintPullUp.setText(R.string.smart_ui_pull_up_release)

                mListener?.onPullUp()
            }
            State.LOAD_OVER -> {
                isEnabled = false
                ivProgressPullUp.visibility = View.GONE
                tvHintPullUp.setText(R.string.smart_ui_pull_up_no_more)
                mHandler.postDelayed({ updateState(State.NORMAL) }, 1000)
            }
        }

        currentState = state
    }

    private fun reset() {
        if (scrollY != 0) {
            mLayoutScroller.startScroll(0, scrollY, 0, -scrollY)
        }
        if (currentState != State.NORMAL) {
            if (currentState <= State.PULL_DOWN_RELEASE) {
                downProgressAnimation?.stop()
                ivProgressPullDown.visibility = View.INVISIBLE
                ivArrowPullDown.visibility = View.VISIBLE
                ivArrowPullDown.rotation = 0f
                tvHintPullDown.setText(R.string.smart_ui_pull_down_normal)
            } else {
                upProgressAnimation?.stop()
                ivProgressPullUp.visibility = View.GONE
                tvHintPullUp.setText(R.string.smart_ui_pull_up_normal)
            }
        }

        isEnabled = true
        if (scrollY != 0) {
            mLayoutScroller.startScroll(0, scrollY, 0, -scrollY)
        }
    }

    fun stopPullBehavior() {
        mHandler.sendEmptyMessage(STOP_PULL)
    }

    fun loadOver() {
        mHandler.sendEmptyMessage(LOAD_OVER)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mLayoutScroller.computeScrollOffset()) {
            scrollTo(0, mLayoutScroller.currY)
        }
        postInvalidate()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollUp(): Boolean {
        mChildScrollUpCallback?.apply {
            return canChildScrollUp(this@SmartPullableLayout, mTarget!!)
        }
        return if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget is AbsListView) {
                val absListView = mTarget as AbsListView
                absListView.childCount > 0 && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                        .top < absListView.paddingTop)
            } else {
                ViewCompat.canScrollVertically(mTarget, -1) || mTarget!!.scrollY > 0
            }
        } else {
            ViewCompat.canScrollVertically(mTarget, -1)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollDown(): Boolean {
        mChildScrollDownCallback?.apply {
            return canChildScrollDown(this@SmartPullableLayout, mTarget!!)
        }

        return if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget is AbsListView) {
                val absListView = mTarget as AbsListView
                absListView.childCount > 0 && (absListView.lastVisiblePosition != absListView.count - 1 || absListView.getChildAt(absListView.childCount - 1).bottom > absListView.measuredHeight)
            } else {
                if (mTarget is ViewGroup) {
                    ViewCompat.canScrollVertically(mTarget, 1) || mTarget!!.scrollY < (mTarget as ViewGroup).getChildAt(0).measuredHeight - mTarget!!.measuredHeight
                } else ViewCompat.canScrollVertically(mTarget, 1) || mTarget!!.scrollY < mTarget!!.measuredHeight - measuredHeight

            }
        } else {
            ViewCompat.canScrollVertically(mTarget, 1)
        }
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        /*
         * 重写此方法的目的是:
         * 如果判断目标视图是一个版本低于Android-L的AbsListView，或另一个不支持嵌套滚动的视图。
         * 则请忽略此请求，以便垂直滚动事件不会被目标视图窃取(本视图无法再监听拦截到任何touchEvent)
         */
        if (android.os.Build.VERSION.SDK_INT < 21 && mTarget is AbsListView || mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget!!)) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    // NestedScrollingParent

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return isEnabled && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (scrollY != 0) { // 只有在SmartPullableLayout自身已经发生过位移的情况才处理预消耗
            if (dy > 0 && scrollY < 0 && Math.abs(dy) >= Math.abs(scrollY)) {
                consumed[1] = scrollY
                scrollTo(0, 0)
                return
            }

            if (dy < 0 && scrollY > 0 && Math.abs(dy) >= Math.abs(scrollY)) {
                consumed[1] = scrollY
                scrollTo(0, 0)
                return
            }

            val yConsume = if (Math.abs(dy) > Math.abs(scrollY)) scrollY else dy
            doScroll(yConsume)
            consumed[1] = yConsume
        }
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(target: View) {
        onStopScroll()
        mNestedScrollingParentHelper.onStopNestedScroll(target)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int) {
        if (mEnabled) {
            doScroll(dyUnconsumed)
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false
    }

    /**
     * 控件状态
     */
    interface State {
        companion object {
            val NORMAL = 0    // 正常状态
            val PULL_DOWN = 1 // 下拉中
            val PULL_DOWN_RELEASEABLE = 2 // 可释放下拉
            val PULL_DOWN_RELEASE = 3     // 已释放下拉
            val PULL_UP = 4    // 上拉中
            val PULL_UP_RELEASEABLE = 5  // 可释放上拉
            val PULL_UP_RELEASE = 6     // 已释放上拉
            val LOAD_OVER = 7     // 无更多数据
        }
    }

    interface OnPullListener {
        fun onPullDown()

        fun onPullUp()
    }

    interface OnChildScrollUpCallback {
        fun canChildScrollUp(parent: SmartPullableLayout, child: View): Boolean
    }

    interface OnChildScrollDownCallback {
        fun canChildScrollDown(parent: SmartPullableLayout, child: View): Boolean
    }
}