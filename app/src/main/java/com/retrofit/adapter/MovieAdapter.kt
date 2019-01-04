package com.retrofit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.retrofit.R
import com.retrofit.model.ImageResult
import com.retrofit.model.MovieBean
import com.retrofit.utils.GlideImageLoader
import com.youth.banner.Banner
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer

/**
 * Created by zq on 2018/7/23
 */
@Suppress("DEPRECATION")
@SuppressLint("InflateParams")
class MovieAdapter(private var mList: MutableList<MovieBean>, private var imgList: MutableList<ImageResult>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_BANNER = 0
    private val MOVIE_CONTENT = 1
    private val IMG_CONTENT = 2
    var contentType = 0
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_BANNER -> (holder as BannerHolder).bindHolder(mList[position])
            MOVIE_CONTENT -> (holder as MovieHolder).bindHolder(mList[position])
            IMG_CONTENT -> (holder as ImageViewHolder).bindHolder(imgList[position])
        }
    }

    override fun getItemCount(): Int = if (contentType == 0) mList.size else imgList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        var viewHolder: RecyclerView.ViewHolder? = null
        when (viewType) {
            ITEM_BANNER -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_banner, null)
                viewHolder = BannerHolder(view)
            }
            MOVIE_CONTENT -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_movies, null)
                viewHolder = MovieHolder(view)
            }
            IMG_CONTENT -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, null)
                viewHolder = ImageViewHolder(view)
            }
        }
        return viewHolder!!
    }

    override fun getItemViewType(position: Int): Int = if (contentType == 0) mList[position].type else IMG_CONTENT


    class BannerHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val banner: Banner = itemView.findViewById(R.id.banner)

        init {
            val params = banner.layoutParams
            val manager = itemView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            params.height = manager.defaultDisplay.width / 2
        }

        fun bindHolder(movieBean: MovieBean) {
            banner.setImageLoader(GlideImageLoader())
                    .setImages(movieBean.videos.map { it.imgUrl })
                    .setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                    .setBannerAnimation(Transformer.Accordion)
                    .start()
        }
    }

    class MovieHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieImages: RecyclerView = itemView.findViewById(R.id.movieImages)

        fun bindHolder(movieBean: MovieBean) {
            movieTitle.text = movieBean.title
            movieImages.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            movieImages.adapter = VideoAdapter(movieBean.videos)
        }
    }

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var mImageItem: ImageView = itemView.findViewById(R.id.list_image_item)

        fun bindHolder(t: ImageResult) {
            Glide.with(itemView.context).load(t.url).apply(RequestOptions().error(R.drawable.error_image).placeholder(R.drawable.place_holder_image)).into(mImageItem)
        }
    }
}