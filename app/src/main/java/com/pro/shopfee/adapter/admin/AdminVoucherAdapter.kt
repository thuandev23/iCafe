package com.pro.shopfee.adapter.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.admin.AdminVoucherAdapter.AdminVoucherViewHolder
import com.pro.shopfee.listener.IOnAdminManagerVoucherListener
import com.pro.shopfee.model.Voucher
import com.pro.shopfee.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminVoucherAdapter(
    private val mListVoucher: List<Voucher>?,
    private val mListener: IOnAdminManagerVoucherListener
) : RecyclerView.Adapter<AdminVoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminVoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_voucher, parent, false)
        return AdminVoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminVoucherViewHolder, position: Int) {
        val voucher = mListVoucher!![position]
        holder.tvTitle.text = voucher.title
        holder.tvMinimum.text = voucher.minimumText
        val expiredDateFormatted = voucher.expiredDate!!.toLong()
        holder.tvDateExpired.text = ("HSD:" + DateTimeUtils.convertTimeStampToDate(expiredDateFormatted)) ?: ""
        holder.imgEdit.setOnClickListener { mListener.onClickUpdateVoucher(voucher) }
        holder.imgDelete.setOnClickListener { mListener.onClickDeleteVoucher(voucher) }
    }

    override fun getItemCount(): Int {
        return mListVoucher?.size ?: 0
    }

    class AdminVoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView
        val tvMinimum: TextView
        val imgEdit: ImageView
        val imgDelete: ImageView
        val tvDateExpired: TextView

        init {
            tvTitle = itemView.findViewById(R.id.tv_title)
            tvMinimum = itemView.findViewById(R.id.tv_minimum)
            imgEdit = itemView.findViewById(R.id.img_edit)
            tvDateExpired = itemView.findViewById(R.id.tv_date_expired)
            imgDelete = itemView.findViewById(R.id.img_delete)
        }
    }
}