package com.seahahn.routinemaker.util

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.main.MainActivity
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.group.GroupListActivity
import com.seahahn.routinemaker.user.LoginActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class User  : Util() {

    private val TAG = this::class.java.simpleName
    lateinit var mOAuthLoginInstance : OAuthLogin

    // 로그인 통신 요청
    open fun login(service : RetrofitService, email : String, pw : String) {
        service.login(email, pw).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "로그인 실패 : {$t}")
//                startActivity<LoginActivity>()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "로그인 요청 응답 수신 성공")
                try {
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    val msg = gson.get("msg").asString
                    val result = gson.get("result").asBoolean
                    when (result) {
                        true -> {
                            toast(msg)
                            Log.d(TAG, response.body().toString())
                            val id = gson.get("id").asInt
                            val email = gson.get("email").asString
                            val nick = gson.get("nick").asString
                            val lv = gson.get("lv").asInt
                            val title = gson.get("title").asString
                            val cc = gson.get("cc").asInt
                            val intro = gson.get("intro").asString
                            val mbs = gson.get("mbs").asInt
                            val inway = gson.get("inway").asString
                            val photo = gson.get("photo").asString
                            startActivity<MainActivity>(
                                "id" to id,
                                "email" to email,
                                "nick" to nick,
                                "lv" to lv,
                                "title" to title,
                                "cc" to cc,
                                "intro" to intro,
                                "mbs" to mbs,
                                "inway" to inway,
                                "photo" to photo
                            )
                            UserInfo.setUserId(applicationContext, id)
                            UserInfo.setUserEmail(applicationContext, email)
                            UserInfo.setUserNick(applicationContext, nick)
                            UserInfo.setUserMbs(applicationContext, mbs)
                            UserInfo.setUserPhoto(applicationContext, photo)
                            UserInfo.setUserPass(applicationContext, pw)
                            UserInfo.setUserInway(applicationContext, inway)
                        }
                        false -> {
                            when(TAG) {
                                "SplashActivity" -> startActivity<LoginActivity>()
                                "LoginActivity" -> toast(msg)
                            }
                        }
                    }
                } catch (e : JsonSyntaxException) {
                    when(TAG) {
                        "SplashActivity" -> startActivity<LoginActivity>()
                        "LoginActivity" -> {}
                    }
                }
            }
        })
    }

    // 회원 탈퇴 요청
    fun userExit(service : RetrofitService, id : Int) {
        service.userExit(id).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "회원 탈퇴 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "회원 탈퇴 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())
                val result = gson.get("result").asBoolean
                val msg = gson.get("msg").asString
                when(result) {
                    true -> {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

                        // 구글 연동 해제
                        if(Firebase.auth.currentUser != null) {
                            val googleUser = Firebase.auth.currentUser
                            googleUser!!.delete().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "User account deleted.")
                                }
                            }
                        }

                        // 네이버 연동 해제. 네트워크 사용으로 인해 다른 스레드로 작동시킴
                        mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                        doAsync {
                            val isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(applicationContext)
                            Log.d(TAG, "네이버 로그아웃 : $isSuccessDeleteToken")
                        }

                        UserInfo.clearUser(applicationContext)
                        finish()
                        startActivity<LoginActivity>()
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}