package com.retrofit.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import com.retrofit.api.Api
import com.retrofit.model.ImageBean
import com.retrofit.model.ImageResult
import com.retrofit.utils.ApiUtls
import com.retrofit.view.BaseView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by zq on 2018/6/11
 */
@SuppressLint("CheckResult")
class ImagePresenter : BasePresenter<BaseView<ImageBean>>() {
    private val api by lazy { ApiUtls.getApi(Api.welfareUrl) }
    private val imageViews: MutableList<ImageView> = ArrayList()
    lateinit var context: Context

    fun openDetail(view: ImageView, mList: MutableList<ImageResult>, position: Int, context: Context) {
        this.context = context
        if (imageViews.size < mList.size) {
            for (index in 0..(mList.size - imageViews.size)) {
                imageViews.add(view)
            }
        }
    }

    override fun loadMore() {
        getData(false)
    }

    override fun refresh() {
        from = 1
        getData(true)
    }

    private fun getData(isFirstLoad: Boolean) {
        val data = api.getImageList(from)
        data.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    from++
                    mvpView?.loadSuccess(it, isFirstLoad)
                }, {
                    mvpView?.loadFail(it.message)
                })
    }
}