package com.pro.shopfee.notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationInterface {
    @POST("v1/projects/cafem-80e6f/messages:send")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    fun sendNotification(
        @Body message: Notification,
        @Header("Authorization") accessToken: String
        = "Bearer ${AccessToken().getAccessToken()}"
    ): Call<Notification>
}