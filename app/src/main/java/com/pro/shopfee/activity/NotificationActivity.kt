package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.NotificationAdapter
import com.pro.shopfee.listener.IClickNotificationListener
import com.pro.shopfee.model.Notification
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction

class NotificationActivity : BaseActivity() {

    private var mListNotification: MutableList<Notification>? = null
    private var mNotificationAdapter: NotificationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initView()
        loadNotifications()
    }


    private fun initView() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.notification)
        val rcvData = findViewById<RecyclerView>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvData.layoutManager = linearLayoutManager
        mListNotification = ArrayList()
        mNotificationAdapter =
            NotificationAdapter(mListNotification, object : IClickNotificationListener {
                override fun onClickNotificationItem(notification: Notification) {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val notificationId = notification.notificationId

                    if (notificationId != null) {
                        val databaseReference = FirebaseDatabase.getInstance()
                            .getReference("notifications/tokens/$currentUserId/notification/$notificationId")

                        databaseReference.child("isRead").setValue(true)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    loadNotifications()
                                    Toast.makeText(
                                        this@NotificationActivity,
                                        "Notification marked as read",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@NotificationActivity,
                                        "Failed to mark notification as read",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    }
                }
            })
        rcvData.adapter = mNotificationAdapter
    }


    private fun resetListDrink() {
        if (mListNotification != null) {
            mListNotification!!.clear()
        } else {
            mListNotification = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("notifications/tokens/$currentUserId/notification")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                resetListDrink()
                for (notificationSnapshot in dataSnapshot.children) {
                    val notificationId = notificationSnapshot.key // Get notification ID
                    val notification = notificationSnapshot.getValue(Notification::class.java)
                    if (notification != null) {
                        notification.notificationId =notificationId
                        mListNotification?.add(notification)
                    }
                }
                if (mListNotification.isNullOrEmpty()) {
                    findViewById<TextView>(R.id.tv_no_notifications).visibility = android.view.View.VISIBLE
                    findViewById<RecyclerView>(R.id.rcv_data).visibility = android.view.View.GONE
                } else {
                    findViewById<TextView>(R.id.tv_no_notifications).visibility = android.view.View.GONE
                    findViewById<RecyclerView>(R.id.rcv_data).visibility = android.view.View.VISIBLE
                }
                mNotificationAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@NotificationActivity,
                    "Failed to load notifications: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}