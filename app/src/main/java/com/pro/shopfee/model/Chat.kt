package com.pro.shopfee.model

class Chat {
    var profileImageUrl= ""
    var username:String= ""
    var chatKey:String= ""
    var receiptUid :String = ""
    var messageId: String = ""
    var messageType: String = ""
    var message: String = ""
    var fromUid: String = ""
    var toUid: String = ""
    var timestamp: Long = 0
    var isRead: Boolean = false
    constructor()

    constructor(
        profileImageUrl: String,
        username: String,
        chatKey: String,
        receiptUid: String,
        messageId: String,
        messageType: String,
        message: String,
        fromUid: String,
        toUid: String,
        timestamp: Long,
        isRead: Boolean
    ) {
        this.profileImageUrl = profileImageUrl
        this.username = username
        this.chatKey = chatKey
        this.receiptUid = receiptUid
        this.messageId = messageId
        this.messageType = messageType
        this.message = message
        this.fromUid = fromUid
        this.toUid = toUid
        this.timestamp = timestamp
        this.isRead = isRead
    }
}
