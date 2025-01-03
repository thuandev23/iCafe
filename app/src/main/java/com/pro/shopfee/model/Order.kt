package com.pro.shopfee.model

import com.pro.shopfee.utils.StringUtil.isEmpty
import java.io.Serializable

class Order : Serializable {
    var id: Long = 0
    var userId: String? = null
    var userEmail: String? = null
    var dateTime: String? = null
    var drinks: List<DrinkOrder>? = null
    var price = 0
    var voucher = 0
    var total = 0
    var paymentMethod: String? = null
    var status = 0
    var cancelReason: String? = null
    var rate = 0.0
    var review: String? = null
    var address: Address? = null
    var latitude = 0.0
    var longitude = 0.0
    var fee = 0
    val listDrinksName: String
        get() {
            if (drinks == null || drinks!!.isEmpty()) return ""
            var result = ""
            for (drinkOrder in drinks!!) {
                result += if (isEmpty(result)) {
                    drinkOrder.name
                } else {
                    ", " + drinkOrder.name
                }
            }
            return result
        }

    companion object {
        const val STATUS_CANCEL_OR_ACCEPT = 0
        const val STATUS_NEW = 1
        const val STATUS_DOING = 2
        const val STATUS_ARRIVED = 3
        const val STATUS_COMPLETE = 4
        const val STATUS_CANCEL = 5
    }
}