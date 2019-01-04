package com.retrofit.widget.pagerbottomtabstrip.item

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.retrofit.R
import com.retrofit.widget.pagerbottomtabstrip.internal.RoundMessageView
import com.retrofit.widget.pagerbottomtabstrip.internal.Utils


/**
 * 材料设计风格项
 */
class MaterialItemView : BaseTabItem {

    private val mMessages: RoundMessageView
    private val mLabel: TextView
    private val mIcon: ImageView

    private var mDefaultDrawable: Drawable? = null
    private var mCheckedDrawable: Drawable? = null

    private var mDefaultColor: Int = 0
    private var mCheckedColor: Int = 0

    private val mTranslation: Float
    private val mTranslationHideTitle: Float

    private val mTopMargin: Int
    private val mTopMarginHideTitle: Int

    private var mHideTitle: Boolean = false
    private var mChecked: Boolean = false

    private var mAnimator: ValueAnimator? = null
    /**
     * 获取动画运行值[0,1]
     */
    var animValue = 1f
        private set

    private var mIsMeasured = false

    override val title: String
        get() = mLabel.text.toString()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val scale = context.resources.displayMetrics.density

        mTranslation = scale * 2
        mTranslationHideTitle = scale * 10
        mTopMargin = (scale * 8).toInt()
        mTopMarginHideTitle = (scale * 16).toInt()

        LayoutInflater.from(context).inflate(R.layout.item_material, this, true)

        mIcon = findViewById(R.id.icon)
        mLabel = findViewById(R.id.label)
        mMessages = findViewById(R.id.messages)
    }

    fun initialization(title: String, drawable: Drawable, checkedDrawable: Drawable, color: Int, checkedColor: Int) {

        mDefaultColor = color
        mCheckedColor = checkedColor

        mDefaultDrawable = Utils.tint(drawable, mDefaultColor)
        mCheckedDrawable = Utils.tint(checkedDrawable, mCheckedColor)

        mLabel.text = title
        mLabel.setTextColor(color)

        mIcon.setImageDrawable(mDefaultDrawable)

        mAnimator = ValueAnimator.ofFloat(1f)
        mAnimator!!.duration = 115L
        mAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mAnimator!!.addUpdateListener { animation ->
            animValue = animation.animatedValue as Float
            if (mHideTitle) {
                mIcon.translationY = -mTranslationHideTitle * animValue
            } else {
                mIcon.translationY = -mTranslation * animValue
            }
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f + animValue * 2f)
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mIsMeasured = true
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked == checked) {
            return
        }

        mChecked = checked

        if (mHideTitle) {
            mLabel.visibility = if (mChecked) View.VISIBLE else View.INVISIBLE
        }

        if (mIsMeasured) {
            // 切换动画
            if (mChecked) {
                mAnimator!!.start()
            } else {
                mAnimator!!.reverse()
            }
        } else if (mChecked) { // 布局还未测量时选中，直接转换到选中的最终状态
            if (mHideTitle) {
                mIcon.translationY = -mTranslationHideTitle
            } else {
                mIcon.translationY = -mTranslation
            }
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        } else { // 布局还未测量并且未选中，保持未选中状态
            mIcon.translationY = 0f
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        // 切换颜色
        if (mChecked) {
            mIcon.setImageDrawable(mCheckedDrawable)
            mLabel.setTextColor(mCheckedColor)
        } else {
            mIcon.setImageDrawable(mDefaultDrawable)
            mLabel.setTextColor(mDefaultColor)
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
     * 设置是否隐藏文字
     */
    fun setHideTitle(hideTitle: Boolean) {
        mHideTitle = hideTitle

        val iconParams = mIcon.layoutParams as FrameLayout.LayoutParams

        if (mHideTitle) {
            iconParams.topMargin = mTopMarginHideTitle
        } else {
            iconParams.topMargin = mTopMargin
        }

        mLabel.visibility = if (mChecked) View.VISIBLE else View.INVISIBLE

        mIcon.layoutParams = iconParams
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
