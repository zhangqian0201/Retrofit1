package com.retrofit.widget.pagerbottomtabstrip

import com.retrofit.widget.pagerbottomtabstrip.listener.OnTabItemSelectedListener

/**
 * Created by zq on 2018/6/11
 */
interface ItemController {
    /**
     * 设置选中项
     * @param index 顺序索引
     */
    abstract fun setSelect(index: Int)

    /**
     * 设置导航按钮上显示的圆形消息数字，通过顺序索引。
     * @param index     顺序索引
     * @param number    消息数字
     */
    abstract fun setMessageNumber(index: Int, number: Int)

    /**
     * 设置显示无数字的消息小原点
     * @param index         顺序索引
     * @param hasMessage    true显示
     */
    abstract fun setHasMessage(index: Int, hasMessage: Boolean)

    /**
     * 导航栏按钮点击监听
     * @param listener [OnTabItemSelectedListener]
     */
    abstract fun addTabItemSelectedListener(listener: OnTabItemSelectedListener)

    /**
     * 获取当前选中项索引
     * @return 索引
     */
    abstract fun getSelected(): Int

    /**
     * 获取导航按钮总数
     * @return 总数
     */
    abstract fun getItemCount(): Int

    /**
     * 获取导航按钮文字
     * @param index 顺序索引
     * @return  文字
     */
    abstract fun getItemTitle(index: Int): String
}