package com.retrofit.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by zq on 2018/6/11
 */
class ViewPagerAdapter(private val list: List<Fragment>, fm: FragmentManager, private var tabText: Array<String>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = list[position]

    override fun getCount(): Int = list.size

    override fun getPageTitle(position: Int): CharSequence = tabText[position]
}