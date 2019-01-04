package com.retrofit.fragment

import android.support.v7.widget.LinearLayoutManager
import com.ljq.mvpframework.factory.CreatePresenter
import com.retrofit.R
import com.retrofit.adapter.MovieAdapter
import com.retrofit.model.ImageResult
import com.retrofit.model.MovieBean
import com.retrofit.presenter.MoviePresenter
import com.retrofit.utils.ToastUtil
import com.retrofit.view.BaseView
import kotlinx.android.synthetic.main.fragment_movie.*

/**
 * Created by zq on 2018/7/23
 */
@CreatePresenter(MoviePresenter::class)
class MovieFragment : BaseFragment<BaseView<List<MovieBean>>, MoviePresenter>(), BaseView<List<MovieBean>> {
    private lateinit var mList: MutableList<MovieBean>
    private lateinit var imgList: MutableList<ImageResult>
    private lateinit var mAdapter: MovieAdapter

    override fun getLayoutId(): Int = R.layout.fragment_movie

    override fun initObject() {
        mList = ArrayList();
        imgList = ArrayList();
        mMovieRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = MovieAdapter(mList, imgList)
        mMovieRecyclerView.adapter = mAdapter
    }

    override fun initData() {
        mvpPresenter.refresh();
    }

    override fun initListener() {
        mMovieRefreshLayout.setOnRefreshListener { mvpPresenter.refresh() }
        mChangeText.setOnClickListener {
            mAdapter.contentType = if (mAdapter.contentType == 0) 1 else 0
            if (ImageFragment.instance.getImageList().size > imgList.size) {
                imgList.clear()
                imgList.addAll(ImageFragment.instance.getImageList())
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun loadSuccess(model: List<MovieBean>, isFirstLoad: Boolean) {
        if (mMovieRefreshLayout.isRefreshing) {
            mMovieRefreshLayout.isRefreshing = false
        }
        if (!mList.isEmpty()) {
            mList.clear()
        }
        mList.addAll(model)
        mAdapter.notifyDataSetChanged()
    }

    override fun loadFail(msg: String?) {
        if (mMovieRefreshLayout.isRefreshing) {
            mMovieRefreshLayout.isRefreshing = false
        }
        msg?.let { ToastUtil.showShort(context!!, it) }
    }

    companion object {
        val instance: MovieFragment by lazy {
            MovieFragment()
        }
    }
}