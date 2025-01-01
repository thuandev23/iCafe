package com.pro.shopfee.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.event.OrderSuccessEvent
import com.pro.shopfee.model.Order
import com.pro.shopfee.notification.Notification
import com.pro.shopfee.notification.NotificationApi
import com.pro.shopfee.notification.NotificationData
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : BaseActivity() {

    private var mOrderBooking: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        loadDataIntent()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ createOrderFirebase() }, 2000)
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        mOrderBooking = bundle[Constant.ORDER_OBJECT] as Order?
    }

    private fun createOrderFirebase() {
        MyApplication[this].getOrderDatabaseReference()?.child(mOrderBooking!!.id.toString())
            ?.setValue(mOrderBooking) { _: DatabaseError?, _: DatabaseReference? ->
                // Clear the cart and notify listeners
                getInstance(this)!!.drinkDAO()!!.deleteAllDrink()
                EventBus.getDefault().post(DisplayCartEvent())
                EventBus.getDefault().post(OrderSuccessEvent())

                // Redirect to receipt page
                val bundle = Bundle().apply {
                    putLong(Constant.ORDER_ID, mOrderBooking!!.id)
                }
                startActivity(this@PaymentActivity, ReceiptOrderActivity::class.java, bundle)
                sendNotificationToAll(mOrderBooking!!.address!!.phone.toString())
                finish()
            }
    }

    private fun sendNotificationToAll(strNameOrder: String) {

        getAllDeviceTokens { tokens ->
            val userTokensMap = mutableMapOf<String, MutableList<String>>()

            tokens.forEach { (userId, token) ->
                userTokensMap.getOrPut(userId) { mutableListOf() }.add(token)
            }

            userTokensMap.forEach { (userId, userTokens) ->
                userTokens.forEach { token ->
                    sendNotificationToToken(token, strNameOrder, userId)
                }
            }
        }

    }

    private fun sendNotificationToToken(token: String, strNameOrder: String, userId: String) {
        val data = hashMapOf(
            "title" to (" 🎉 " + "New Order"), "body" to "Have a new order from $strNameOrder"
        )
        val notification = Notification(
            message = NotificationData(
                token = token, data = data
            )
        )

        NotificationApi.create().sendNotification(notification)
            .enqueue(object : Callback<Notification> {
                override fun onResponse(
                    call: Call<Notification>, response: Response<Notification>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@PaymentActivity,
                            "Notification sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        val database = FirebaseDatabase.getInstance().getReference("notifications")
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())

                        val notificationData = mapOf(
                            "title" to data["title"],
                            "body" to data["body"],
                            "isRead" to false,
                            "timestamp" to currentDate  // Store date as a formatted string
                        )

                        database.child("tokens").child(userId).child("notification").push()
                            .setValue(notificationData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@PaymentActivity,
                                        "Notification saved for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@PaymentActivity,
                                        "Error saving notification for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    } else {
                        Toast.makeText(
                            this@PaymentActivity, "Notification failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    Toast.makeText(
                        this@PaymentActivity, "Failed to send notification", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getAllDeviceTokens(onComplete: (List<Pair<String, String>>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("notifications").child("tokens")
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokens = task.result.children.mapNotNull { snapshot ->
                    val email = snapshot.child("email").getValue(String::class.java)
                    val userId = snapshot.child("userId").getValue(String::class.java)
                    val token = snapshot.child("token").getValue(String::class.java)

                    if (email != null && email.endsWith(Constant.ADMIN_EMAIL_FORMAT) && userId != null && token != null) {
                        Pair(userId, token)
                    } else {
                        null
                    }
                }
                onComplete(tokens)
            } else {
                Toast.makeText(this, "Error retrieving tokens", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
