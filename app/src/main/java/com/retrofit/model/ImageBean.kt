package com.retrofit.model

/**
 * Created by zq on 2018/6/11
 */
data class ImageBean(var error: Boolean, var results: MutableList<ImageResult>)

data class ImageResult(var _id: String, var url: String, var who: String)