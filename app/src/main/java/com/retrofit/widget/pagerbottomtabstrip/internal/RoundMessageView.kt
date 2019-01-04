package com.retrofit.widget.pagerbottomtabstrip.internal

import android.content.Context
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.retrofit.R
import java.util.*


class RoundMessageView : FrameLayout {
    private val mOval: View
    private val mMessages: TextView

    var messageNumber: Int = 0
        set(number) {
            field = number

            if (messageNumber > 0) {
                mOval.visibility = View.INVISIBLE
                mMessages.visibility = View.VISIBLE

                if (messageNumber < 10) {
                    mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                } else {
                    mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
                }

                if (messageNumber <= 99) {
                    mMessages.text = String.format(Locale.ENGLISH, "%d", messageNumber)
                } else {
                    mMessages.text = String.format(Locale.ENGLISH, "%d+", 99)
                }
            } else {
                mMessages.visibility = View.INVISIBLE
                if (mHasMessage) {
                    mOval.visibility = View.VISIBLE
                }
            }
        }
    private var mHasMessage: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.round_message_view, this, true)

        mOval = findViewById(R.id.oval)
        mMessages = findViewById<View>(R.id.msg) as TextView
        mMessages.typeface = Typeface.DEFAULT_BOLD
        mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
    }

    fun setHasMessage(hasMessage: Boolean) {
        mHasMessage = hasMessage

        if (hasMessage) {
            mOval.visibility = if (messageNumber > 0) View.INVISIBLE else View.VISIBLE
        } else {
            mOval.visibility = View.INVISIBLE
        }
    }

    fun tintMessageBackground(@ColorInt color: Int) {
        val drawable = Utils.tint(ContextCompat.getDrawable(context, R.drawable.round)!!, color)
        ViewCompat.setBackground(mOval, drawable)
        ViewCompat.setBackground(mMessages, drawable)
    }

    fun setMessageNumberColor(@ColorInt color: Int) {
        mMessages.setTextColor(color)
    }

    fun hasMessage(): Boolean {
        return mHasMessage
    }


}
