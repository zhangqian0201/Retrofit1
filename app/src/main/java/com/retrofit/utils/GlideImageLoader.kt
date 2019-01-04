package com.retrofit.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.youth.banner.loader.ImageLoader

/**
 * Created by zq on 2018/6/30
 */
class GlideImageLoader : ImageLoader() {
    override fun displayImage(mContext: Context?, path: Any?, imageView: ImageView?) {
        mContext?.let { context -> imageView?.let { Glide.with(context).load(path).into(it) } }
    }
}