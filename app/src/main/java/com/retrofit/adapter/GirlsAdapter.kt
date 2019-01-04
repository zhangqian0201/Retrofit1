package com.retrofit.adapter

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.retrofit.R
import com.retrofit.model.GirlBean

/**
 * Created by zq on 2018/6/15
 */
class GirlsAdapter(var list: MutableList<GirlBean>, private var onItemClick: (View, Int) -> Unit) : RecyclerView.Adapter<GirlsAdapter.GirlsViewHolder>() {

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: GirlsViewHolder, position: Int) {
        holder.bindHolder(list[position], position)
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GirlsViewHolder {
        return GirlsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_girls, null), onItemClick)
    }

    class GirlsViewHolder(val view: View, private var onItemClick: (View, Int) -> Unit) : ItemRecycleViewHolder<GirlBean>(view) {
        private val imageView: ImageView = view.findViewById(R.id.girls_content)

        @SuppressLint("SetTextI18n")
        override fun bindHolder(t: GirlBean, position: Int) {
            Glide.with(view.context).load(t.picUrl).apply(RequestOptions().error(R.drawable.error_image).placeholder(R.drawable.place_holder_image)).into(imageView)
            imageView.setOnClickListener { v -> onItemClick(v, position) }
        }
    }
}