package com.retrofit.widget.pagerbottomtabstrip

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.internal.*
import com.retrofit.widget.pagerbottomtabstrip.item.BaseTabItem
import com.retrofit.widget.pagerbottomtabstrip.item.MaterialItemView
import com.retrofit.widget.pagerbottomtabstrip.item.OnlyIconMaterialItemView
import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener
import java.util.*

/**
 * Created by zq on 2018/6/11
 */
class PageNavigationView : ViewGroup {
    private var mTabPaddingTop: Int = 0
    private var mTabPaddingBottom: Int = 0

    private val INSTANCE_STATUS = "INSTANCE_STATUS"
    private val STATUS_SELECTED = "STATUS_SELECTED"

    private var mNavigationController: NavigationController? = null

    private var mPageChangeListener: ViewPagerPageChangeListener? = null
    private var mViewPager: ViewPager? = null

    private var mEnableVerticalLayout: Boolean = false

    private val mTabItemListener = object : OnTabItemSelectedListener {
        override fun onSelected(index: Int, old: Int) {
            mViewPager?.setCurrentItem(index, false)
        }

        override fun onRepeat(index: Int) {}
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setPadding(0, 0, 0, 0)

        val a = context.obtainStyledAttributes(attrs, R.styleable.PageNavigationView)
        if (a.hasValue(R.styleable.PageNavigationView_NavigationPaddingTop)) {
            mTabPaddingTop = a.getDimensionPixelSize(R.styleable.PageNavigationView_NavigationPaddingTop, 0)
        }
        if (a.hasValue(R.styleable.PageNavigationView_NavigationPaddingBottom)) {
            mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.PageNavigationView_NavigationPaddingBottom, 0)
        }
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val count = childCount

        var maxWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        var maxHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            maxWidth = Math.max(maxWidth, child.measuredWidth)
            maxHeight = Math.max(maxHeight, child.measuredHeight)
        }

        setMeasuredDimension(maxWidth, maxHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        val height = b - t

        (0 until count)
                .map { getChildAt(it) }
                .filter { it.visibility != View.GONE }
                .forEach { it.layout(0, 0, width, height) }
    }

    /**
     * 构建 Material Desgin 风格的导航栏
     */
    fun material(): MaterialBuilder {
        return MaterialBuilder()
    }

    /**
     * 构建自定义导航栏
     */
    fun custom(): CustomBuilder {
        return CustomBuilder()
    }

    /**
     * 构建 自定义 的导航栏
     */
    inner class CustomBuilder internal constructor() {
        private var items: MutableList<BaseTabItem> = ArrayList<BaseTabItem>()

        private var enableVerticalLayout: Boolean = false

        /**
         * 完成构建
         * @return  [NavigationController],通过它进行后续操作
         */
        fun build(): NavigationController? {

            mEnableVerticalLayout = enableVerticalLayout

            //未添加任何按钮
            if (items.size == 0) {
                return null
            }

            val itemController: ItemController

            if (enableVerticalLayout) {//垂直布局
                val verticalItemLayout = CustomItemVerticalLayout(context)
                verticalItemLayout.initialize(items)
                verticalItemLayout.setPadding(0, mTabPaddingTop, 0, mTabPaddingBottom)

                this@PageNavigationView.removeAllViews()
                this@PageNavigationView.addView(verticalItemLayout)
                itemController = verticalItemLayout
            } else {//水平布局
                val customItemLayout = CustomItemLayout(context)
                customItemLayout.initialize(items)
                customItemLayout.setPadding(0, mTabPaddingTop, 0, mTabPaddingBottom)

                this@PageNavigationView.removeAllViews()
                this@PageNavigationView.addView(customItemLayout)
                itemController = customItemLayout
            }

            mNavigationController = NavigationController(Controller(), itemController)
            mNavigationController?.addTabItemSelectedListener(mTabItemListener)

            return mNavigationController
        }

        /**
         * 添加一个导航按钮
         * @param baseTabItem   [BaseTabItem],所有自定义Item都必须继承它
         * @return [CustomBuilder]
         */
        fun addItem(baseTabItem: BaseTabItem): CustomBuilder {
            items.add(baseTabItem)
            return this@CustomBuilder
        }

        /**
         * 使用垂直布局
         */
        fun enableVerticalLayout(): CustomBuilder {
            enableVerticalLayout = true
            return this@CustomBuilder
        }
    }

    /**
     * 构建 Material Desgin 风格的导航栏
     */
    inner class MaterialBuilder internal constructor() {
        private var itemDatas: MutableList<ViewData> = ArrayList()
        private var defaultColor: Int = 0
        private var mode: Int = 0
        private var messageBackgroundColor: Int = 0
        private var messageNumberColor: Int = 0
        private var enableVerticalLayout: Boolean = false

        /**
         * 完成构建
         *
         * @return  [NavigationController],通过它进行后续操作
         */
        fun build(): NavigationController? {
            mEnableVerticalLayout = enableVerticalLayout

            // 未添加任何按钮
            if (itemDatas.size == 0) {
                return null
            }

            // 设置默认颜色
            if (defaultColor == 0) {
                defaultColor = 0x56000000
            }

            val itemController: ItemController

            if (enableVerticalLayout) {//垂直布局

                val items = ArrayList<BaseTabItem>()

                for (data in itemDatas) {

                    val materialItemView = OnlyIconMaterialItemView(context)
                    materialItemView.initialization(data.title!!, data.drawable!!, data.checkedDrawable!!, defaultColor, data.chekedColor)

                    //检查是否设置了消息圆点的颜色
                    if (messageBackgroundColor != 0) {
                        materialItemView.setMessageBackgroundColor(messageBackgroundColor)
                    }

                    //检查是否设置了消息数字的颜色
                    if (messageNumberColor != 0) {
                        materialItemView.setMessageNumberColor(messageNumberColor)
                    }

                    items.add(materialItemView)
                }

                val materialItemVerticalLayout = MaterialItemVerticalLayout(context)
                materialItemVerticalLayout.initialize(items)
                materialItemVerticalLayout.setPadding(0, mTabPaddingTop, 0, mTabPaddingBottom)

                this@PageNavigationView.removeAllViews()
                this@PageNavigationView.addView(materialItemVerticalLayout)

                itemController = materialItemVerticalLayout

            } else {//水平布局

                val changeBackground = mode and MaterialMode.CHANGE_BACKGROUND_COLOR > 0

                val items = ArrayList<MaterialItemView>()
                val checkedColors = ArrayList<Int>()

                for (data in itemDatas) {
                    // 记录设置的选中颜色
                    checkedColors.add(data.chekedColor)

                    val materialItemView = MaterialItemView(context)
                    // 需要切换背景颜色就默认将选中颜色改成白色
                    if (changeBackground) {
                        materialItemView.initialization(data.title!!, data.drawable!!, data.checkedDrawable!!, defaultColor, Color.WHITE)
                    } else {
                        materialItemView.initialization(data.title!!, data.drawable!!, data.checkedDrawable!!, defaultColor, data.chekedColor)
                    }

                    //检查是否设置了消息圆点的颜色
                    if (messageBackgroundColor != 0) {
                        materialItemView.setMessageBackgroundColor(messageBackgroundColor)
                    }

                    //检查是否设置了消息数字的颜色
                    if (messageNumberColor != 0) {
                        materialItemView.setMessageNumberColor(messageNumberColor)
                    }

                    items.add(materialItemView)
                }

                val materialItemLayout = MaterialItemLayout(context)
                materialItemLayout.initialize(items, checkedColors, mode)
                materialItemLayout.setPadding(0, mTabPaddingTop, 0, mTabPaddingBottom)

                this@PageNavigationView.removeAllViews()
                this@PageNavigationView.addView(materialItemLayout)

                itemController = materialItemLayout
            }

            mNavigationController = NavigationController(Controller(), itemController)
            mNavigationController?.addTabItemSelectedListener(mTabItemListener)

            return mNavigationController
        }

        /**
         * 添加一个导航按钮
         * @param drawableRes  图标资源
         * @param title        显示文字内容.尽量简短
         * @return [MaterialBuilder]
         */
        fun addItem(@DrawableRes drawableRes: Int, title: String): MaterialBuilder {
            addItem(drawableRes, drawableRes, title, Utils.getColorPrimary(context))
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawableRes          图标资源
         * @param checkedDrawableRes   选中时的图标资源
         * @param title                显示文字内容.尽量简短
         * @return  [MaterialBuilder]
         */
        fun addItem(@DrawableRes drawableRes: Int, @DrawableRes checkedDrawableRes: Int, title: String): MaterialBuilder {
            addItem(drawableRes, checkedDrawableRes, title, Utils.getColorPrimary(context))
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawableRes   图标资源
         * @param title         显示文字内容.尽量简短
         * @param chekedColor   选中的颜色
         * @return  [MaterialBuilder]
         */
        fun addItem(@DrawableRes drawableRes: Int, title: String, @ColorInt chekedColor: Int): MaterialBuilder {
            addItem(drawableRes, drawableRes, title, chekedColor)
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawableRes           图标资源
         * @param checkedDrawableRes    选中时的图标资源
         * @param title                 显示文字内容.尽量简短
         * @param chekedColor           选中的颜色
         * @return  [MaterialBuilder]
         */
        fun addItem(@DrawableRes drawableRes: Int, @DrawableRes checkedDrawableRes: Int, title: String, @ColorInt chekedColor: Int): MaterialBuilder {
            addItem(ContextCompat.getDrawable(context, drawableRes)!!, ContextCompat.getDrawable(context, checkedDrawableRes)!!, title, chekedColor)
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawable  图标资源
         * @param title     显示文字内容.尽量简短
         * @return [MaterialBuilder]
         */
        fun addItem(drawable: Drawable, title: String): MaterialBuilder {
            addItem(drawable, Utils.newDrawable(drawable), title, Utils.getColorPrimary(context))
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawable          图标资源
         * @param checkedDrawable   选中时的图标资源
         * @param title             显示文字内容.尽量简短
         * @return  [MaterialBuilder]
         */
        fun addItem(drawable: Drawable, checkedDrawable: Drawable, title: String): MaterialBuilder {
            addItem(drawable, checkedDrawable, title, Utils.getColorPrimary(context))
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         * @param drawable      图标资源
         * @param title         显示文字内容.尽量简短
         * @param chekedColor   选中的颜色
         * @return  [MaterialBuilder]
         */
        fun addItem(drawable: Drawable, title: String, @ColorInt chekedColor: Int): MaterialBuilder {
            addItem(drawable, Utils.newDrawable(drawable), title, chekedColor)
            return this@MaterialBuilder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawable          图标资源
         * @param checkedDrawable   选中时的图标资源
         * @param title             显示文字内容.尽量简短
         * @param chekedColor       选中的颜色
         * @return  [MaterialBuilder]
         */
        fun addItem(drawable: Drawable, checkedDrawable: Drawable, title: String, @ColorInt chekedColor: Int): MaterialBuilder {
            val data = ViewData()
            data.drawable = drawable
            data.checkedDrawable = checkedDrawable
            data.title = title
            data.chekedColor = chekedColor
            itemDatas.add(data)
            return this@MaterialBuilder
        }

        /**
         * 设置导航按钮的默认（未选中状态）颜色
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return  [MaterialBuilder]
         */
        fun setDefaultColor(@ColorInt color: Int): MaterialBuilder {
            defaultColor = color
            return this@MaterialBuilder
        }

        /**
         * 设置消息圆点的颜色
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return  [MaterialBuilder]
         */
        fun setMessageBackgroundColor(@ColorInt color: Int): MaterialBuilder {
            messageBackgroundColor = color
            return this@MaterialBuilder
        }

        /**
         * 设置消息数字的颜色
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return  [MaterialBuilder]
         */
        fun setMessageNumberColor(@ColorInt color: Int): MaterialBuilder {
            messageNumberColor = color
            return this@MaterialBuilder
        }

        /**
         * 设置模式(在垂直布局中无效)。默认文字一直显示，且背景色不变。
         * 可以通过[MaterialMode]选择模式。
         *
         *
         * 例如:
         * `MaterialMode.HIDE_TEXT`
         *
         *
         * 或者多选:
         * `MaterialMode.HIDE_TEXT | MaterialMode.CHANGE_BACKGROUND_COLOR`
         *
         * @param mode [MaterialMode]
         * @return [MaterialBuilder]
         */
        fun setMode(mode: Int): MaterialBuilder {
            this@MaterialBuilder.mode = mode
            return this@MaterialBuilder
        }

        /**
         * 使用垂直布局
         */
        fun enableVerticalLayout(): MaterialBuilder {
            enableVerticalLayout = true
            return this@MaterialBuilder
        }

        inner class ViewData {
            internal var drawable: Drawable? = null
            internal var checkedDrawable: Drawable? = null
            internal var title: String? = null
            @ColorInt
            internal var chekedColor: Int = 0
        }
    }

    /**
     * 实现控制接口
     */
    private inner class Controller : BottomLayoutController {

        private var animator: ObjectAnimator? = null
        private var hide = false

        override fun setupWithViewPager(viewPager: ViewPager) {
            mViewPager = viewPager

            if (mPageChangeListener != null) {
                mPageChangeListener?.let { mViewPager?.removeOnPageChangeListener(it) }
            } else {
                mPageChangeListener = ViewPagerPageChangeListener()
            }

            mViewPager?.let { pager ->
                val n = pager.currentItem
                mNavigationController?.apply {
                    if (getSelected() != n) {
                        setSelect(n)
                    }
                }
                mPageChangeListener?.let { pager.addOnPageChangeListener(it) }

            }
        }

        override fun hideBottomLayout() {
            if (!hide) {
                hide = true
                getAnimator().start()
            }
        }

        override fun showBottomLayout() {
            if (hide) {
                hide = false
                getAnimator().reverse()
            }
        }

        private fun getAnimator(): ObjectAnimator {

            if (animator == null) {
                animator = if (mEnableVerticalLayout) {//垂直布局向左隐藏
                    ObjectAnimator.ofFloat(
                            this@PageNavigationView, "translationX", 0.toFloat(), (-this@PageNavigationView.width).toFloat())
                } else {//水平布局向下隐藏
                    ObjectAnimator.ofFloat(
                            this@PageNavigationView, "translationY", 0.toFloat(), (this@PageNavigationView.height).toFloat())
                }

                animator!!.duration = 300
                animator!!.interpolator = AccelerateDecelerateInterpolator()
            }
            return animator as ObjectAnimator
        }
    }

    private inner class ViewPagerPageChangeListener : ViewPager.OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            mNavigationController?.let {
                if (it.getSelected() != position) {
                    it.setSelect(position)
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        if (mNavigationController == null) {
            return super.onSaveInstanceState()
        }
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState())
        bundle.putInt(STATUS_SELECTED, mNavigationController!!.getSelected())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val selected = state.getInt(STATUS_SELECTED, 0)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATUS))

            if (selected != 0) {
                mNavigationController?.setSelect(selected)
            }

            return
        }
        super.onRestoreInstanceState(state)
    }
}