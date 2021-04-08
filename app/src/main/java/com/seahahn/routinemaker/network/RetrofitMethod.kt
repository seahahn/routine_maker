package com.seahahn.routinemaker.network

import retrofit2.Retrofit

class RetrofitMethod {
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitMethod : RetrofitMethod
    private lateinit var service : RetrofitService

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)
    }
}