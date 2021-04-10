package com.seahahn.routinemaker.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.User
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MypageActivity : User() {

    private val TAG = this::class.java.simpleName
    private lateinit var service : RetrofitService

    var mbsNum : Int = 0
    lateinit var photoUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // 레트로핏 통신 연결
        service = initRetrofit()

        val tbtitle = findViewById<TextView>(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.mypage) // 툴바 제목에 들어갈 텍스트
        initToolbar(tbtitle, titleText, 1) // 툴바 세팅하기

        val id = getUserId(applicationContext)
        val nick = findViewById<TextView>(R.id.nick)
        val level = findViewById<TextView>(R.id.level)
        val title = findViewById<TextView>(R.id.title)
        val intro = findViewById<TextView>(R.id.intro)
        val membership = findViewById<TextView>(R.id.membership)
        val photo = findViewById<ImageView>(R.id.photo)
        mypageInfo(service, id, nick, level, title, intro, membership, photo)

        val logout = findViewById<TextView>(R.id.logout) // 로그아웃 텍스트 버튼
        val exit = findViewById<TextView>(R.id.exit) // 회원 탈퇴 텍스트 버튼
        logout.setOnClickListener(BtnOnClickListener())
        exit.setOnClickListener(BtnOnClickListener())
    }

    private fun mypageInfo(service : RetrofitService, id : Int, nick : TextView, lv : TextView, title : TextView, intro : TextView, mbs : TextView, photo : ImageView) {
        service.mypageInfo(id).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "마이페이지 정보 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "마이페이지 정보 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())

                nick.text = gson.get("nick").asString
                lv.text = (gson.get("lv").asInt).toString()
                title.text = gson.get("title").asString
//                        val cc = gson.get("cc").asInt
                intro.text = gson.get("intro").asString

                mbsNum = gson.get("mbs").asInt
                if(mbsNum == 0) {
                    mbs.text = getString(R.string.mbsBasic)
                } else if(mbsNum == 1) {
                    mbs.text = getString(R.string.mbsPremium)
                }

                photoUrl = gson.get("photo").asString
                Glide.with(applicationContext).load(photoUrl)
                    .placeholder(R.drawable.warning)
                    .error(R.drawable.warning)
                    .into(photo)
            }
        })
    }

    // 수정하기, 비밀번호 변경하기, 로그아웃, 회원탈퇴 텍스트 버튼 눌렀을 때의 동작 모음
    inner class BtnOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.logout -> {
                    Firebase.auth.signOut() // 구글 로그아웃
                    mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                    mOAuthLoginInstance.logout(this@MypageActivity) // 네이버 로그인 해제. 로그아웃임.

                    UserInfo.clearUser(this@MypageActivity) // 기기(SharedPref)에 저장되어 있던 사용자 정보 삭제
                    Log.d(TAG, "로그아웃")
                    toast("로그아웃 되었습니다.")
                    startActivity<LoginActivity>()
                }
                R.id.exit -> {
                    // 구글 연동 해제
                    if(Firebase.auth.currentUser != null) {
                        val googleUser = Firebase.auth.currentUser
                        googleUser.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User account deleted.")
                            }
                        }
                    }

                    // 네이버 연동 해제. 네트워크 사용으로 인해 다른 스레드로 작동시킴
                    mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                    doAsync {
                        val isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(this@MypageActivity)
                        Log.d(TAG, "네이버 로그아웃 : $isSuccessDeleteToken")
                    }

                    UserInfo.clearUser(this@MypageActivity)
                    Log.d(TAG, "연동 해제")
                    toast("회원 탈퇴 되었습니다.")
                    startActivity<LoginActivity>()
                }
            }
        }
    }
}