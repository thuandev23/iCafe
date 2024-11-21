package com.pro.shopfee.adapter.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pro.shopfee.fragment.admin.tabOrderWeb.OrderDoneWebFragment
import com.pro.shopfee.fragment.admin.tabOrderWeb.OrderProcessWebFragment
import com.pro.shopfee.model.TabOrderWeb

class OrderPagerWebsiteAdapter(
    fragmentActivity: FragmentActivity,
    private val tabs: List<TabOrderWeb>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        return when (tabs[position].type) {
            TabOrderWeb.TAB_ORDER_PROCESS -> OrderProcessWebFragment()
            TabOrderWeb.TAB_ORDER_DONE -> OrderDoneWebFragment()
            else -> throw IllegalArgumentException("Unknown tab type")
        }
    }
}