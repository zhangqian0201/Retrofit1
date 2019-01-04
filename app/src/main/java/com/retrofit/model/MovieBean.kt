package com.retrofit.model

/**
 * Created by zq on 2018/6/30
 */
class MovieBean(val type: Int, val title: String, val videos: List<VideoBean>)

class VideoBean(val imgUrl: String, val name: String)