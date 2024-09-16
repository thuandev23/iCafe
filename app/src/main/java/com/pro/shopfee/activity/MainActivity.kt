package com.pro.shopfee.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.MyViewPagerAdapter
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity() {

    private var mBottomNavigationView: BottomNavigationView? = null
    var viewPager2: ViewPager2? = null
    private var layoutCartBottom: RelativeLayout? = null
    private var tvCountItem: TextView? = null
    private var tvDrinksName: TextView? = null
    private var tvAmount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        initUi()
        mBottomNavigationView = findViewById(R.id.bottom_navigation)
        viewPager2 = findViewById(R.id.viewpager_2)
        viewPager2?.isUserInputEnabled = false
        val myViewPagerAdapter = MyViewPagerAdapter(this)
        viewPager2?.adapter = myViewPagerAdapter
        viewPager2?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> mBottomNavigationView?.menu?.findItem(R.id.nav_home)?.isChecked = true
                    1 -> mBottomNavigationView?.menu?.findItem(R.id.nav_history)?.isChecked = true
                    2 -> mBottomNavigationView?.menu?.findItem(R.id.nav_account)?.isChecked = true
                }
            }
        })
        mBottomNavigationView?.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager2?.currentItem = 0
                }
                R.id.nav_history -> {
                    viewPager2?.currentItem = 1
                }
                R.id.nav_account -> {
                    viewPager2?.currentItem = 2
                }
            }
            true
        }
        displayLayoutCartBottom()
    }

    private fun initUi() {
        layoutCartBottom = findViewById(R.id.layout_cart_bottom)
        tvCountItem = findViewById(R.id.tv_count_item)
        tvDrinksName = findViewById(R.id.tv_drinks_name)
        tvAmount = findViewById(R.id.tv_amount)
    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
            .title(getString(R.string.app_name))
            .content(getString(R.string.msg_exit_app))
            .positiveText(getString(R.string.action_ok))
            .onPositive { _: MaterialDialog?, _: DialogAction? -> finish() }
            .negativeText(getString(R.string.action_cancel))
            .cancelable(false)
            .show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDisplayCartEvent(event: DisplayCartEvent?) {
        displayLayoutCartBottom()
    }

    private fun displayLayoutCartBottom() {
        val listDrink = getInstance(this)!!
            .drinkDAO()!!.listDrinkCart
        if (listDrink == null || listDrink.isEmpty()) {
            layoutCartBottom!!.visibility = View.GONE
        } else {
            layoutCartBottom!!.visibility = View.VISIBLE
            val strCountItem = listDrink.size.toString() + " " + getString(R.string.label_item)
            tvCountItem!!.text = strCountItem
            var strDrinksName = ""
            for (drink in listDrink) {
                strDrinksName += if (isEmpty(strDrinksName)) {
                    drink.name
                } else {
                    ", " + drink.name
                }
            }
            if (isEmpty(strDrinksName)) {
                tvDrinksName!!.visibility = View.GONE
            } else {
                tvDrinksName!!.visibility = View.VISIBLE
                tvDrinksName!!.text = strDrinksName
            }
            var amount = 0
            for (drink in listDrink) {
                amount += drink.totalPrice
            }
            val strAmount = amount.toString() + Constant.CURRENCY
            tvAmount!!.text = strAmount
        }
        layoutCartBottom!!.setOnClickListener {
            startActivity(
                this,
                CartActivity::class.java
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}