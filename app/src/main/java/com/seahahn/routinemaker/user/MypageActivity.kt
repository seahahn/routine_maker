package com.seahahn.routinemaker.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MypageActivity : User() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        val title = findViewById<TextView>(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.mypage) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        val logout = findViewById<TextView>(R.id.logout)
        val exit = findViewById<TextView>(R.id.exit)
        logout.setOnClickListener(btnOnClickListener())
        exit.setOnClickListener(btnOnClickListener())
    }

    inner class btnOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.logout -> {
                    Firebase.auth.signOut() // 구글 로그아웃
                    mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                    mOAuthLoginInstance.logout(this@MypageActivity) // 네이버 로그인 해제. 로그아웃임.

                    AutoLogin.clearUser(this@MypageActivity) // 기기(SharedPref)에 저장되어 있던 사용자 정보 삭제
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
                        Log.d(TAG, "네이버 로그아웃 : "+isSuccessDeleteToken)
                    }

                    AutoLogin.clearUser(this@MypageActivity)
                    Log.d(TAG, "연동 해제")
                    toast("회원 탈퇴 되었습니다.")
                    startActivity<LoginActivity>()
                }
            }
        }
    }
}