package com.pro.shopfee.model

import com.pro.shopfee.utils.Constant
import java.io.Serializable

class Voucher : Serializable {
    var id: Long = 0
    var discount = 0
    var minimum = 0
    var createdAt: Long = 0
    var expiredDate: Long = 0
    var isSelected = false

    constructor()
    constructor(id: Long, discount: Int, minimum: Int, expiredDate: Long, createdAt: Long) {
        this.id = id
        this.discount = discount
        this.minimum = minimum
        this.expiredDate = expiredDate
        this.createdAt = createdAt
    }

    val title: String
        get() = "Giảm giá $discount%"
    val minimumText: String
        get() = if (minimum > 0) {
            "Áp dụng cho đơn hàng tối thiểu " + minimum + Constant.CURRENCY
        } else "Áp dụng cho mọi đơn hàng"

    fun getCondition(amount: Int): String {
        if (minimum <= 0) return ""
        val condition = minimum - amount
        return if (condition > 0) {
            "Hãy mua thêm " + condition + Constant.CURRENCY + " để nhận được khuyến mại này"
        } else ""
    }

    fun isVoucherEnable(amount: Int): Boolean {
        if (minimum <= 0) return true
        val condition = minimum - amount
        return condition <= 0
    }

    fun getPriceDiscount(amount: Int): Int {
        return amount * discount / 100
    }
}