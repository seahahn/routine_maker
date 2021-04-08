package com.seahahn.routinemaker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitMethod
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.user.AutoLogin
import com.seahahn.routinemaker.user.LoginActivity
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SplashActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitMethod : RetrofitMethod
    private lateinit var service : RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 레트로핏 통신 연결
        initRetrofit()

        // 사용자 정보 저장되어 있으면 자동 로그인하여 바로 메인으로, 아니라면 로그인 창으로 이동
        if(!AutoLogin.getUserId(this).isBlank() || !AutoLogin.getUserPass(this).isBlank()) {
            val email = AutoLogin.getUserId(this)
            val pw = AutoLogin.getUserPass(this)
            login(service, email, pw)
        } else {
            startActivity<LoginActivity>()
        }
    }

    // 레트로핏 객체 생성 및 API 연결
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)
    }

    // 로그인 통신 요청
    private fun login(service : RetrofitService, email : String, pw : String) {
        service.login(email, pw).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "로그인 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "로그인 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when (result) {
                    true -> {
                        Toast.makeText(this@SplashActivity, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, response.body().toString())
                        val id = gson.get("id").asString
                        val email = gson.get("email").asString
                        val nick = gson.get("nick").asString
                        val lv = gson.get("lv").asInt
                        val title = gson.get("title").asString
                        val cc = gson.get("cc").asInt
                        val intro = gson.get("intro").asString
                        val photo = gson.get("photo").asString
                        val inway = gson.get("inway").asString
                        val created_at = gson.get("created_at").asString
                        startActivity<MainActivity>(
                            "id" to id,
                            "email" to email,
                            "nick" to nick,
                            "lv" to lv,
                            "title" to title,
                            "cc" to cc,
                            "intro" to intro,
                            "photo" to photo,
                            "created_at" to created_at,
                            "inway" to inway
                        )

                        AutoLogin.setUserId(this@SplashActivity, email)
                        AutoLogin.setUserPass(this@SplashActivity, pw)
                    }
                    false -> {
                        // 비번 변경으로 인해 로그인하지 못한 경우 다시 로그인하도록 함
                        // SNS 로그인이라면 토근 값으로 로그인하므로 이메일을 변경하거나 로그아웃한 경우가 아니라면 다시 로그인 창을 볼 일이 없음
                        startActivity<LoginActivity>()
                    }
                }
            }
        })
    }
}