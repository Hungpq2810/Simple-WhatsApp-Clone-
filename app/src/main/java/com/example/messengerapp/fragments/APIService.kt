package com.example.messengerapp.fragments

import com.example.messengerapp.Notifications.MyResponse
import com.example.messengerapp.Notifications.Sender
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {

    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): retrofit2.Call<MyResponse>
}