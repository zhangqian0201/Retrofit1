package com.retrofit.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.retrofit.R
import com.retrofit.model.ImageResult

/**
 * Created by zq on 2018/6/11
 */
class ImageAdapter(var list: MutableList<ImageResult>, private var onItemClick: (View, Int) -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false), onItemClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bindHolder(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    class ImageViewHolder(val view: View, private var onItemClick: (View, Int) -> Unit) : ItemRecycleViewHolder<ImageResult>(view) {
        private var mImageItem: ImageView = view.findViewById(R.id.list_image_item)

        override fun bindHolder(t: ImageResult, position: Int) {
            Glide.with(view.context).load(t.url).into(mImageItem)
            mImageItem.setOnClickListener { v -> onItemClick(v, position) }
        }
    }
}