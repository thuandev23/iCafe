package com.pro.shopfee.model

import com.google.gson.Gson

data class User(
    var email: String? = null,
    var password: String? = null,
    var username: String? = null,
    var phone: String? = null,
    var address: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var isAdmin: Boolean = false,
    var image: String? = null,
    var longitude: Double? = null,
    var latitude: Double? = null,
    var uid: String? = null,
    var online: String? = null,
    var typingTo: String? = null,
    var typing: String? = null,
    var block: String? = null,
    var onlineStatus: String? = null,
    var typingStatus: String? = null,
    var typingToStatus: String? = null
) {
    // Convert object to JSON
    fun toJSon(): String {
        return Gson().toJson(this)
    }

    // Copy function with optional parameters
    fun copyWith(
        email: String? = this.email,
        password: String? = this.password,
        username: String? = this.username,
        image: String? = this.image,
        phone: String? = this.phone,
        address: String? = this.address,
        gender: String? = this.gender,
        birthday: String? = this.birthday,
        isAdmin: Boolean = this.isAdmin,
        longitude: Double? = this.longitude,
        latitude: Double? = this.latitude,
        uid: String? = this.uid,
        online: String? = this.online,
        typingTo: String? = this.typingTo,
        typing: String? = this.typing,
        block: String? = this.block,
        onlineStatus: String? = this.onlineStatus,
        typingStatus: String? = this.typingStatus,
        typingToStatus: String? = this.typingToStatus
    ): User {
        return this.copy(
            email = email,
            password = password,
            username = username,
            image = image,
            phone = phone,
            address = address,
            gender = gender,
            birthday = birthday,
            isAdmin = isAdmin,
            longitude = longitude,
            latitude = latitude,
            uid = uid,
            online = online,
            typingTo = typingTo,
            typing = typing,
            block = block,
            onlineStatus = onlineStatus,
            typingStatus = typingStatus,
            typingToStatus = typingToStatus
        )
    }
}
