package com.retrofit.widget.pagerbottomtabstrip

import android.support.v4.view.ViewPager
import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener

/**
 * Created by zq on 2018/6/11
 */
class NavigationController(private val mBottomLayoutController: BottomLayoutController, private val mItemController: ItemController) : ItemController, BottomLayoutController {

    override fun getSelected(): Int = mItemController.getSelected()

    override fun getItemCount(): Int = mItemController.getItemCount()

    override fun setSelect(index: Int) {
        mItemController.setSelect(index)
    }

    override fun setMessageNumber(index: Int, number: Int) {
        mItemController.setMessageNumber(index, number)
    }

    override fun setHasMessage(index: Int, hasMessage: Boolean) {
        mItemController.setHasMessage(index, hasMessage)
    }

    override fun addTabItemSelectedListener(listener: OnTabItemSelectedListener) {
        mItemController.addTabItemSelectedListener(listener)
    }

    override fun getItemTitle(index: Int): String = mItemController.getItemTitle(index)

    override fun setupWithViewPager(viewPager: ViewPager) {
        mBottomLayoutController.setupWithViewPager(viewPager)
    }

    override fun hideBottomLayout() {
        mBottomLayoutController.hideBottomLayout()
    }

    override fun showBottomLayout() {
        mBottomLayoutController.showBottomLayout()
    }
}