package com.retrofit.presenter

import android.content.Context
import android.widget.ImageView
import com.retrofit.api.Api
import com.retrofit.model.GirlBean
import com.retrofit.model.GirlsBean
import com.retrofit.utils.ApiUtls
import com.retrofit.view.BaseView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by zq on 2018/6/15
 */
class GirlsPresenter : BasePresenter<BaseView<GirlsBean>>() {
    private val api by lazy { ApiUtls.getApi(Api.girlUrl) }
    private val imageViews: MutableList<ImageView> = ArrayList()
//    lateinit var transferConfig: TransferConfig
    lateinit var context: Context
//    private val transferee by lazy {
//        Transferee.getDefault(context)
//    }

    fun openDetail(view: ImageView, mList: MutableList<GirlBean>, position: Int, context: Context) {
        this.context = context
        if (imageViews.size < mList.size) {
            for (index in 0..(mList.size - imageViews.size)) {
                imageViews.add(view)
            }
        }
//        Log.e("girlsDetail", "position == $position   picUrl == ${mList[position].picUrl}")
//        transferConfig = TransferConfig.build()
//                .setSourceImageList(mList.map { it.picUrl })//SourceImageList\OriginImageList size一致
//                .setOriginImageList(imageViews)
//                .setMissDrawable(ColorDrawable(Color.parseColor("#DCDDE1")))
//                .setErrorDrawable(ColorDrawable(Color.parseColor("#DCDDE1")))
//                .setProgressIndicator(ProgressPieIndicator())
//                .setNowThumbnailIndex(position)//仅一张
//                .setIndexIndicator(NumberIndexIndicator())
//                .setImageLoader(GlideImageLoader.with(context.applicationContext))
//                .create()
//        transferee.apply(transferConfig).show()
    }

    override fun loadMore() {
        getData(false)
    }

    override fun refresh() {
        getData(true)
    }

    private fun getData(isFirstLoad: Boolean) {
        val data = api.getGirls(Api.girlKey, 10)
        data.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.newslist.map {
                        if (it.picUrl.contains("tesetu")) {
                            it.picUrl == "http://ww1.sinaimg.cn/large/0065oQSqly1frept5di16j30p010g0z9.jpg"
                        }
                    }
                    mvpView?.loadSuccess(it, isFirstLoad)
                }, {
                    mvpView?.loadFail(it.message)
                })
    }
}