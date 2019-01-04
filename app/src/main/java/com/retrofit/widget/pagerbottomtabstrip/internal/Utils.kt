package com.retrofit.widget.pagerbottomtabstrip.internal

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.TypedValue


object Utils {

    fun tint(drawable: Drawable, color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        wrappedDrawable.mutate()
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }

    fun newDrawable(drawable: Drawable): Drawable {
        val constantState = drawable.constantState
        return if (constantState != null) constantState.newDrawable() else drawable
    }

    /**
     * 获取colorPrimary的颜色,需要V7包的支持
     * @param context 上下文
     * @return 0xAARRGGBB
     */
    fun getColorPrimary(context: Context): Int {
        val res = context.resources
        val attrRes = res.getIdentifier("colorPrimary", "attr", context.packageName)
        return if (attrRes == 0) {
            -0xff6978
        } else ContextCompat.getColor(context, getResourceId(context, attrRes))
    }

    /**
     * 获取自定义属性的资源ID
     * @param context    上下文
     * @param attrRes    自定义属性
     * @return    resourceId
     */
    private fun getResourceId(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.resourceId
    }

}
