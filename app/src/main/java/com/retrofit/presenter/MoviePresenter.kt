package com.retrofit.presenter

import com.retrofit.model.MovieBean
import com.retrofit.model.VideoBean
import com.retrofit.view.BaseView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import java.lang.Exception

/**
 * Created by zq on 2018/7/11
 */
class MoviePresenter : BasePresenter<BaseView<List<MovieBean>>>() {
    override fun loadMore() {

    }

    override fun refresh() {
        getData()
    }

    private fun getData() {
        Observable.create<List<MovieBean>> { emitter ->
            try {
                val doc = Jsoup.connect("http://maoyan.com/").get()
                val banners = doc.select("div.banner").select("a")
                val titles = doc.select("div.content").select("div.panel-header")
                val movies = ArrayList<MovieBean>()
                val videos = doc.select("div.content").select("dl")
                if (!banners.isEmpty()) {
                    movies.add(MovieBean(0, "banner", banners.map {
                        VideoBean(it.attr("data-bgUrl"),
                                "http://maoyan.com/films/news/41840${it.attr("data-bgUrl")}")
                    }))
                }

                val sum = 3
                if (!videos.isEmpty()) {
                    (0..1).mapTo(movies) { index ->
                        MovieBean(1, titles[sum + index].select("span.panel-title").text(),
                                videos[index].select("div.movie-item")
                                        .map {
                                            VideoBean(it.select("img")[1].dataset()["src"] as String,
                                                    it.select("div.movie-title").text())
                                        })
                    }
                }
                if (!movies.isEmpty()) {
                    emitter.onNext(movies)
                } else {
                    emitter.onError(Throwable("资源获取失败"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { mvpView.loadSuccess(it, false) }
                        , { mvpView.loadFail(it.message) })
    }
}