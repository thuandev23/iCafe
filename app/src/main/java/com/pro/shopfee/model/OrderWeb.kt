package com.pro.shopfee.model

data class OrderWeb(
    val orderId: String = "",
    val name: String = "",
    val phone: String = "",
    val tableNumber: String = "",
    val total : Int = 0,
    var statusOrder : Boolean = false,
    var statusPayment : Boolean = false,
    var timestamp: Long = 0L,
    val cart: List<CartItem> = listOf()
)

data class CartItem(
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val image: String = "",
    val description: String = "",
    val sale: Int = 0
)