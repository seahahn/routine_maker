package com.seahahn.routinemaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.user.LoginActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity

class TestActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    lateinit var mOAuthLoginInstance : OAuthLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        var tv1 = findViewById(R.id.tv1) as TextView
        var tv2 = findViewById(R.id.tv2) as TextView
        var tv3 = findViewById(R.id.tv3) as TextView
        var tv4 = findViewById(R.id.tv4) as TextView
        var tv5 = findViewById(R.id.tv5) as TextView
        var tv6 = findViewById(R.id.tv6) as TextView
        var tv7 = findViewById(R.id.tv7) as TextView
        var tv8 = findViewById(R.id.tv8) as TextView
        var tv9 = findViewById(R.id.tv9) as TextView
        var tv10 = findViewById(R.id.tv10) as TextView
//        tv1.setText(Integer.toString(intent.getIntExtra("id", 0)))
        tv1.setText(intent.getStringExtra("id"))
        tv2.setText(intent.getStringExtra("email"))
        tv3.setText(intent.getStringExtra("nick"))
        tv4.setText(Integer.toString(intent.getIntExtra("lv", 0)))
        tv5.setText(intent.getStringExtra("title"))
        tv6.setText(Integer.toString(intent.getIntExtra("cc", 0)))
        tv7.setText(intent.getStringExtra("intro"))
        tv8.setText(intent.getStringExtra("photo"))
        tv9.setText(intent.getStringExtra("indate"))
        tv10.setText(intent.getStringExtra("inway"))

        var logoutBtn = findViewById<Button>(R.id.logout)
        logoutBtn.setOnClickListener {
            mOAuthLoginInstance = OAuthLogin.getInstance()
            signOut()
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
//        mOAuthLoginInstance.logout(this@MainActivity)
        // 네이버 연동 해제. 네트워크 사용으로 인해 다른 스레드로 작동시킴
        doAsync {
            var isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(this@TestActivity)
            Log.d(TAG, "네이버 로그아웃 : "+isSuccessDeleteToken)
        }

        UserInfo.clearUser(this)

        Log.d(TAG, "구글 로그아웃")

        startActivity<LoginActivity>()
    }
}