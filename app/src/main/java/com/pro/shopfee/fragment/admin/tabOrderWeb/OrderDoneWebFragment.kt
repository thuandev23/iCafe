package com.pro.shopfee.fragment.admin.tabOrderWeb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.pro.shopfee.adapter.admin.OrderWebAdapter
import com.pro.shopfee.databinding.FragmentOrderDoneWebBinding
import com.pro.shopfee.model.OrderWeb

class OrderDoneWebFragment   : Fragment() {
    private var _binding: FragmentOrderDoneWebBinding? = null
    private val binding get() = _binding!!

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("ordersweb")
    private val orders: MutableList<OrderWeb> = mutableListOf()
    private lateinit var adapter: OrderWebAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDoneWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrderWebAdapter(orders, databaseReference, requireContext())
        binding.rcvData.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvData.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orders.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderId = orderSnapshot.key ?: continue
                    val order = orderSnapshot.getValue(OrderWeb::class.java)
                    // check order null show text no data

                    if (order != null && order.statusOrder) {
                        orders.add(order.copy(orderId = orderId))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
