package com.pro.shopfee.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.ReceiptOrderActivity
import com.pro.shopfee.activity.TrackingOrderActivity
import com.pro.shopfee.adapter.OrderAdapter
import com.pro.shopfee.adapter.OrderAdapter.IClickOrderListener
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.TabOrder
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity

class OrderFragment : Fragment() {

    private var mView: View? = null
    private var orderTabType = 0
    private var listOrder: MutableList<Order>? = null
    private var orderAdapter: OrderAdapter? = null
    private var mOrderAllValueEventListener: ValueEventListener? = null
    private var mOrderValueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_order, container, false)
        loadDataArguments()
        initUi()
        if (user!!.isAdmin) {
            loadListOrderAllUsersFromFirebase()
        } else {
            loadListOrderFromFirebase()
        }
        return mView
    }

    private fun loadDataArguments() {
        val bundle = arguments ?: return
        orderTabType = bundle.getInt(Constant.ORDER_TAB_TYPE)
    }

    private fun initUi() {
        listOrder = ArrayList()
        val rcvOrder = mView!!.findViewById<RecyclerView>(R.id.rcv_order)
        val linearLayoutManager = LinearLayoutManager(activity)
        rcvOrder.layoutManager = linearLayoutManager
        orderAdapter = OrderAdapter(activity, listOrder, object : IClickOrderListener {
            override fun onClickTrackingOrder(orderId: Long) {
                val bundle = Bundle()
                bundle.putLong(Constant.ORDER_ID, orderId)
                startActivity(activity!!, TrackingOrderActivity::class.java, bundle)
            }

            override fun onClickReceiptOrder(order: Order?) {
                val bundle = Bundle()
                bundle.putLong(Constant.ORDER_ID, order!!.id)
                startActivity(activity!!, ReceiptOrderActivity::class.java, bundle)
            }
        })
        rcvOrder.adapter = orderAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListOrderAllUsersFromFirebase() {
        if (activity == null) return
        mOrderAllValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listOrder != null) {
                    listOrder!!.clear()
                } else {
                    listOrder = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(
                        Order::class.java
                    )
                    if (order != null) {
                        if (TabOrder.TAB_ORDER_PROCESS == orderTabType) {
                            if (Order.STATUS_COMPLETE != order.status) {
                                listOrder!!.add(0, order)
                            }
                        } else if (TabOrder.TAB_ORDER_DONE == orderTabType) {
                            if (Order.STATUS_COMPLETE == order.status) {
                                listOrder!!.add(0, order)
                            }
                        }
                    }
                }
                if (orderAdapter != null) orderAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[activity!!].getOrderDatabaseReference()
            ?.addValueEventListener(mOrderAllValueEventListener!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListOrderFromFirebase() {
        if (activity == null) return
        mOrderValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listOrder != null) {
                    listOrder!!.clear()
                } else {
                    listOrder = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(
                        Order::class.java
                    )
                    if (order != null) {
                        if (TabOrder.TAB_ORDER_PROCESS == orderTabType) {
                            if (Order.STATUS_COMPLETE != order.status) {
                                listOrder!!.add(0, order)
                            }
                        } else if (TabOrder.TAB_ORDER_DONE == orderTabType) {
                            if (Order.STATUS_COMPLETE == order.status) {
                                listOrder!!.add(0, order)
                            }
                        }
                    }
                }
                if (orderAdapter != null) orderAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[activity!!].getOrderDatabaseReference()
            ?.orderByChild("userEmail")
            ?.equalTo(user!!.email)
            ?.addValueEventListener(mOrderValueEventListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (orderAdapter != null) orderAdapter!!.release()
        if (activity != null && mOrderAllValueEventListener != null) {
            MyApplication[activity!!].getOrderDatabaseReference()
                ?.removeEventListener(mOrderAllValueEventListener!!)
        }
        if (activity != null && mOrderValueEventListener != null) {
            MyApplication[activity!!].getOrderDatabaseReference()
                ?.removeEventListener(mOrderValueEventListener!!)
        }
    }

    companion object {
        fun newInstance(type: Int): OrderFragment {
            val orderFragment = OrderFragment()
            val bundle = Bundle()
            bundle.putInt(Constant.ORDER_TAB_TYPE, type)
            orderFragment.arguments = bundle
            return orderFragment
        }
    }
}