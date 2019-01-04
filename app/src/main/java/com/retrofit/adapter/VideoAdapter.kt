package com.retrofit.adapter

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.retrofit.R
import com.retrofit.model.VideoBean

/**
 * Created by zq on 2018/7/23
 */
@SuppressLint("InflateParams")
class VideoAdapter(private val mList: List<VideoBean>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindHolder(mList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_movie_video, null))
    }

    override fun getItemCount(): Int = mList.size

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val videoImage: ImageView = itemView.findViewById(R.id.video_image)
        private val videoName: TextView = itemView.findViewById(R.id.video_name)

        fun bindHolder(videoBean: VideoBean) {
            videoName.text = videoBean.name
            Glide.with(itemView.context).load(videoBean.imgUrl).into(videoImage)
        }
    }
}