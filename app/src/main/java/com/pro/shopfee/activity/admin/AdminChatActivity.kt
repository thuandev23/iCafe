package com.pro.shopfee.activity.admin


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.adapter.ChatAdapter
import com.pro.shopfee.adapter.admin.AdminChatAdapter
import com.pro.shopfee.model.Chat

class AdminChatActivity : BaseActivity() {
    private companion object {
        private const val TAG = "AdminChatActivity"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private var myId: String = ""
    private lateinit var chatsArrayList: ArrayList<Chat>
    private lateinit var adapterChat: AdminChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_customer)
        initToolbar()
        initUi()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { view: View? -> onBackPressed() }
        tvToolbarTitle.text = getString(R.string.msg_customer)
    }

    private fun initUi() {
        firebaseAuth = FirebaseAuth.getInstance()
        myId = firebaseAuth.uid!!
        Log.d(TAG, "initUi: myId: $myId")
        chatsArrayList = ArrayList()
        loadListMessage()
        val etSearch = findViewById<EditText>(R.id.edt_search_name_customer)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    val query = s.toString()
                    adapterChat.filter.filter(query)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun loadListMessage() {
        chatsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("chats")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                for (ds in snapshot.children) {
                    val chatKey = "${ds.key}"
                    Log.d(TAG, "onDataChange: chatKey: $chatKey")
                    if (chatKey.contains(myId)) {
                        val chat = ds.getValue(Chat::class.java)
                        chat!!.chatKey = chatKey
                        chatsArrayList.add(chat)
                    } else {
                        Log.d(TAG, "onDataChange: Not contain , Skipped")
                    }
                }
                val noDataTextView = findViewById<TextView>(R.id.tv_no_data)
                val recyclerView = findViewById<RecyclerView>(R.id.rcv_data_message)

                if (chatsArrayList.isEmpty()) {
                    noDataTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    noDataTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    adapterChat = AdminChatAdapter(this@AdminChatActivity, chatsArrayList)
                    recyclerView.layoutManager = LinearLayoutManager(this@AdminChatActivity)
                    recyclerView.adapter = adapterChat
                    setupSwipeToDelete()
                    sort()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
            }
        })
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // We're not supporting drag & drop, so return false
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Get the position of the swiped item
                val position = viewHolder.adapterPosition
                val chat = chatsArrayList[position]

                // Delete chat by chatKey
                deleteChatByChatKey(chat.chatKey, position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val textSize = 24f
                val textMargin = 50f
                val textColor = Color.WHITE

                // Tạo Paint để vẽ chữ
                val paint = Paint()
                paint.color = textColor
                paint.textSize = textSize
                paint.isAntiAlias = true

                // Đo kích thước của chữ để căn giữa theo chiều dọc
                val text = "Delete"
                val textBounds = Rect()
                paint.getTextBounds(text, 0, text.length, textBounds)
                val textHeight = textBounds.height()

                // Đặt nền đỏ khi trượt
                if (dX > 0) { // Trượt sang phải
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)

                    // Tính vị trí cho chữ
                    val textLeft = itemView.left + textMargin
                    val textBottom = itemView.top + (itemView.height + textHeight) / 2
                    c.drawText(text, textLeft, textBottom.toFloat(), paint)

                } else if (dX < 0) { // Trượt sang trái
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    // Tính vị trí cho chữ
                    val textLeft = itemView.right - textMargin - paint.measureText(text)
                    val textBottom = itemView.top + (itemView.height + textHeight) / 2
                    c.drawText(text, textLeft, textBottom.toFloat(), paint)
                } else { // Không trượt
                    background.setBounds(0, 0, 0, 0)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }

        // Attach the ItemTouchHelper to the RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.rcv_data_message))
    }

    private fun deleteChatByChatKey(chatKey: String, position: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("chats").child(chatKey)
        showProgressDialog(true)
        ref.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Remove the item from the list and notify the adapter
                chatsArrayList.removeAt(position)
                adapterChat.notifyItemRemoved(position)
                adapterChat.notifyItemRangeChanged(position, chatsArrayList.size)
                Log.d(TAG, "Chat deleted successfully")
                showProgressDialog(false)
            } else {
                Log.e(TAG, "Failed to delete chat: ${task.exception?.message}")
                adapterChat.notifyItemChanged(position)
                showProgressDialog(false)
            }
        }
    }

    private fun sort() {
        Handler().postDelayed({
            chatsArrayList.sortByDescending { chat -> chat.timestamp }
            adapterChat.notifyDataSetChanged()
        }, 500)
    }

}