package com.pro.shopfee.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ReviewsDrinkAdapter
import com.pro.shopfee.model.Rating
import com.pro.shopfee.model.RatingReview
import com.pro.shopfee.utils.Constant

class ReviewDrinksActivity : BaseActivity() {

    private lateinit var reviewsAdapter: ReviewsDrinkAdapter
    private lateinit var reviewsList: ArrayList<Rating>
    private var drinkId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_drinks)

        reviewsList = ArrayList()
        reviewsAdapter = ReviewsDrinkAdapter(reviewsList)
        loadDataIntent()
        val recyclerView = findViewById<RecyclerView>(R.id.rcv_reviews)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reviewsAdapter
        initToolbar()
        loadReviews()
        val tvRatingDrinks = findViewById<TextView>(R.id.tv_rating_drinks)
        tvRatingDrinks.setOnClickListener {
            val intent = Intent(this, RatingReviewActivity::class.java)

            val ratingReview = RatingReview(
                RatingReview.TYPE_RATING_REVIEW_DRINK, drinkId.toString()
            )
            intent.putExtra(Constant.RATING_REVIEW_OBJECT, ratingReview)

            startActivity(intent)
        }

    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)

        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.reviews_drink)
    }
    private fun loadDataIntent() {
        val ratingReview =
            intent.getSerializableExtra(Constant.RATING_REVIEW_OBJECT) as? RatingReview
        drinkId = ratingReview?.id
    }
    private fun loadReviews() {
        val ratingReview =
            intent.getSerializableExtra(Constant.RATING_REVIEW_OBJECT) as? RatingReview
        drinkId = ratingReview?.id

        drinkId?.let {
            val databaseReference = MyApplication[this].getRatingDrinkDatabaseReference(it)
            databaseReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewsList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val rating = dataSnapshot.getValue(Rating::class.java)
                        if (rating != null) {
                            reviewsList.add(rating)
                        }
                    }
                    if (reviewsList.isNullOrEmpty()) {
                        findViewById<TextView>(R.id.tv_no_rating).visibility = android.view.View.VISIBLE
                        findViewById<RecyclerView>(R.id.rcv_reviews).visibility = android.view.View.GONE
                    } else {
                        findViewById<TextView>(R.id.tv_no_rating).visibility = android.view.View.GONE
                        findViewById<RecyclerView>(R.id.rcv_reviews).visibility = android.view.View.VISIBLE
                    }
                    reviewsAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ReviewDrinksActivity,
                        "Failed to load reviews: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) ?: run {
                Toast.makeText(
                    this@ReviewDrinksActivity,
                    "Database reference not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: run {
            Toast.makeText(this@ReviewDrinksActivity, "Drink ID not found", Toast.LENGTH_SHORT)
                .show()
        }
    }

}
