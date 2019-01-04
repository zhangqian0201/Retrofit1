package com.retrofit.activity

import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import com.ljq.mvpframework.presenter.BaseMvpPresenter
import com.ljq.mvpframework.view.BaseMvpView
import com.retrofit.R
import com.retrofit.adapter.ViewPagerAdapter
import com.retrofit.fragment.GirlsFragment
import com.retrofit.fragment.ImageFragment
import com.retrofit.fragment.MovieFragment
import com.retrofit.fragment.MyFragment
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("DEPRECATION")
class MainActivity<view : BaseMvpView, presenter : BaseMvpPresenter<view>> : BaseActivity<view, presenter>(), BaseMvpView {
    private val mTabImg = intArrayOf(R.drawable.ic_wallpaper_teal_24dp, R.drawable.ic_image_teal_24dp, R.drawable.ic_movies_teal_24dp, R.drawable.ic_movies_teal_24dp)
    private val mTabColor = intArrayOf(R.color.comic, R.color.movie, R.color.set, R.color.white)

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initObject() {
        val tabText = arrayOf("福利", "妹子", "电影", "我的")
        val list = listOf<Fragment>(ImageFragment.instance, GirlsFragment.instance, MovieFragment.instance, MyFragment.instance)
        viewPager.adapter = ViewPagerAdapter(list, supportFragmentManager, tabText)
        setBottomTab()
        viewPager.offscreenPageLimit = 2
    }

    override fun initData() {

    }

    override fun initListener() {
        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bottomView.selectTab(position)
            }
        })
    }

    private fun setBottomTab() {
        //汉字是否一直显示:(false:显示选中的；true全部显示)
        bottomView.isWithText(true)
        //开启跑到左边
//        bottomView.activateTabletMode();
        //整体背景色 设置为false时icon和汉字显示颜色能用
        bottomView.isColoredBackground(true);
//        bottomView.setItemActiveColorWithoutColoredBackground(ContextCompat.getColor(this, R.color.movie))
        //去掉影子
        // bottomView.disableShadow()
        //绑定viewpager
        bottomView.setUpWithViewPager(viewPager, mTabColor, mTabImg)
    }
}