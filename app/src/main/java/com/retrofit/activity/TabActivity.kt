package com.retrofit.activity

import android.os.Build
import android.support.annotation.RequiresApi
import com.ljq.mvpframework.presenter.BaseMvpPresenter
import com.ljq.mvpframework.view.BaseMvpView
import com.retrofit.R
import com.retrofit.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.tab_layout.*


class TabActivity<view : BaseMvpView, presenter : BaseMvpPresenter<view>> : BaseActivity<view, presenter>(), BaseMvpView {
    private var picBottom = 0

    override fun getLayoutId(): Int = R.layout.activity_tab

    override fun initObject() {

    }

    override fun initData() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initListener() {
        classification.setOnClickListener { ToastUtil.showShort(this@TabActivity, "分类2") }
        mScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val top = Math.max(scrollY, picBottom)
            ll_tab.layout(0, top, ll_tab.width, top + ll_tab.height)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            picBottom = iv_pic.bottom
        }
    }
}