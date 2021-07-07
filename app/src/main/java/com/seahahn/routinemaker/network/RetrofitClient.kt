package com.seahahn.routinemaker.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var instance: Retrofit? = null
    private var fcmInstance: Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()
    // 서버 주소
    private const val BASE_URL = "http://15.165.168.238/" // AWS 서버(본진)
//    private const val BASE_URL = "http://10.0.2.2/" // 로컬 서버
    private const val FCM_URL = "https://fcm.googleapis.com/" // FCM REST API URL

    // SingleTon
    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        return instance!!
    }

    fun getFCMInstance(): Retrofit {
        if (fcmInstance == null) {
            fcmInstance = Retrofit.Builder()
                .baseUrl(FCM_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        return fcmInstance!!
    }
}