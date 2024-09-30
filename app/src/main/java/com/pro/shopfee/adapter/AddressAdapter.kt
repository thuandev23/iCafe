package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.AddressAdapter.AddressViewHolder
import com.pro.shopfee.model.Address

class AddressAdapter(
    private val listAddress: List<Address>?,
    private val iClickAddressListener: IClickAddressListener
) : RecyclerView.Adapter<AddressViewHolder>() {

    interface IClickAddressListener {
        fun onClickAddressItem(address: Address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = listAddress!![position]
        holder.tvName.text = address.name
        holder.tvPhone.text = address.phone
        holder.tvAddress.text = address.address
        holder.tvLatitude.text = address.latitude?.toString()
        holder.tvLongitude.text = address.longitude?.toString()

        if (address.isSelected) {
            holder.imgStatus.setImageResource(R.drawable.ic_item_selected)
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_item_unselect)
        }
        holder.layoutItem.setOnClickListener {
            iClickAddressListener.onClickAddressItem(address)
        }
    }

    override fun getItemCount(): Int {
        return listAddress?.size ?: 0
    }

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgStatus: ImageView
        val tvName: TextView
        val tvPhone: TextView
        val tvAddress: TextView
        val tvLatitude: TextView
        val tvLongitude: TextView
        val layoutItem: LinearLayout

        init {
            imgStatus = itemView.findViewById(R.id.img_status)
            tvName = itemView.findViewById(R.id.tv_name)
            tvPhone = itemView.findViewById(R.id.tv_phone)
            tvAddress = itemView.findViewById(R.id.tv_address)
            tvLatitude = itemView.findViewById(R.id.tv_latitude)
            tvLongitude = itemView.findViewById(R.id.tv_longitude)
            layoutItem = itemView.findViewById(R.id.layout_item)
        }
    }
}
