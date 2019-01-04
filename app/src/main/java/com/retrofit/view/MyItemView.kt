package com.retrofit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.retrofit.R

/**
 * Created by zq on 2018/8/6
 */
class MyItemView : RelativeLayout {
    private lateinit var ivIcon: ImageView
    private lateinit var tvContent: TextView
    private lateinit var tvDes: TextView
    private lateinit var ivEnter: ImageView
    private lateinit var line: View

    constructor(context: Context) : super(context, null, 0) {
        initView()
    }

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        initView()
        val array = context.obtainStyledAttributes(attrs, R.styleable.MyItemView)
        ivIcon.setImageDrawable(array.getDrawable(R.styleable.MyItemView_my_item_icon))
        tvContent.text = array.getText(R.styleable.MyItemView_my_item_content)
        tvDes.text = array.getText(R.styleable.MyItemView_my_item_des)
        val isShowRight = array.getBoolean(R.styleable.MyItemView_my_item_show_right, true)
        val isShowLine = array.getBoolean(R.styleable.MyItemView_my_item_show_line, true)
        ivEnter.visibility = if (isShowRight) {
            tvDes.setPadding(0,0,0,0)
            VISIBLE
        } else {
            tvDes.setPadding(0,0,0,0)
            GONE
        }
        line.visibility = if (isShowLine) VISIBLE else GONE
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private fun initView() {
        View.inflate(context, R.layout.layout_my_item, this)
        ivIcon = findViewById(R.id.iv_icon)
        tvContent = findViewById(R.id.tv_content)
        tvDes = findViewById(R.id.tv_des)
        ivEnter = findViewById(R.id.iv_enter)
        line = findViewById(R.id.line)
    }

    fun setDes(des: CharSequence){
        tvDes.text = des
    }
}