package com.retrofit.view

import com.ljq.mvpframework.view.BaseMvpView

/**
 * Created by zq on 2018/6/11
 */
interface BaseView<M> : BaseMvpView {
    fun loadSuccess(model: M, isFirstLoad: Boolean)

    fun loadFail(msg: String?)
}