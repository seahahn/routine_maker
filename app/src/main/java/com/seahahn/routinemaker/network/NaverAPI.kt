package com.seahahn.routinemaker.network

import android.content.Context
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverAPI {
    @GET("/v1/nid/me")
    fun getNaverUserInfo(
        @Header("Authorization") accessToken : String
    ): Call<JsonObject>

    companion object {
        const val BASE_URL = "https://openapi.naver.com/"
        const val CLIENT_ID = "Eqnzx3WTgu0UF1OckiJC"
        const val CLIENT_SECRET = "1yO6vhssgd"
        const val NAVER_CLIENT_NAME = "루틴 메이커"
        var signout : Boolean = false

        fun create() : NaverAPI {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("X-Naver-Client-Id", CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NaverAPI::class.java)
        }
    }
}