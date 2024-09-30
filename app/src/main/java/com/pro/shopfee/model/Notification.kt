package com.pro.shopfee.model

import java.io.Serializable

class Notification : Serializable {
    var title: String? = null
    var body: String? = null
    var isRead: Boolean? = null
    var timestamp: String? = null
    var notificationId: String? = null

    constructor()
    constructor(
        body: String?,
        title: String?,
        isRead: Boolean?,
        timestamp: String?,
        notificationId: String?
    ) {
        this.body = body
        this.isRead = isRead
        this.timestamp = timestamp
        this.title = title
        this.notificationId = notificationId
    }
}
