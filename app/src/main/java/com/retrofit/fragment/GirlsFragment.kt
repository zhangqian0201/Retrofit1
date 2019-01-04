package com.retrofit.fragment

import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import com.ljq.mvpframework.factory.CreatePresenter
import com.retrofit.R
import com.retrofit.adapter.GirlsAdapter
import com.retrofit.model.GirlBean
import com.retrofit.model.GirlsBean
import com.retrofit.presenter.GirlsPresenter
import com.retrofit.view.BaseView
import com.retrofit.widget.SmartPullableLayout
import kotlinx.android.synthetic.main.fragment_girls.*

/**
 * Created by zq on 2018/6/15
 */
@CreatePresenter(GirlsPresenter::class)
class GirlsFragment : BaseFragment<BaseView<GirlsBean>, GirlsPresenter>(), BaseView<GirlsBean> {
    private lateinit var mList: MutableList<GirlBean>
    private lateinit var mAdapter: GirlsAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var hasMore = false

    override fun getLayoutId(): Int = R.layout.fragment_girls

    override fun initObject() {
        mList = ArrayList()
        mLayoutManager = LinearLayoutManager(context)
        mGirlsRecycle.layoutManager = mLayoutManager
        mAdapter = GirlsAdapter(mList) { view, position -> mvpPresenter.openDetail(view as ImageView, mList, position, context!!) }
        mGirlsRecycle.adapter = mAdapter
    }

    override fun initData() {
        mvpPresenter.refresh()
    }

    override fun initListener() {
        mGirlsRefresh.setOnPullListener(object : SmartPullableLayout.OnPullListener {
            override fun onPullDown() {
                mvpPresenter.refresh()
            }

            override fun onPullUp() {
                if (hasMore) {
                    mvpPresenter.loadMore()
                } else {
                    mGirlsRefresh.loadOver()
                }
            }
        })
    }

    override fun loadSuccess(model: GirlsBean, isFirstLoad: Boolean) {
        hasMore = model.newslist.size == 10
        if (hasMore || isFirstLoad) {
            mGirlsRefresh.stopPullBehavior()
        } else {
            mGirlsRefresh.loadOver()
        }
        if (isFirstLoad && !mList.isEmpty()) {
            mList.clear()
        }
        mList.addAll(model.newslist)
        mAdapter.notifyDataSetChanged()
    }

    override fun loadFail(msg: String?) {
        mGirlsRefresh.stopPullBehavior()
    }

    companion object {
        val instance: GirlsFragment by lazy {
            GirlsFragment()
        }
    }
}