package com.pro.shopfee.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.CartAdapter
import com.pro.shopfee.adapter.CartAdapter.IClickCartListener
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.*
import com.pro.shopfee.model.*
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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
    private var paymentMethodSelected: PaymentMethod? = null
    private var addressSelected: Address? = null
    private var voucherSelected: Voucher? = null

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
            orderBooking.status = Order.STATUS_NEW
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

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}