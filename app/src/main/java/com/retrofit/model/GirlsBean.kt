package com.retrofit.model

/**
 * Created by zq on 2018/6/15
 */
class GirlsBean(var code: Int, var msg: String, var newslist: MutableList<GirlBean>)

class GirlBean(var ctime: String, var title: String, var description: String, var picUrl: String, var url: String)