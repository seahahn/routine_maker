package com.seahahn.routinemaker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.user.LoginActivity
import com.seahahn.routinemaker.util.User
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : User() {
    private val TAG = this::class.java.simpleName
    private lateinit var service : RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        UserInfo.clearUser(this@SplashActivity) // 기기(SharedPref)에 저장되어 있던 사용자 정보 삭제
        // 레트로핏 통신 연결
        service = initRetrofit()

        // 사용자 정보 저장되어 있으면 자동 로그인하여 바로 메인으로, 아니라면 로그인 창으로 이동
        if(!UserInfo.getUserEmail(this).isBlank() || !UserInfo.getUserPass(this).isBlank()) {
            val email = UserInfo.getUserEmail(this)
            val pw = UserInfo.getUserPass(this)
            login(service, email, pw)
        } else {
            startActivity<LoginActivity>()
        }
    }
}