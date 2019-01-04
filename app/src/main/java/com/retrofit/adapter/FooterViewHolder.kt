package com.retrofit.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewStub
import com.retrofit.R

/**
 * Created by zq on 2018/6/11
 */
class FooterViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    private var loadingView = view.findViewById<ViewStub>(R.id.loading_viewstub)
    private var errorView = view.findViewById<ViewStub>(R.id.network_error_viewstub)
    private var endView = view.findViewById<ViewStub>(R.id.end_viewstub)

    companion object {
        val LOADING = 0
        val END = 1
        val ERROR = 2
        val Normal = 3
    }

    fun setShowFooterType(type: Int) {
        when (type) {
            Normal -> {
                loadingView.visibility = View.GONE
                errorView.visibility = View.GONE
                endView.visibility = View.GONE
            }
            LOADING -> {
                loadingView.visibility = View.VISIBLE
                errorView.visibility = View.GONE
                endView.visibility = View.GONE
            }
            END -> {
                loadingView.visibility = View.GONE
                errorView.visibility = View.GONE
                endView.visibility = View.VISIBLE
            }
            ERROR -> {
                loadingView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                endView.visibility = View.GONE
            }
        }
    }
}