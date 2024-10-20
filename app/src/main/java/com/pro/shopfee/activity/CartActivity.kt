package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pro.shopfee.R
import com.pro.shopfee.adapter.CartAdapter
import com.pro.shopfee.adapter.CartAdapter.IClickCartListener
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.*
import com.pro.shopfee.model.*
import com.pro.shopfee.notification.Notification
import com.pro.shopfee.notification.NotificationApi
import com.pro.shopfee.notification.NotificationData
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

class CartActivity : BaseActivity() {

    private var rcvCart: RecyclerView? = null
    private var layoutAddOrder: LinearLayout? = null
    private var layoutPaymentMethod: RelativeLayout? = null
    private var tvPaymentMethod: TextView? = null
    private var layoutAddress: RelativeLayout? = null
    private var tvAddress: TextView? = null
    private var layoutVoucher: RelativeLayout? = null
    private var tvVoucher: TextView? = null
    private var tvNameVoucher: TextView? = null
    private var tvPriceDrink: TextView? = null
    private var tvCountItem: TextView? = null
    private var tvAmount: TextView? = null
    private var tvPriceVoucher: TextView? = null
    private var tvCheckout: TextView? = null
    private var listDrinkCart: MutableList<Drink>? = null
    private var cartAdapter: CartAdapter? = null
    private var priceDrink = 0
    private var mAmount = 0
    private var fee = 0
    private var paymentMethodSelected: PaymentMethod? = null
    private var addressSelected: Address? = null
    private var latitudeAddress: Double? = 0.0
    private var longitudeAddress: Double? = 0.0
    private var voucherSelected: Voucher? = null
    private var userID = FirebaseAuth.getInstance().currentUser!!.uid
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        initToolbar()
        initUi()
        initListener()
        initData()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.label_cart)
    }

    private fun initUi() {
        rcvCart = findViewById(R.id.rcv_cart)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvCart?.layoutManager = linearLayoutManager
        layoutAddOrder = findViewById(R.id.layout_add_order)
        layoutPaymentMethod = findViewById(R.id.layout_payment_method)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)
        layoutAddress = findViewById(R.id.layout_address)
        tvAddress = findViewById(R.id.tv_address)
        layoutVoucher = findViewById(R.id.layout_voucher)
        tvVoucher = findViewById(R.id.tv_voucher)
        tvNameVoucher = findViewById(R.id.tv_name_voucher)
        tvCountItem = findViewById(R.id.tv_count_item)
        tvPriceDrink = findViewById(R.id.tv_price_drink)
        tvAmount = findViewById(R.id.tv_amount)
        tvPriceVoucher = findViewById(R.id.tv_price_voucher)
        tvCheckout = findViewById(R.id.tv_checkout)
    }

    private fun initListener() {
        layoutAddOrder!!.setOnClickListener { finish() }
        layoutPaymentMethod!!.setOnClickListener {
            val bundle = Bundle()
            if (paymentMethodSelected != null) {
                bundle.putInt(Constant.PAYMENT_METHOD_ID, paymentMethodSelected!!.id)
            }
            startActivity(this@CartActivity, PaymentMethodActivity::class.java, bundle)
        }
        layoutAddress!!.setOnClickListener {
            val bundle = Bundle()
            if (addressSelected != null) {
                bundle.putLong(Constant.ADDRESS_ID, addressSelected!!.id)
            }
            startActivity(this@CartActivity, AddressActivity::class.java, bundle)
        }
        layoutVoucher!!.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(Constant.AMOUNT_VALUE, priceDrink)
            if (voucherSelected != null) {
                bundle.putLong(Constant.VOUCHER_ID, voucherSelected!!.id)
            }
            startActivity(this@CartActivity, VoucherActivity::class.java, bundle)
        }
        tvCheckout!!.setOnClickListener {
            if (listDrinkCart == null || listDrinkCart!!.isEmpty()) return@setOnClickListener
            if (paymentMethodSelected == null) {
                showToastMessage(getString(R.string.label_choose_payment_method))
                return@setOnClickListener
            }
            if (addressSelected == null) {
                showToastMessage(getString(R.string.label_choose_address))
                return@setOnClickListener
            }
            val orderBooking = Order()
            orderBooking.id = System.currentTimeMillis()
            orderBooking.userId = userID
            orderBooking.userEmail = user!!.email
            orderBooking.dateTime = System.currentTimeMillis().toString()
            val drinks: MutableList<DrinkOrder> = ArrayList()
            for (drink in listDrinkCart!!) {
                drinks.add(
                    DrinkOrder(
                        drink.name, drink.option, drink.count,
                        drink.priceOneDrink, drink.image
                    )
                )
            }
            orderBooking.drinks = drinks
            orderBooking.price = priceDrink
            if (voucherSelected != null) {
                orderBooking.voucher = voucherSelected!!.getPriceDiscount(priceDrink)
            }
            orderBooking.total = mAmount
            orderBooking.paymentMethod = paymentMethodSelected!!.name
            orderBooking.address = addressSelected
            orderBooking.latitude = latitudeAddress!!
            orderBooking.longitude = longitudeAddress!!
            orderBooking.status = Order.STATUS_CANCEL_OR_ACCEPT
            orderBooking.cancelReason = ""
            orderBooking.fee = fee
            sendNotificationToAdmins(orderBooking.userEmail.toString())
            val bundle = Bundle()
            bundle.putSerializable(Constant.ORDER_OBJECT, orderBooking)
            startActivity(this@CartActivity, PaymentActivity::class.java, bundle)
        }
    }

    private fun initData() {
        listDrinkCart = ArrayList()
        listDrinkCart = getInstance(this)!!.drinkDAO()!!.listDrinkCart
        if (listDrinkCart == null || listDrinkCart!!.isEmpty()) {
            return
        }
        cartAdapter = CartAdapter(listDrinkCart, object : IClickCartListener {
            override fun onClickDeleteItem(drink: Drink?, position: Int) {
                getInstance(this@CartActivity)!!.drinkDAO()!!.deleteDrink(drink)
                listDrinkCart?.removeAt(position)
                cartAdapter!!.notifyItemRemoved(position)
                displayCountItemCart()
                calculateTotalPrice()
                EventBus.getDefault().post(DisplayCartEvent())
            }

            override fun onClickUpdateItem(drink: Drink?, position: Int) {
                getInstance(this@CartActivity)!!.drinkDAO()!!.updateDrink(drink)
                cartAdapter!!.notifyItemChanged(position)
                calculateTotalPrice()
                EventBus.getDefault().post(DisplayCartEvent())
            }

            override fun onClickEditItem(drink: Drink?) {
                val bundle = Bundle()
                bundle.putLong(Constant.DRINK_ID, drink!!.id)
                bundle.putSerializable(Constant.DRINK_OBJECT, drink)
                startActivity(this@CartActivity, DrinkDetailActivity::class.java, bundle)
            }
        })
        rcvCart!!.adapter = cartAdapter
        calculateTotalPrice()
        displayCountItemCart()
    }

    private fun displayCountItemCart() {
        val strCountItem = "(" + listDrinkCart!!.size + " " + getString(R.string.label_item) + ")"
        tvCountItem!!.text = strCountItem
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrice() {
        if (listDrinkCart == null || listDrinkCart!!.isEmpty()) {
            val strZero = 0.toString() + Constant.CURRENCY
            priceDrink = 0
            tvPriceDrink!!.text = strZero
            mAmount = 0
            tvAmount!!.text = strZero
            return
        }
        var totalPrice = 0
        for (drink in listDrinkCart!!) {
            totalPrice += drink.totalPrice
        }
        priceDrink = totalPrice
        val strPriceDrink = priceDrink.toString() + Constant.CURRENCY
        tvPriceDrink!!.text = strPriceDrink
        mAmount = totalPrice
        if (voucherSelected != null) {
            mAmount -= voucherSelected!!.getPriceDiscount(priceDrink)
        }
        // Calculate the distance between shop and user
        if(addressSelected!=null) {
            val shopLatitude = 10.878057
            val shopLongitude = 106.654845
            val distance = calculateDistance(
                shopLatitude,
                shopLongitude,
                latitudeAddress!!,
                longitudeAddress!!
            )
            // Calculate the delivery fee based on the distance
            val deliveryFee = calculateDeliveryFee(distance)
            val deliveryFeeTextView = findViewById<TextView>(R.id.tv_price_shipping)
            deliveryFeeTextView.text = "+$deliveryFee${Constant.CURRENCY}"
            fee = deliveryFee
            mAmount += deliveryFee
        }
        val strAmount = mAmount.toString() + Constant.CURRENCY
        tvAmount!!.text = strAmount
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPaymentMethodSelectedEvent(event: PaymentMethodSelectedEvent) {
        paymentMethodSelected = event.paymentMethod
        tvPaymentMethod!!.text = paymentMethodSelected!!.name
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddressSelectedEvent(event: AddressSelectedEvent) {
        addressSelected = event.address
        tvAddress!!.text = addressSelected!!.address
        latitudeAddress = event.lat
        longitudeAddress = event.lng
        calculateTotalPrice()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoucherSelectedEvent(event: VoucherSelectedEvent) {
        voucherSelected = event.voucher
        tvVoucher!!.text = voucherSelected!!.title
        tvNameVoucher!!.text = voucherSelected!!.title
        val strPriceVoucher = ("-" + voucherSelected!!.getPriceDiscount(priceDrink)
                + Constant.CURRENCY)
        tvPriceVoucher!!.text = strPriceVoucher
        calculateTotalPrice()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOrderSuccessEvent(event: OrderSuccessEvent?) {
        finish()
    }
    private fun sendNotificationToAdmins(name: String) {
        getAllDeviceTokens { tokens ->
            val userTokensMap = mutableMapOf<String, MutableList<String>>()

            tokens.forEach { (userId, token) ->
                userTokensMap.getOrPut(userId) { mutableListOf() }.add(token)
            }

            userTokensMap.forEach { (userId, userTokens) ->
                userTokens.forEach { token ->
                    sendNotificationToToken(token, name, userId)
                }
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
                            this@CartActivity,
                            "Notification sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        val database = FirebaseDatabase.getInstance().getReference("notifications")
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())

                        val notificationData = mapOf(
                            "title" to notificationData["title"],
                            "body" to notificationData["body"],
                            "isRead" to false,
                            "timestamp" to currentDate
                        )
                        database.child("tokens").child(adminId).child("notification").push()
                            .setValue(notificationData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@CartActivity,
                                        "Notification saved for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@CartActivity,
                                        "Error saving notification for token $token",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@CartActivity,
                            "Notification failed to send",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    Toast.makeText(
                        this@CartActivity,
                        "Failed to send notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }


    private fun getAllDeviceTokens(onComplete: (List<Pair<String, String>>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("notifications").child("tokens")
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokens = task.result.children.mapNotNull { snapshot ->
                    val email = snapshot.child("email").getValue(String::class.java)
                    val adminId = snapshot.child("userId").getValue(String::class.java)
                    val token = snapshot.child("token").getValue(String::class.java)

                    if (email != null && email.endsWith(Constant.ADMIN_EMAIL_FORMAT) && adminId != null && token != null) {
                        Pair(adminId, token)
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
    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Distance in kilometers
    }
    private fun calculateDeliveryFee(distance: Double): Int {
        val baseFee = 10 // Base fee in your currency (e.g., 10,000 VND)
        val feePerKm = 3  // Fee per kilometer (e.g., 3,000 VND per km)
        return baseFee + (distance * feePerKm).toInt()
    }

}