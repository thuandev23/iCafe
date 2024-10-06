package com.pro.shopfee.activity

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ChatAdapter
import com.pro.shopfee.model.Chat
import com.pro.shopfee.utils.Constant.MESSAGE_TYPE_IMAGE
import com.pro.shopfee.utils.Constant.MESSAGE_TYPE_TEXT
import java.util.Arrays

class ChatActivity : BaseActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var receiverUid: String = ""
    private var myId: String = ""
    private var chatPath = ""
    private var imageUri: Uri? = null
    private var tvNameChat: TextView? = null
    private var imageChat: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initUi()
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        myId = firebaseAuth.currentUser!!.uid
        receiverUid = intent.getStringExtra("receiptUid")!!
        chatPath = chatPath(receiverUid, myId)
        loadReceptionDetails()
        loadMessages()
    }

    private fun initUi() {
        tvNameChat = findViewById(R.id.tv_name_chat)
        imageChat = findViewById(R.id.img_user_chat)
        val btnSendChat = findViewById<ImageView>(R.id.btn_send_chat)
        val btnUploadImageChat = findViewById<ImageView>(R.id.btn_picture_chat)
        val btnBackChat = findViewById<ImageView>(R.id.img_toolbar_back_chat)
        btnBackChat.setOnClickListener {
            finish()
        }
        btnSendChat.setOnClickListener {
            validateData()
        }
        btnUploadImageChat.setOnClickListener {
            ImagePicker.with(this).crop().compress(1024).maxResultSize(1080, 1080).start()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            uploadImageToFirebase()
        }
    }

    private fun loadMessages() {
        val messageArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("chats")
        ref.child(chatPath).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageArrayList.clear()

                    for (chatSnapshot in snapshot.children) {
                        val chat: Chat? = chatSnapshot.getValue(Chat::class.java)
                        messageArrayList.add(chat!!)
                    }

                    if (messageArrayList.isEmpty()) {
                        Log.d(TAG, "onDataChange: No messages found")
                    } else {
                        Log.d(TAG, "onDataChange: ${messageArrayList.size} messages found")
                    }

                    // Set up RecyclerView and Adapter
                    val recyclerView = findViewById<RecyclerView>(R.id.rv_chat_messages)
                    val adapterChat = ChatAdapter(this@ChatActivity, messageArrayList)
                    recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
                    recyclerView.adapter = adapterChat

                    // Notify the adapter of data change
                    adapterChat.notifyDataSetChanged()

                    // Scroll to the last message
                    recyclerView.scrollToPosition(messageArrayList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "loadMessages: DatabaseError: ${error.message}")
                }
            })
    }


    private fun loadReceptionDetails() {
        Log.d(TAG, "loadReceptionDetails:")
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.orderByChild("uid").equalTo(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        try {
                            val name = "" + ds.child("username").value
                            val image = "" + ds.child("image").value
                            Log.d(TAG, "onDataChange: name: $name")
                            Log.d(TAG, "onDataChange: image: $image")
                            tvNameChat!!.text = name
                            try {
                                Glide.with(this@ChatActivity).load(image)
                                    .placeholder(R.drawable.profile).circleCrop().into(imageChat!!)
                            } catch (e: Exception) {
                                Log.d(TAG, "onDataChange: Exception: ${e.message}")
                                imageChat!!.setImageResource(R.drawable.profile)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "onDataChange: Exception: ${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun uploadImageToFirebase() {
        Log.d(TAG, "uploadImageToFirebase:")

        // show progress
        progressDialog.setMessage("Sending image...")
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val fileNameAndPath = "ChatImages/" + "post_$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath)
        storageReference.putFile(imageUri!!).addOnProgressListener {
                val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                progressDialog.setMessage("Uploading $progress%")
            }.addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val url = uriTask.result
                Log.d(TAG, "uploadImageToFirebase: url: $url")
                if (uriTask.isSuccessful) {
                    sendMessage(MESSAGE_TYPE_IMAGE, url.toString(), timestamp)
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
            }
    }

    private fun validateData() {
        val message = findViewById<TextView>(R.id.et_chat_message).text.toString().trim()
        val timestamp = System.currentTimeMillis()
        if (message.isEmpty()) {
            Toast.makeText(this, "Enter message to send ...", Toast.LENGTH_SHORT).show()
        } else {
            sendMessage(MESSAGE_TYPE_TEXT, message, timestamp)
        }
    }

    private fun sendMessage(messageType: String, message: String, timestamp: Long) {
        Log.d(TAG, "sendMessage: messageType: $messageType")
        Log.d(TAG, "sendMessage: message: $message")
        Log.d(TAG, "sendMessage: timestamp: $timestamp")
        //show progress
        progressDialog.setMessage("Sending message...")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("chats")
        val keyId = refChat.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["messageId"] = "$keyId"
        hashMap["messageType"] = messageType
        hashMap["message"] = message
        hashMap["fromUid"] = myId
        hashMap["toUid"] = receiverUid
        hashMap["timestamp"] = timestamp
        hashMap["isRead"] = false

        refChat.child(chatPath).child(keyId!!).setValue(hashMap).addOnSuccessListener {
                Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                findViewById<TextView>(R.id.et_chat_message).text = ""
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to send message due to ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun chatPath(receptionId: String, myId: String): String {
        val arr = arrayOf(receptionId, myId)
        Arrays.sort(arr)
        return arr[0] + "_" + arr[1]
    }
}