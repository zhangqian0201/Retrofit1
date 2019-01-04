package com.retrofit.utils

import android.annotation.SuppressLint
import com.retrofit.api.Api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by zq on 2018/6/11.
 */
@SuppressLint("StaticFieldLeak")
object ApiUtls {
    fun getApi(baseUrl: String): Api {
        val okClientBuilder = OkHttpClient.Builder().apply {
            //log
            this.addInterceptor(HttpLoggingInterceptor().apply { this.level = HttpLoggingInterceptor.Level.BODY })
            //...
        }
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okClientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Api::class.java)
    }
}