package com.retrofit.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.retrofit.R

/**
 * Created by zq on 2018/8/7
 */
@SuppressLint("InflateParams")
class ToastUtil {
    companion object {
        private var isDebug = false
        var isShow = true

        var mToast: Toast? = null

        private var currentToast: Toast? = null
        @SuppressLint("StaticFieldLeak")
        private var toastView: View? = null

        private fun createToast(context: Context, msg: CharSequence, mDuration: Int): Toast? {
            val mView = LayoutInflater.from(context).inflate(R.layout.layout_custom_toast, null)
            val toastMsg_tv: TextView = mView.findViewById(R.id.toastMsg_tv)
            toastMsg_tv.text = msg
            mToast = Toast(context)
            mToast?.apply {
                view = mView
                duration = mDuration
                setGravity(Gravity.CENTER, 0, 0)
            }
            return mToast
        }

        fun showShort(context: Context, message: CharSequence) {
            if (isShow) {
                cancel()

                mToast = createToast(context, message, Toast.LENGTH_SHORT)
                mToast?.show()
            }
        }

        fun showLong(context: Context, message: CharSequence) {
            if (isShow) {
                cancel()
                mToast = createToast(context, message, Toast.LENGTH_LONG)
                mToast?.show()
            }
        }

        private fun cancel() {
            mToast?.cancel()
        }

        /**
         * 使用同1个toast,避免多toast重复问题
         */
        fun makeText(context: Context?, text: CharSequence, mDuration: Int): Toast? {
            if (currentToast == null && context != null) {
                currentToast = Toast.makeText(context, text, mDuration)
                toastView = currentToast!!.view
            }
            if (toastView != null) {
                currentToast?.apply {
                    view = toastView
                    setText(text)
                    duration = mDuration
                }
            }
            return currentToast
        }
    }
}