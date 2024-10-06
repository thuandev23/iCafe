package com.pro.shopfee.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.listener.IClickNotificationListener
import com.pro.shopfee.model.Notification

class NotificationAdapter(
    private val listNotification: MutableList<Notification>?, // Ensure list is mutable for updating
    private val iClickNotificationListener: IClickNotificationListener
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = listNotification!![position]

        // Set data into views
        holder.title.text = notification.title
        holder.body.text = notification.body
        holder.time.text = notification.timestamp

        if (notification.isRead == true) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            holder.img_notification.setColorFilter(Color.GRAY)
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.img_notification.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary))
        }

        holder.itemView.setOnClickListener {
            iClickNotificationListener.onClickNotificationItem(notification)
            notification.isRead = true
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return listNotification?.size ?: 0
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val body: TextView = itemView.findViewById(R.id.tv_body)
        val time: TextView = itemView.findViewById(R.id.tv_time)
        val img_notification: ImageView = itemView.findViewById(R.id.image_notification)
    }
}
