package com.pro.shopfee.adapter.admin

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.R
import com.pro.shopfee.model.OrderWeb
import com.pro.shopfee.utils.DateTimeUtils
import kotlinx.android.synthetic.main.item_order_web.view.img_drink
import kotlinx.android.synthetic.main.item_order_web.view.layout_payment
import kotlinx.android.synthetic.main.item_order_web.view.tv_action
import kotlinx.android.synthetic.main.item_order_web.view.tv_drinks_name
import kotlinx.android.synthetic.main.item_order_web.view.tv_order_id
import kotlinx.android.synthetic.main.item_order_web.view.tv_payment
import kotlinx.android.synthetic.main.item_order_web.view.tv_quantity
import kotlinx.android.synthetic.main.item_order_web.view.tv_success
import kotlinx.android.synthetic.main.item_order_web.view.tv_total
import java.text.DecimalFormat

class OrderWebAdapter(
    private val orders: List<OrderWeb>, private val databaseReference: DatabaseReference, private val context: Context
) : RecyclerView.Adapter<OrderWebAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_order_web, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(order: OrderWeb) {
            // Hiển thị thông tin cơ bản của đơn hàng
            view.tv_order_id.text =
                context.getString(R.string.table, order.tableNumber, order.orderId)
            view.tv_total.text = context.getString(R.string.gia, formatPrice(order.total))
            view.tv_drinks_name.text = context.getString(R.string.khachhang, order.name)
            view.tv_quantity.text = context.getString(R.string.soluong, order.cart.size.toString())
            view.tv_action.text = context.getString(R.string.chitiet)
            if (order.statusOrder) {
                view.tv_success.text = context.getString(R.string.hoanthanh)
                view.tv_success.setTextColor(view.context.resources.getColor(R.color.green))
                view.tv_success.setBackgroundResource(R.drawable.bg_white_corner_6_border_green)
                view.layout_payment.visibility = View.GONE
            } else {
                view.tv_success.text = context.getString(R.string.process)
                view.tv_success.setTextColor(view.context.resources.getColor(R.color.colorPrimary))
                view.tv_success.setBackgroundResource(R.drawable.bg_white_corner_6_border_gray)
                view.setOnClickListener {
                    Toast.makeText(
                        view.context,
                        "Đơn hàng bàn ${order.tableNumber} - ${order.orderId} của ${order.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (order.statusPayment) {
                view.tv_payment.text = context.getString(R.string.dathanhtoan)
                view.tv_payment.setTextColor(view.context.resources.getColor(R.color.green))
                view.tv_payment.setBackgroundResource(R.drawable.bg_white_corner_6_border_green)
            } else {
                view.tv_payment.text = context.getString(R.string.chuathanhtoan)
                view.tv_payment.setTextColor(view.context.resources.getColor(R.color.red))
                view.tv_payment.setBackgroundResource(R.drawable.bg_white_corner_6_border_red)

            }
            // Load hình ảnh đầu tiên từ danh sách sản phẩm (nếu có)
            if (order.cart.isNotEmpty()) {
                val firstItem = order.cart[0]
                Glide.with(view.context).load(firstItem.image).centerCrop()
                    .error(R.drawable.ic_broken_image).into(view.img_drink)
            }
            view.tv_action.setOnClickListener {
                showDrinksDetailsDialog(order)
            }
        }

        private fun showDrinksDetailsDialog(order: OrderWeb) {
            val drinkDetails = order.cart.joinToString("\n") { drink ->
                context.getString(R.string.soluong1, drink.name, drink.quantity.toString())
            }

            val spannable = SpannableStringBuilder()

            spannable.append("Tên người đặt: ")
            spannable.append(order.name, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append("\n")

            spannable.append("Số điện thoại: ")
            spannable.append(order.phone, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append("\n")

            spannable.append("Thanh toán: ")
            val paymentStatusColor = if (order.statusPayment) Color.GREEN else Color.RED
            spannable.append(
                if (order.statusPayment) "Đã thanh toán" else "Chưa thanh toán",
                ForegroundColorSpan(paymentStatusColor),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.append("\n")

            spannable.append("Thời gian: ")
            spannable.append(
                DateTimeUtils.convertTimeStampToDate(order.timestamp),
                ForegroundColorSpan(Color.BLACK),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.append("\n\n")
            spannable.append("Chi tiết đồ uống:\n")
            spannable.append(drinkDetails, ForegroundColorSpan(Color.DKGRAY), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)



            val dialog = androidx.appcompat.app.AlertDialog.Builder(view.context)
                .setTitle("Thông tin bàn số: ${order.tableNumber}").setMessage(spannable)
                .setPositiveButton("Đóng") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.setNegativeButton("Đã làm xong") { dialogInterface, _ ->
                    updateOrderStatus(order)
                    dialogInterface.dismiss()
                }.create()

            dialog.show()
        }

        private fun updateOrderStatus(order: OrderWeb) {
            if (!order.statusOrder) {
                order.statusOrder = true
                order.timestamp = System.currentTimeMillis()
                val orderReference = databaseReference.child(order.orderId)
                orderReference.child("statusOrder").setValue(true).addOnSuccessListener {
                        Toast.makeText(
                            view.context,
                            "Trạng thái đã được cập nhật thành hoàn thành!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            view.context, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    view.context, "Đơn hàng đã ở trạng thái hoàn thành.", Toast.LENGTH_SHORT
                ).show()
            }
        }}

    private fun formatPrice(price: Int): String {
        val formatter = DecimalFormat("#,###,###")
        return formatter.format(price)
    }
}