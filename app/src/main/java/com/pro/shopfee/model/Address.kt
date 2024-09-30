package com.pro.shopfee.model

import java.io.Serializable
class Address(
    var id: Long = 0,
    var name: String? = null,
    var phone: String? = null,
    var address: String? = null,
    var userEmail: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var isSelected: Boolean = false
) : Serializable
