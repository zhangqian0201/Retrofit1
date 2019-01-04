package com.retrofit.widget.pagerbottomtabstrip.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.internal.RoundMessageView
import com.retrofit.widget.pagerbottomtabstrip.internal.Utils

/**
 * 只有图标的材料设计项(用于垂直布局)
 */
class OnlyIconMaterialItemView : BaseTabItem {

    private val mMessages: RoundMessageView
    private val mIcon: ImageView

    private var mDefaultDrawable: Drawable? = null
    private var mCheckedDrawable: Drawable? = null

    private var mDefaultColor: Int = 0
    private var mCheckedColor: Int = 0

    override lateinit var title: String
        private set

    private var mChecked: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.item_material_only_icon, this, true)

        mIcon = findViewById(R.id.icon)
        mMessages = findViewById(R.id.messages)
    }

    fun initialization(title: String, drawable: Drawable, checkedDrawable: Drawable, color: Int, checkedColor: Int) {

        this.title = title

        mDefaultColor = color
        mCheckedColor = checkedColor

        mDefaultDrawable = Utils.tint(drawable, mDefaultColor)
        mCheckedDrawable = Utils.tint(checkedDrawable, mCheckedColor)

        mIcon.setImageDrawable(mDefaultDrawable)

        if (Build.VERSION.SDK_INT >= 21) {
            background = RippleDrawable(ColorStateList(arrayOf(intArrayOf()), intArrayOf(0xFFFFFF and checkedColor or 0x56000000)), null, null)
        } else {
            setBackgroundResource(R.drawable.material_item_background)
        }
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked == checked) {
            return
        }

        mChecked = checked

        // 切换颜色
        if (mChecked) {
            mIcon.setImageDrawable(mCheckedDrawable)
        } else {
            mIcon.setImageDrawable(mDefaultDrawable)
        }
    }

    override fun setMessageNumber(number: Int) {
        mMessages.visibility = View.VISIBLE
        mMessages.messageNumber = number
    }

    override fun setHasMessage(hasMessage: Boolean) {
        mMessages.visibility = View.VISIBLE
        mMessages.setHasMessage(hasMessage)
    }

    /**
     * 设置消息圆形的颜色
     */
    fun setMessageBackgroundColor(@ColorInt color: Int) {
        mMessages.tintMessageBackground(color)
    }

    /**
     * 设置消息数据的颜色
     */
    fun setMessageNumberColor(@ColorInt color: Int) {
        mMessages.setMessageNumberColor(color)
    }
}
