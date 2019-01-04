package com.retrofit.activity

import android.os.Bundle
import com.ljq.mvpframework.presenter.BaseMvpPresenter
import com.ljq.mvpframework.view.AbstractMvpAppCompatActivity
import com.ljq.mvpframework.view.BaseMvpView

/**
 * Created by zq on 2018/6/11
 */
abstract class BaseActivity<V : BaseMvpView, P : BaseMvpPresenter<V>> : AbstractMvpAppCompatActivity<V, P>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initObject()
        initListener()
        initData()
    }

    abstract fun getLayoutId(): Int

    abstract fun initObject()

    abstract fun initData()

    abstract fun initListener()
}