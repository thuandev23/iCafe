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
        MyApplication[this].getOrderDatabaseReference()
            ?.child(mOrderBooking!!.id.toString())
            ?.setValue(mOrderBooking) { _: DatabaseError?, _: DatabaseReference? ->
                // Clear the cart and notify listeners
                getInstance(this)!!
                    .drinkDAO()!!.deleteAllDrink()
                EventBus.getDefault().post(DisplayCartEvent())
                EventBus.getDefault().post(OrderSuccessEvent())

                // Send notification to admins
                sendNotificationToAdmins(mOrderBooking!!.userEmail.toString())

                // Redirect to receipt page
                val bundle = Bundle().apply {
                    putLong(Constant.ORDER_ID, mOrderBooking!!.id)
                }
                startActivity(this@PaymentActivity, ReceiptOrderActivity::class.java, bundle)
                finish()
            }
    }

    private fun sendNotificationToAdmins(userEmail: String) {
        getAllAdminTokens { tokens ->
            tokens.forEach { (adminId, token) ->
                sendNotificationToToken(token, userEmail, adminId)
            }
        }
    }

    private fun sendNotificationToToken(token: String, userEmail: String, adminId: String) {
        val notificationData = hashMapOf(
            "title" to (" 🎉 " + getString(R.string.icafe_new_order_title)),
            "body" to getString(R.string.icafe_new_order_desc, userEmail)
        )

        val notification = Notification(
            message = NotificationData(
                token = token,
                data = notificationData
            )
        )

        NotificationApi.create().sendNotification(notification).enqueue(
            object : Callback<Notification> {
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@PaymentActivity,
                            "Notification sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        saveNotificationToFirebase(adminId, notificationData, token)
                    } else {
                        Toast.makeText(
                            this@PaymentActivity,
                            "Notification failed to send",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Failed to send notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun saveNotificationToFirebase(adminId: String, notificationData: Map<String, String>, token: String) {
        val database = FirebaseDatabase.getInstance().getReference("notifications")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val notificationEntry = mapOf(
            "title" to notificationData["title"],
            "body" to notificationData["body"],
            "isRead" to false,
            "timestamp" to currentDate
        )

        database.child("tokens").child(adminId).child("notification").push()
            .setValue(notificationEntry)
            .addOnCompleteListener { task ->
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
    }

    private fun getAllAdminTokens(onComplete: (List<Pair<String, String>>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("notifications").child("tokens")
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokens = task.result.children.mapNotNull { snapshot ->
                    val adminId = snapshot.child("userId").getValue(String::class.java)
                    val token = snapshot.child("token").getValue(String::class.java)

                    // Check if the user is an admin
                    if (adminId != null && token != null && adminId.endsWith(Constant.ADMIN_EMAIL_FORMAT)) {
                        Pair(adminId, token)
                    } else {
                        null
                    }
                }
                onComplete(tokens)
            } else {
                Toast.makeText(this, "Error retrieving admin tokens", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
