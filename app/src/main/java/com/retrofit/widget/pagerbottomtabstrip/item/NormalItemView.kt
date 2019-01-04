package com.retrofit.widget.pagerbottomtabstrip.item


import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.internal.RoundMessageView


class NormalItemView : BaseTabItem {

    private val mIcon: ImageView
    private val mTitle: TextView
    private val mMessages: RoundMessageView

    private var mDefaultDrawable: Int = 0
    private var mCheckedDrawable: Int = 0

    private var mDefaultTextColor = 0x56000000
    private var mCheckedTextColor = 0x56000000

    override val title: String
        get() = mTitle.text.toString()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.item_normal, this, true)

        mIcon = findViewById(R.id.icon)
        mTitle = findViewById(R.id.title)
        mMessages = findViewById(R.id.messages)
    }

    /**
     * 方便初始化的方法
     * @param drawableRes           默认状态的图标
     * @param checkedDrawableRes    选中状态的图标
     * @param title                 标题
     */
    fun initialize(@DrawableRes drawableRes: Int, @DrawableRes checkedDrawableRes: Int, title: String) {
        mDefaultDrawable = drawableRes
        mCheckedDrawable = checkedDrawableRes
        mTitle.text = title
    }

    override fun setChecked(checked: Boolean) {
        if (checked) {
            mIcon.setImageResource(mCheckedDrawable)
            mTitle.setTextColor(mCheckedTextColor)
        } else {
            mIcon.setImageResource(mDefaultDrawable)
            mTitle.setTextColor(mDefaultTextColor)
        }
    }

    override fun setMessageNumber(number: Int) {
        mMessages.messageNumber = number
    }

    override fun setHasMessage(hasMessage: Boolean) {
        mMessages.setHasMessage(hasMessage)
    }

    fun setTextDefaultColor(@ColorInt color: Int) {
        mDefaultTextColor = color
    }

    fun setTextCheckedColor(@ColorInt color: Int) {
        mCheckedTextColor = color
    }
}
