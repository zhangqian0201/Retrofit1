package com.retrofit.widget.pagerbottomtabstrip

import android.support.v4.view.ViewPager

/**
 * Created by zq on 2018/6/11
 */
interface BottomLayoutController {
    /**
     * 方便适配ViewPager页面切换
     *
     *
     * 注意：ViewPager页面数量必须等于导航栏的Item数量
     * @param viewPager [ViewPager]
     */
    abstract fun setupWithViewPager(viewPager: ViewPager)

    /**
     * 向下移动隐藏导航栏
     */
    abstract fun hideBottomLayout()

    /**
     * 向上移动显示导航栏
     */
    abstract fun showBottomLayout()
}