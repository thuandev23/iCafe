package com.pro.shopfee.fragment.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.R
import com.pro.shopfee.activity.admin.AdminNotificationActivity
import com.pro.shopfee.adapter.OrderPagerAdapter
import com.pro.shopfee.adapter.admin.OrderPagerWebsiteAdapter
import com.pro.shopfee.model.TabOrder
import com.pro.shopfee.model.TabOrderWeb
import com.pro.shopfee.utils.GlobalFunction
import java.util.ArrayList
import java.util.Locale

class AdminOrderWebsiteFragment : Fragment() {
    private var mView: View? = null
    private var viewPagerOrder: ViewPager2? = null
    private var tabOrder: TabLayout? = null
    private var notificationCount: TextView? = null
    private var imgNotification: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_admin_order_website, container, false)
        initUi()
        displayTabsOrder()
        getUnreadNotificationCount()
        initListeners()
        return mView}
    private fun initUi() {
        viewPagerOrder = mView!!.findViewById(R.id.view_pager_order_web)
        viewPagerOrder?.isUserInputEnabled = false
        tabOrder = mView!!.findViewById(R.id.tab_order_web)
        notificationCount = mView!!.findViewById(R.id.tv_notification_count_web)
        imgNotification = mView!!.findViewById(R.id.img_notification_web)
    }

    private fun initListeners() {
        imgNotification!!.setOnClickListener {
            GlobalFunction.startActivity(requireActivity(), AdminNotificationActivity::class.java)
        }
    }

    private fun displayTabsOrder() {
        val list: MutableList<TabOrderWeb> = ArrayList()
        list.add(TabOrderWeb(TabOrderWeb.TAB_ORDER_PROCESS, getString(R.string.label_process)))
        list.add(TabOrderWeb(TabOrderWeb.TAB_ORDER_DONE, getString(R.string.label_done)))
        if (activity == null) return
        viewPagerOrder!!.offscreenPageLimit = list.size
        val adapter = OrderPagerWebsiteAdapter(requireActivity(), list)
        viewPagerOrder!!.adapter = adapter
        TabLayoutMediator(
            tabOrder!!, viewPagerOrder!!
        ) { tab: TabLayout.Tab, position: Int -> tab.text = list[position].name.toLowerCase(Locale.getDefault()) }
            .attach()

    }
    private fun getUnreadNotificationCount() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("notifications/tokens/$currentUserId/notification")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadCount = 0
                for (notificationSnapshot in snapshot.children) {
                    val isRead = notificationSnapshot.child("isRead").getValue(Boolean::class.java) ?: true
                    if (!isRead) {
                        unreadCount++
                    }
                }
                if (unreadCount > 0) {
                    notificationCount?.visibility = View.VISIBLE
                    notificationCount?.text = unreadCount.toString()
                } else {
                    notificationCount?.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu xảy ra
                Log.e("Firebase", "Error retrieving notifications: ${error.message}")
            }
        })
    }
}