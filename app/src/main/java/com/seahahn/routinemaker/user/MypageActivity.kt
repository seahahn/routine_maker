package com.seahahn.routinemaker.user

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
//import com.camerash.toggleedittextview.ToggleEditButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.ToggleEditButton
//import com.seahahn.routinemaker.user.MypageActivity.ToggleEditButton.Companion.EDIT_KEY
//import com.seahahn.routinemaker.user.MypageActivity.ToggleEditButton.Companion.SUPER_STATE_KEY
//import com.seahahn.routinemaker.util.ToggleEditButton
import com.seahahn.routinemaker.util.ToggleEditTextView
import com.seahahn.routinemaker.util.User
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserNick
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MypageActivity : User() {

    private val TAG = this::class.java.simpleName
    private lateinit var service : RetrofitService

    lateinit var nick : ToggleEditTextView
    lateinit var nickupdate : ToggleEditButton
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

        // 사용자 정보 불러오기
        val id = getUserId(applicationContext) // DB 내 사용자의 고유 번호
//        val nick = findViewById<TextView>(R.id.nick) // 사용자 닉네임
        val level = findViewById<TextView>(R.id.level) // 사용자 레벨
        val title = findViewById<TextView>(R.id.title) // 사용자의 칭호(타이틀)
        val intro = findViewById<TextView>(R.id.intro) // 사용자 자기소개글
        val membership = findViewById<TextView>(R.id.membership) // 사용자 멤버십
        val photo = findViewById<ImageView>(R.id.photo) // 사용자 프로필 사진

        d(TAG, "닉네임 : " + getUserNick(applicationContext))
        nick = findViewById(R.id.nick)
        mypageInfo(service, id, nick, level, title, intro, membership, photo) // 사용자 정보 불러오기

        // 사용자 정보 수정하기
//        val nickupdate = findViewById<TextView>(R.id.nickupdate) // 사용자 닉네임
        nickupdate = findViewById(R.id.nickupdate)
//        d(TAG, "nick.getText() : "+nick.getText())
//        nickupdate.bind(nick.getText(), nick)
        nickupdate.setOnClickListener(BtnOnClickListener())

        val titleupdate = findViewById<TextView>(R.id.titleupdate) // 사용자의 칭호(타이틀)
        val introupdate = findViewById<TextView>(R.id.introupdate) // 사용자 자기소개글
        val mbsApply = findViewById<TextView>(R.id.mbsApply) // 사용자 멤버십
        val photoUpdate = findViewById<ImageView>(R.id.photoUpdate) // 사용자 프로필 사진


        val logout = findViewById<TextView>(R.id.logout) // 로그아웃 텍스트 버튼
        val exit = findViewById<TextView>(R.id.exit) // 회원 탈퇴 텍스트 버튼
        logout.setOnClickListener(BtnOnClickListener())
        exit.setOnClickListener(BtnOnClickListener())
    }

    private fun checkNick(service : RetrofitService, nick : String): Boolean {
        // 서버와 통신하여 닉네임 중복 확인
        Log.d(TAG, "닉네임 체크 : $nick")
        val oriNick = getUserNick(applicationContext)
        var ret = false
        if(nick.isEmpty()) {
            Toast.makeText(this@MypageActivity, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return ret // 이메일을 입력하지 않은 경우 여기서 빠져나감. return 안하면 onResponse에서 null받았다고 하면서 에러 발생.
        } else if(nick == oriNick) {
            // 닉네임 수정 안하고 그대로 둔 경우
            ret = true
            return ret
        }
        service.checkNick(nick).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "닉네임 체크 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "닉네임 체크 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                Log.d(TAG, msg)
                Toast.makeText(this@MypageActivity, msg, Toast.LENGTH_SHORT).show()
                ret = result
            }
        })
        return ret
    }

    private fun mypageInfo(service : RetrofitService, id : Int, nick : ToggleEditTextView, lv : TextView, title : TextView, intro : TextView, mbs : TextView, photo : ImageView) {
        service.mypageInfo(id).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "마이페이지 정보 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "마이페이지 정보 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())

                nick.setText(gson.get("nick").asString)
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

                d(TAG, "nick.getText() : "+nick.getText())
                nickupdate.bind(nick.getText(), nick)
            }
        })
    }

    // 수정하기, 비밀번호 변경하기, 로그아웃, 회원탈퇴 텍스트 버튼 눌렀을 때의 동작 모음
    inner class BtnOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.nickupdate -> {
                    d(TAG, "nick.getText() : "+nick.getText())
                    nickupdate.bind(nick.getText(), nick)
                }
                R.id.titleupdate -> {}
                R.id.introupdate -> {}
                R.id.mbsApply -> {}
                R.id.photoUpdate -> {}
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