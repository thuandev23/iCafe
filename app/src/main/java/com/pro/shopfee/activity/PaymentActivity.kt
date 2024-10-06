package com.pro.shopfee.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.event.OrderSuccessEvent
import com.pro.shopfee.model.Order
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import org.greenrobot.eventbus.EventBus

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

                // Redirect to receipt page
                val bundle = Bundle().apply {
                    putLong(Constant.ORDER_ID, mOrderBooking!!.id)
                }
                startActivity(this@PaymentActivity, ReceiptOrderActivity::class.java, bundle)
                finish()
            }
    }


}
