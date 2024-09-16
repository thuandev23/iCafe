package com.pro.shopfee.model

import java.io.Serializable

class Address : Serializable {
    var id: Long = 0
    var name: String? = null
    var phone: String? = null
    var address: String? = null
    var userEmail: String? = null
    var isSelected = false

    constructor()
    constructor(id: Long, name: String?, phone: String?, address: String?, userEmail: String?) {
        this.id = id
        this.name = name
        this.phone = phone
        this.address = address
        this.userEmail = userEmail
    }
}