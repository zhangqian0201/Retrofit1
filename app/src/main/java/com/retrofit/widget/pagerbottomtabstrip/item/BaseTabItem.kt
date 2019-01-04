package com.retrofit.widget.pagerbottomtabstrip.item

import android.content.Context
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 所有自定义Item都必须继承此类
 */
abstract class BaseTabItem : FrameLayout {

    /**
     * 获取标题文字
     */
    abstract val title: String

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    /**
     * 设置选中状态
     */
    abstract fun setChecked(checked: Boolean)

    /**
     * 设置消息数字。注意：数字需要大于0才会显示
     */
    abstract fun setMessageNumber(number: Int)

    /**
     * 设置是否显示无数字的小圆点。注意：如果消息数字不为0，优先显示带数字的圆
     */
    abstract fun setHasMessage(hasMessage: Boolean)

    /**
     * 已选中的状态下再次点击时触发
     */
    fun onRepeat() {}

}