package com.pro.shopfee.adapter

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.pro.shopfee.R
import com.pro.shopfee.model.Chat
import com.pro.shopfee.utils.Constant.MESSAGE_TYPE_TEXT
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatAdapter : Adapter<ChatAdapter.HolderChat> {
    private val context: Context
    private val chatArrayList: ArrayList<Chat>

    companion object {
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_RIGHT = 1
        private const val TAG = "ChatAdapter"
    }

    private var firebaseAuth: FirebaseAuth

    constructor(context: Context, chatArrayList: ArrayList<Chat>) {
        this.context = context
        this.chatArrayList = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageTv: TextView = itemView.findViewById<TextView>(R.id.tv_chat_message)
        var timeTv: TextView = itemView.findViewById<TextView>(R.id.tv_chat_time)
        var messageIv: ImageView = itemView.findViewById<ImageView>(R.id.iv_chat_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        if (viewType == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false)
            return HolderChat(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false)
            return HolderChat(view)

        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val calender = Calendar.getInstance(Locale.ENGLISH)
        calender.timeInMillis = timestamp
        return DateFormat.format("dd/MM/yyyy hh:mm", calender).toString()
    }

    override fun getItemCount(): Int = chatArrayList.size

    override fun getItemViewType(position: Int): Int {
        return if (chatArrayList[position].fromUid == firebaseAuth.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val chat = chatArrayList[position]
        //get data
        val message = chat.message
        val timestamp = chat.timestamp
        val messageType = chat.messageType
        val formattedTime = formatTimestamp(timestamp)
        holder.timeTv.text = formattedTime
        if (messageType == MESSAGE_TYPE_TEXT) {
            //text message
            holder.messageTv.visibility = View.VISIBLE
            holder.messageIv.visibility = View.GONE
            holder.messageTv.text = message
        } else {
            //image message
            holder.messageTv.visibility = View.GONE
            holder.messageIv.visibility = View.VISIBLE
            try {
                Glide.with(context).load(message).placeholder(R.drawable.ic_broken_image)
                    .error(R.drawable.ic_broken_image).into(holder.messageIv)
            } catch (e: Exception) {
                Log.d(TAG, "onBindViewHolder: ", e)
            }
        }
    }
}