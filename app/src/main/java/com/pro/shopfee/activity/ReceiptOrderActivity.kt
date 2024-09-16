package com.pro.shopfee.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.DrinkOrderAdapter
import com.pro.shopfee.model.Order
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.DateTimeUtils.convertTimeStampToDate
import com.pro.shopfee.utils.GlobalFunction.startActivity

class ReceiptOrderActivity : BaseActivity() {

    private var tvIdTransaction: TextView? = null
    private var tvDateTime: TextView? = null
    private var rcvDrinks: RecyclerView? = null
    private var tvPrice: TextView? = null
    private var tvVoucher: TextView? = null
    private var tvTotal: TextView? = null
    private var tvPaymentMethod: TextView? = null
    private var tvName: TextView? = null
    private var tvPhone: TextView? = null
    private var tvAddress: TextView? = null
    private var tvTrackingOrder: TextView? = null
    private var orderId: Long = 0
    private var mOrder: Order? = null
    private var mValueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_order)
        loadDataIntent()
        initToolbar()
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
        tvToolbarTitle.text = getString(R.string.label_receipt_order)
    }

    private fun initUi() {
        tvIdTransaction = findViewById(R.id.tv_id_transaction)
        tvDateTime = findViewById(R.id.tv_date_time)
        rcvDrinks = findViewById(R.id.rcv_drinks)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvDrinks?.layoutManager = linearLayoutManager
        tvPrice = findViewById(R.id.tv_price)
        tvVoucher = findViewById(R.id.tv_voucher)
        tvTotal = findViewById(R.id.tv_total)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)
        tvTrackingOrder = findViewById(R.id.tv_tracking_order)
        tvName = findViewById(R.id.tv_name)
        tvPhone = findViewById(R.id.tv_phone)
        tvAddress = findViewById(R.id.tv_address)
    }

    private fun initListener() {
        tvTrackingOrder!!.setOnClickListener {
            if (mOrder == null) return@setOnClickListener
            val bundle = Bundle()
            bundle.putLong(Constant.ORDER_ID, mOrder!!.id)
            startActivity(
                this@ReceiptOrderActivity,
                TrackingOrderActivity::class.java, bundle
            )
            finish()
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
        tvIdTransaction!!.text = mOrder!!.id.toString()
        tvDateTime!!.text = convertTimeStampToDate(mOrder!!.dateTime!!.toLong())
        val strPrice = mOrder!!.price.toString() + Constant.CURRENCY
        tvPrice!!.text = strPrice
        val strVoucher = "-" + mOrder!!.voucher + Constant.CURRENCY
        tvVoucher!!.text = strVoucher
        val strTotal = mOrder!!.total.toString() + Constant.CURRENCY
        tvTotal!!.text = strTotal
        tvPaymentMethod!!.text = mOrder!!.paymentMethod
        tvName!!.text = mOrder!!.address!!.name
        tvPhone!!.text = mOrder!!.address!!.phone
        tvAddress!!.text = mOrder!!.address!!.address
        val adapter = DrinkOrderAdapter(mOrder!!.drinks)
        rcvDrinks!!.adapter = adapter
        if (Order.STATUS_COMPLETE == mOrder!!.status) {
            tvTrackingOrder!!.visibility = View.GONE
        } else {
            tvTrackingOrder!!.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mValueEventListener?.let {
            MyApplication[this].getOrderDetailDatabaseReference(orderId)?.removeEventListener(it)
        }
    }
}