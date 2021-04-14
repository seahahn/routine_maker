package com.seahahn.routinemaker.util

import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.MainActivity
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.SplashActivity
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.user.LoginActivity
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
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "로그인 요청 응답 수신 성공")
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
            }
        })
    }

    // 툴바 버튼 클릭 시 작동할 기능
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d(TAG, "onOptionsItemSelected")
//        when(item.itemId){
//            android.R.id.home->{ // 툴바 좌측 버튼
//                if(homeBtn == R.drawable.hbgmenu) { // 햄버거 메뉴 버튼일 경우
////                    drawerLayout.openDrawer(GravityCompat.START) // 네비게이션 드로어 열기
//                } else if(homeBtn == R.drawable.backward_arrow) { // 좌향 화살표일 경우
//                    Log.d(TAG, "뒤로 가기")
//                    finish() // 액티비티 종료하기
//                }
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

}