package com.retrofit.presenter

import com.ljq.mvpframework.presenter.BaseMvpPresenter
import com.ljq.mvpframework.view.BaseMvpView

/**
 * Created by zq on 2018/6/11
 */
abstract class BasePresenter<V : BaseMvpView> : BaseMvpPresenter<V>() {
    protected var from = 0

    abstract fun loadMore()

    abstract fun refresh()
}