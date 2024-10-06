package com.pro.shopfee.widget

import android.widget.Filter
import com.pro.shopfee.adapter.admin.AdminChatAdapter
import com.pro.shopfee.model.Chat


class FilterChats: Filter {
    private val adapterChats: AdminChatAdapter
    private val filterList: ArrayList<Chat>
    constructor(adapterChats: AdminChatAdapter, filterList: ArrayList<Chat>) {
        this.adapterChats = adapterChats
        this.filterList = filterList
    }
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence ?= constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()){
            constraint = constraint.toString().toLowerCase()
            val filteredChats = ArrayList<Chat>()
            for (chat in filterList.indices){
                if(filterList[chat].username.toLowerCase().contains(constraint)){
                    filteredChats.add(filterList[chat])
                }
            }
            results.count = filteredChats.size
            results.values = filteredChats
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterChats.chatList = results?.values as ArrayList<Chat>
        adapterChats.notifyDataSetChanged()
    }
}