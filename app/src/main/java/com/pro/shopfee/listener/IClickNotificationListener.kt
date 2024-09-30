package com.pro.shopfee.listener

import com.pro.shopfee.model.Notification

interface IClickNotificationListener {
    fun onClickNotificationItem(notification: Notification)
}