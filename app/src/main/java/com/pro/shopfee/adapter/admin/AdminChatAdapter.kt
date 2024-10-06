package com.pro.shopfee.adapter.admin

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.R
import com.pro.shopfee.activity.ChatActivity
import com.pro.shopfee.model.Chat
import com.pro.shopfee.utils.Constant.MESSAGE_TYPE_TEXT
import com.pro.shopfee.widget.FilterChats
import java.util.Calendar
import java.util.Locale

class AdminChatAdapter : RecyclerView.Adapter<AdminChatAdapter.HolderAdminChat>, Filterable {
    private var context: Context
    var chatList: ArrayList<Chat>
    private var firebaseAuth: FirebaseAuth
    private var myUid: String = ""
    private var filterList: ArrayList<Chat>
    private var filter: FilterChats? = null
    private var progressDialog: MaterialDialog? = null

    private companion object {
        const val CHAT_ADMIN_TAG = "CHAT_ADMIN_TAG_ADAPTER"
    }

    constructor(
        context: Context, chatList: ArrayList<Chat>
    ) {
        this.context = context
        this.chatList = chatList
        this.filterList = chatList

        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.uid!!
    }

    inner class HolderAdminChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileIv: ImageView = itemView.findViewById<ImageView>(R.id.img_customer_chat)
        var nameTv: TextView = itemView.findViewById<TextView>(R.id.tv_name_customer_chat)
        var lastMessageTv: TextView = itemView.findViewById<TextView>(R.id.tv_chat_customer_message)
        var timeTv: TextView = itemView.findViewById<TextView>(R.id.tv_chat_customer_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAdminChat {
        val view = LayoutInflater.from(context).inflate(R.layout.row_chats, parent, false)
        return HolderAdminChat(view)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: HolderAdminChat, position: Int) {
        val chat = chatList[position]
        loadLastMessage(chat, holder)
        holder.itemView.setOnClickListener {
            showProgressDialog(true)
            val receiptUid = chat.receiptUid
            if (receiptUid != null) {
                markMessageAsRead(chat.chatKey, chat.messageId)
                showProgressDialog(false)
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("receiptUid", receiptUid)
                context.startActivity(intent)

            }
        }
    }
    private fun markMessageAsRead(chatKey: String, messageId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("chats").child(chatKey).child(messageId)
        ref.child("isRead").setValue(true)
    }
    private fun loadLastMessage(chat: Chat, holder: HolderAdminChat) {
        val chatKey = chat.chatKey
        Log.d(CHAT_ADMIN_TAG, "loadLastMessage: $chatKey")

        val ref = FirebaseDatabase.getInstance().getReference("chats")
        ref.child(chatKey).limitToLast(1).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (chatSnapshot in snapshot.children) {
                        val fromUid = "${chatSnapshot.child("fromUid").value}"
                        val message = "${chatSnapshot.child("message").value}"
                        val timestamp =
                            chatSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val messageId = "${chatSnapshot.child("messageId").value}"
                        val messageType = "${chatSnapshot.child("messageType").value}"
                        val toUid = "${chatSnapshot.child("toUid").value}"
                        val isRead = chatSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                        val formattedTimestamp = formatTimestamp(timestamp)

                        chat.message = message
                        chat.messageId = messageId
                        chat.messageType = messageType
                        chat.fromUid = fromUid
                        chat.timestamp = timestamp
                        chat.toUid = toUid
                        holder.timeTv.text = formattedTimestamp
                        chat.isRead = isRead

                        if (chat.isRead) {
                            holder.lastMessageTv.setTypeface(null, Typeface.NORMAL)
                        } else {
                            holder.lastMessageTv.setTypeface(null, Typeface.BOLD)
                        }

                        if (messageType == MESSAGE_TYPE_TEXT) {
                            holder.lastMessageTv.text = message
                        } else {
                            holder.lastMessageTv.text = "Sent attachment..."
                        }
                    }
                    loadReceiptUserDetails(chat, holder)
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.d(CHAT_ADMIN_TAG, "loadLastMessage: ${error.message}")
                }
            })
    }

    private fun loadReceiptUserDetails(chat: Chat, holder: HolderAdminChat) {
        val fromUid = chat.fromUid
        val toUid = chat.toUid
        var receiptUid = ""
        if (fromUid == myUid) {
            receiptUid = toUid
        } else {
            receiptUid = fromUid
        }
        chat.receiptUid = receiptUid
        Log.d(CHAT_ADMIN_TAG, "loadReceiptDetails: $fromUid")
        Log.d(CHAT_ADMIN_TAG, "loadReceiptDetails: $toUid")
        Log.d(CHAT_ADMIN_TAG, "loadReceiptDetails: $receiptUid")
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(receiptUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = "${snapshot.child("username").value}"
                val image = "${snapshot.child("image").value}"
                chat.username = name
                chat.profileImageUrl = image

                holder.nameTv.text = name
                try {
                    Glide.with(context).load(image).placeholder(R.drawable.profile)
                        .error(R.drawable.profile).circleCrop().into(holder.profileIv)
                } catch (e: Exception) {
                    Log.d(CHAT_ADMIN_TAG, "onDataChange: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

    private fun formatTimestamp(timestamp: Long): String {
        val calender = Calendar.getInstance(Locale.ENGLISH)
        calender.timeInMillis = timestamp
        return DateFormat.format("hh:mm", calender).toString()
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterChats(this, filterList)
        }
        return filter!!
    }
    fun showProgressDialog(value: Boolean) {
        if (value) {
            if (progressDialog != null && !progressDialog!!.isShowing) {
                progressDialog!!.show()
                progressDialog!!.setCancelable(false)
            }
        } else {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }
}