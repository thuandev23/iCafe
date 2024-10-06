package com.pro.shopfee.activity

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import com.pro.shopfee.BuildConfig.GOOGLE_MAPS_API_KEY
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.DrinkOrderAdapter
import com.pro.shopfee.map.DirectionsResponse
import com.pro.shopfee.map.GoogleMapsApiService
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.RatingReview
import com.pro.shopfee.notification.Notification
import com.pro.shopfee.notification.NotificationApi
import com.pro.shopfee.notification.NotificationData
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.set

class TrackingOrderActivity : BaseActivity() {

    private var rcvDrinks: RecyclerView? = null
    private var layoutReceiptOrder: LinearLayout? = null
    private var dividerStep0: View? = null
    private var dividerStep1: View? = null
    private var dividerStep2: View? = null
    private var imgStep0: ImageView? = null
    private var imgStep1: ImageView? = null
    private var imgStep2: ImageView? = null
    private var imgStep3: ImageView? = null
    private var tvTakeOrder: TextView? = null
    private var tvTakeOrderMessage: TextView? = null
    private var orderId: Long = 0
    private var mOrder: Order? = null
    private var isOrderArrived = false
    private var mValueEventListener: ValueEventListener? = null
    private lateinit var googleMap: GoogleMap
    private var latitudeData: Double = 0.0
    private var longitudeData: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)
        loadDataIntent()
        initToolbar()
        initMap()
        initUi()
        initListener()
        loadOrderDetailFromFirebase()
    }


    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        orderId = bundle.getLong(Constant.ORDER_ID)
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.label_tracking_order)
    }

    private fun getAdminLocation(callback: (LatLng, String) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.orderByChild("admin").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (adminSnapshot in snapshot.children) {
                        val latitude = adminSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude =
                            adminSnapshot.child("longitude").getValue(Double::class.java)
                        val address = adminSnapshot.child("address").getValue(String::class.java)
                        if (latitude != null && longitude != null) {
                            callback(LatLng(latitude, longitude), address ?: "")
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi nếu có
                }
            })
    }

    private fun initMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map

            getAdminLocation { adminLocation, adminAddress ->
                drawRoute(adminLocation, adminAddress)
            }

        }
    }

    private fun drawRoute(originShop: LatLng, adminAddress: String) {
        val retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        val service = retrofit.create(GoogleMapsApiService::class.java)
        val apiKey = GOOGLE_MAPS_API_KEY
        service.getDirections(
            "${originShop.latitude},${originShop.longitude}",
            "${latitudeData},${longitudeData}",
            apiKey
        ).enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>, response: Response<DirectionsResponse>
            ) {
                val directionResponse = response.body()
                if (directionResponse != null && directionResponse.routes.isNotEmpty()) {
                    val polyline = directionResponse.routes[0].overview_polyline.points
                    val decodedPath = PolyUtil.decode(polyline)
                    googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    googleMap.addPolyline(
                        PolylineOptions().addAll(decodedPath).color(
                            ContextCompat.getColor(
                                this@TrackingOrderActivity, R.color.colorPrimary
                            )
                        ).width(10f)
                    )
                    googleMap.addMarker(
                        MarkerOptions().position(originShop).title("Shop")
                            .snippet(mOrder!!.address!!.address).icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    Bitmap.createScaledBitmap(
                                        BitmapFactory.decodeResource(
                                            resources, R.drawable.profile
                                        ), 80, 80, false
                                    )
                                )
                            )
                    )

                    googleMap.addMarker(
                        MarkerOptions().position(LatLng(latitudeData, longitudeData))
                            .title("Customer").snippet(adminAddress).icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    Bitmap.createScaledBitmap(
                                        BitmapFactory.decodeResource(
                                            resources, R.drawable.profile
                                        ), 80, 80, false
                                    )
                                )
                            )

                    )
                    zoomRoute(decodedPath)
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                showToastMessage("Failed to get directions")
            }
        })
    }

    private fun zoomRoute(route: List<LatLng>) {
        if (route.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLng in route) boundsBuilder.include(latLng)
        val bounds = boundsBuilder.build()
        val padding = 100
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    private fun initUi() {
        rcvDrinks = findViewById(R.id.rcv_drinks)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvDrinks?.layoutManager = linearLayoutManager
        layoutReceiptOrder = findViewById(R.id.layout_receipt_order)
        dividerStep0 = findViewById(R.id.divider_step_0)
        dividerStep1 = findViewById(R.id.divider_step_1)
        dividerStep2 = findViewById(R.id.divider_step_2)
        imgStep0 = findViewById(R.id.img_step_0)
        imgStep1 = findViewById(R.id.img_step_1)
        imgStep2 = findViewById(R.id.img_step_2)
        imgStep3 = findViewById(R.id.img_step_3)
        tvTakeOrder = findViewById(R.id.tv_take_order_user)
        tvTakeOrderMessage = findViewById(R.id.tv_take_order_message)
        val layoutBottomUser = findViewById<LinearLayout>(R.id.layout_bottom_user)
        if (user!!.isAdmin) {
            layoutBottomUser.visibility = View.GONE
        } else {
            layoutBottomUser.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        layoutReceiptOrder!!.setOnClickListener {
            if (mOrder == null) return@setOnClickListener
            val bundle = Bundle()
            bundle.putLong(Constant.ORDER_ID, mOrder!!.id)
            startActivity(
                this@TrackingOrderActivity, ReceiptOrderActivity::class.java, bundle
            )
            finish()
        }
        if (user!!.isAdmin) {
            imgStep0!!.setOnClickListener { updateStatusOrder(Order.STATUS_CANCEL_OR_ACCEPT) }
            imgStep1!!.setOnClickListener {
                showReceiveOrderDialog(orderId)
            }
            imgStep2!!.setOnClickListener { updateStatusOrder(Order.STATUS_DOING) }
            imgStep3!!.setOnClickListener { updateStatusOrder(Order.STATUS_ARRIVED) }
        } else {
            imgStep0!!.setOnClickListener(null)
            imgStep1!!.setOnClickListener(null)
            imgStep2!!.setOnClickListener(null)
            imgStep3!!.setOnClickListener(null)
        }
        tvTakeOrder!!.setOnClickListener {
            if (isOrderArrived) {
                updateStatusOrder(Order.STATUS_COMPLETE)
            }
        }
    }

    private fun showReceiveOrderDialog(orderId: Long) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.confirm_new_order))
        dialogBuilder.setMessage(getString(R.string.title_confirm_new_order))

        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            updateStatusOrder(Order.STATUS_NEW)
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            showCancelOrderReasonDialog(orderId)
            dialog.dismiss()
        }
        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun showCancelOrderReasonDialog(orderId: Long) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.title_cancel))

        // Tạo EditText để admin nhập lý do
        val input = EditText(this)
        input.hint = getString(R.string.hint_cancel_input)
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
            val reason = input.text.toString().trim()
            if (reason.isNotEmpty()) {
                cancelOrder(orderId, reason)
            } else {
                Toast.makeText(this, getString(R.string.enter_reasson), Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun cancelOrder(orderId: Long, reason: String) {
        val orderRef =
            FirebaseDatabase.getInstance().getReference("order").child(orderId.toString())
        val updates = mapOf(
            "status" to Order.STATUS_CANCEL, "cancelReason" to reason
        )


        orderRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finish()
                if (user!!.isAdmin) {
                sendNotificationCancelOrder(orderId.toString(),user!!.uid.toString())}
            } else {
                Toast.makeText(this, "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadOrderDetailFromFirebase() {
        showProgressDialog(true)
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressDialog(false)
                mOrder = snapshot.getValue(Order::class.java)

                if (mOrder == null) return
                initData()
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressDialog(false)
                showToastMessage(getString(R.string.msg_get_date_error))
            }
        }
        MyApplication[this].getOrderDetailDatabaseReference(orderId)
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun initData() {
        val adapter = DrinkOrderAdapter(mOrder!!.drinks)
        rcvDrinks!!.adapter = adapter
        latitudeData = mOrder!!.latitude
        longitudeData = mOrder!!.longitude
        when (mOrder!!.status) {
            Order.STATUS_CANCEL_OR_ACCEPT -> {
                imgStep0!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep0!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep1!!.setImageResource(R.drawable.ic_step_disable)
                dividerStep1!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                imgStep2!!.setImageResource(R.drawable.ic_step_disable)
                dividerStep2!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                imgStep3!!.setImageResource(R.drawable.ic_step_disable)
                isOrderArrived = false
                tvTakeOrder!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                tvTakeOrderMessage!!.visibility = View.GONE
            }

            Order.STATUS_NEW -> {
                imgStep0!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep0!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep1!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep1!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep2!!.setImageResource(R.drawable.ic_step_disable)
                dividerStep2!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                imgStep3!!.setImageResource(R.drawable.ic_step_disable)
                isOrderArrived = false
                tvTakeOrder!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                tvTakeOrderMessage!!.visibility = View.GONE

            }

            Order.STATUS_CANCEL -> {
                imgStep0!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep0!!.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                imgStep1!!.setImageResource(R.drawable.ic_cancel)
                dividerStep1!!.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                imgStep2!!.setImageResource(R.drawable.ic_step_disable)
                dividerStep2!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                imgStep3!!.setImageResource(R.drawable.ic_step_disable)
                isOrderArrived = false
                tvTakeOrder!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                tvTakeOrderMessage!!.visibility = View.GONE
            }

            Order.STATUS_DOING -> {
                imgStep0!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep0!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep1!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep1!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep2!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep2!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep3!!.setImageResource(R.drawable.ic_step_disable)
                isOrderArrived = false
                tvTakeOrder!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                tvTakeOrderMessage!!.visibility = View.GONE
            }

            Order.STATUS_ARRIVED -> {
                imgStep0!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep0!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep1!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep1!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep2!!.setImageResource(R.drawable.ic_step_enable)
                dividerStep2!!.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                imgStep3!!.setImageResource(R.drawable.ic_step_enable)
                isOrderArrived = true
                tvTakeOrder!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                tvTakeOrderMessage!!.visibility = View.VISIBLE
            }
        }
    }

    private fun updateStatusOrder(status: Int) {
        if (mOrder == null) return
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        MyApplication[this].getOrderDatabaseReference()?.child(mOrder!!.id.toString())
            ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                if (Order.STATUS_COMPLETE == status) {
                    val bundle = Bundle()
                    val ratingReview =
                        RatingReview(RatingReview.TYPE_RATING_REVIEW_ORDER, mOrder!!.id.toString())
                    bundle.putSerializable(Constant.RATING_REVIEW_OBJECT, ratingReview)
                    startActivity(
                        this@TrackingOrderActivity, RatingReviewActivity::class.java, bundle
                    )
                    finish()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mValueEventListener?.let {
            MyApplication[this].getOrderDetailDatabaseReference(orderId)?.removeEventListener(it)
        }
    }
    private fun sendNotificationCancelOrder(orderId: String, userId: String) {

        getDeviceTokenForUser(userId) { token ->
            if (token != null) {
                // Gửi thông báo đến token này
                    sendNotificationToToken(token, orderId, userId)
                }
            }
    }

    private fun sendNotificationToToken(token: String, orderId: String, userId: String) {
        val data = hashMapOf(
            "title" to (" 🎉 " + "Đơn hàng đã bị hủy"),
            "body" to "Đơn hàng $orderId đã bị hủy"
        )
        val notification = Notification(
            message = NotificationData(
                token = token,
                data = data
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
                            this@TrackingOrderActivity,
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
                                        this@TrackingOrderActivity,
                                        "Notification saved for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@TrackingOrderActivity,
                                        "Error saving notification for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    } else {
                        Toast.makeText(
                            this@TrackingOrderActivity,
                            "Notification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    Toast.makeText(
                        this@TrackingOrderActivity,
                        "Failed to send notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun getDeviceTokenForUser(userId: String, onComplete: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("notifications").child("tokens")
        database.child(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result.children.firstOrNull()?.child("token")?.getValue(String::class.java)
                onComplete(token)
            } else {
                Toast.makeText(this, "Error retrieving token for user", Toast.LENGTH_SHORT).show()
                onComplete(null)
            }
        }
    }


}