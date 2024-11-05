package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pro.shopfee.R
import com.pro.shopfee.model.Rating
class ReviewsDrinkAdapter(private val listReviewsDrink: List<Rating>?) :
    RecyclerView.Adapter<ReviewsDrinkAdapter.ReviewsDrinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsDrinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reviews, parent, false)
        return ReviewsDrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewsDrinkViewHolder, position: Int) {
        val review = listReviewsDrink!![position]

        // Set user data
        holder.tvName.text = review.userName ?: "Anonymous"
        holder.ratingBar.rating = review.rate.toFloat()
        holder.tvReviews.text = review.review
        Glide.with(holder.itemView.context).load(review.userImage).placeholder(R.drawable.profile).circleCrop().into(holder.imgUser)
    }

    override fun getItemCount(): Int {
        return listReviewsDrink?.size ?: 0
    }

    class ReviewsDrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUser: ImageView = itemView.findViewById(R.id.img_reviewer_type)
        val tvName: TextView = itemView.findViewById(R.id.tv_reviewer_title)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingbar)
        val tvReviews: TextView = itemView.findViewById(R.id.tv_reviewer_condition)
    }
}

