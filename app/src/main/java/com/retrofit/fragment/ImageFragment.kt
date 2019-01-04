package com.retrofit.fragment

import android.support.v7.widget.GridLayoutManager
import android.widget.ImageView
import com.ljq.mvpframework.factory.CreatePresenter
import com.retrofit.R
import com.retrofit.adapter.ImageAdapter
import com.retrofit.model.ImageBean
import com.retrofit.model.ImageResult
import com.retrofit.presenter.ImagePresenter
import com.retrofit.view.BaseView
import com.retrofit.widget.SmartPullableLayout
import kotlinx.android.synthetic.main.fragment_image.*

/**
 * Created by zq on 2018/6/11
 */

@CreatePresenter(ImagePresenter::class)
class ImageFragment : BaseFragment<BaseView<ImageBean>, ImagePresenter>(), BaseView<ImageBean> {
    private lateinit var mAdapter: ImageAdapter
    private lateinit var mList: MutableList<ImageResult>
    private lateinit var mLayoutManager: GridLayoutManager
    private var hasMore = false

    override fun getLayoutId(): Int = R.layout.fragment_image

    override fun initObject() {
        mList = ArrayList()
        mLayoutManager = GridLayoutManager(context, 2)
        mImageRecycle.layoutManager = mLayoutManager
        mAdapter = ImageAdapter(mList) { view, position -> mvpPresenter.openDetail(view as ImageView, mList, position, context!!) }
        mImageRecycle.adapter = mAdapter
    }

    override fun initData() {
        mvpPresenter.refresh()
    }

    override fun initListener() {
        mImageRefresh.setOnPullListener(object : SmartPullableLayout.OnPullListener {
            override fun onPullDown() {
                mvpPresenter.refresh()
            }

            override fun onPullUp() {
                if (hasMore) {
                    mvpPresenter.loadMore()
                } else {
                    mImageRefresh.loadOver()
                }
            }
        })
    }

    override fun loadSuccess(model: ImageBean, isFirstLoad: Boolean) {
        hasMore = model.results.size == 10
        if (hasMore || isFirstLoad) {
            mImageRefresh.stopPullBehavior()
        } else {
            mImageRefresh.loadOver()
        }
        if (isFirstLoad && !mList.isEmpty()) {
            mList.clear()
        }
        mList.addAll(model.results)
        mAdapter.notifyDataSetChanged()
    }

    fun getImageList(): MutableList<ImageResult> = mList

    override fun loadFail(msg: String?) {
        mImageRefresh?.stopPullBehavior()
    }

    companion object {
        val instance: ImageFragment by lazy {
            ImageFragment()
        }
    }
}