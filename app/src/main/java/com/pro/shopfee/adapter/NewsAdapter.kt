package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pro.shopfee.R
import com.pro.shopfee.model.News
import com.pro.shopfee.model.Topping

class NewsAdapter (
    private val listNews: List<News>?
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    interface IClickToppingListener {
        fun onClickToppingItem(topping: Topping)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val new = listNews!![position]
        holder.tvTitle.text = new.title
        holder.tvDesc.text = new.description
        // Load image
        Glide.with(holder.itemView.context)
            .load(new.image)
            .into(holder.image_news)
        holder.time_news.text = new.timestamp
    }

    override fun getItemCount(): Int {
        return listNews?.size ?: 0
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_news)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_desc_news)
        val image_news: ImageView = itemView.findViewById(R.id.img_news)
        val time_news: TextView = itemView.findViewById(R.id.tv_time_news)
    }
}