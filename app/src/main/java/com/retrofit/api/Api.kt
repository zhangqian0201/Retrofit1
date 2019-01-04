package com.retrofit.api

import com.retrofit.model.GirlsBean
import com.retrofit.model.ImageBean
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by zq on 2018/6/11
 */
interface Api {
    companion object {
        val welfareUrl = "http://gank.io/api/data/"
        val girlUrl = "https://api.tianapi.com/"
        val girlKey = "eaf6cf0c428da040349003524a2a8389"
    }

    @GET("福利/10/{page}")
    fun getImageList(@Path("page") page: Int): Observable<ImageBean>

    @GET("meinv/")
    fun getGirls(@Query("key") key: String, @Query("num") num: Int): Observable<GirlsBean>

    @GET
    fun downloadPicFromNet(@Url fileUrl:String): Observable<ResponseBody>
}