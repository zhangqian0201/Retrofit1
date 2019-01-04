package com.retrofit.adapter

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by zq on 2018/6/11
 */
abstract class ItemRecycleViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bindHolder(t: T, position: Int)
}