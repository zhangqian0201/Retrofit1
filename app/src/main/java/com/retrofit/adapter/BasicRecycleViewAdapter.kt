package com.retrofit.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.retrofit.R


/**
 * Created by zq on 2018/6/11
 */
abstract class BasicRecycleViewAdapter<T, M : ItemRecycleViewHolder<T>>(private var list: MutableList<T>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected val INDEX_CONTENT = 0
    protected val INDEX_FOOTER = 1
    protected val INDEX_HEARD = 2
    private var footerViewHolder: FooterViewHolder? = null
    var footerType = 0
        set(value) {
            field = value
            footerViewHolder?.setShowFooterType(footerType)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            INDEX_CONTENT -> {
                (holder as ItemRecycleViewHolder<T>).bindHolder(list[position], position);
            }
        }
    }

    override fun getItemCount(): Int = list.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        var viewHolder: RecyclerView.ViewHolder? = null
        when (viewType) {
            INDEX_CONTENT -> {
                view = LayoutInflater.from(parent.context).inflate(getItemLayoutId(), parent, false)
                viewHolder = getItemViewHolder(view)
            }
            INDEX_FOOTER -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.sample_common_list_footer, parent, false)
                footerViewHolder = FooterViewHolder(view)
                viewHolder = footerViewHolder
            }
        }
        return viewHolder!!
    }

    override fun getItemViewType(position: Int): Int {
        Log.e("ItemViewType", "list.size == " + list.size + "\nposition == " + position)
        return if (itemCount == position + 1)
            INDEX_FOOTER
        else INDEX_CONTENT
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == INDEX_FOOTER
                            || getItemViewType(position) == INDEX_HEARD)
                        manager.spanCount
                    else
                        1
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        lp?.let {
            if (it is StaggeredGridLayoutManager.LayoutParams) {
                it.isFullSpan = getItemViewType(holder.layoutPosition) == INDEX_FOOTER
                        || getItemViewType(holder.layoutPosition) == INDEX_HEARD
            }
        }
    }

    protected abstract fun getItemLayoutId(): Int

    protected abstract fun getItemViewHolder(view: View): M
}