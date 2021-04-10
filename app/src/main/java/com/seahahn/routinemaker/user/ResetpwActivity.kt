package com.seahahn.routinemaker.user

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.User
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class ResetpwActivity : User() {
    private val TAG = this::class.java.simpleName
    private lateinit var service : RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpw)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 이메일 값 및 비번, 비번 확인 입력값 가져오기
        val email = intent.getStringExtra("email")
        val pwInput = findViewById<TextInputEditText>(R.id.pwInput)
        val pw = pwInput.text
        val pwcInput = findViewById<TextInputEditText>(R.id.pwcInput)
        val pwc = pwcInput.text

        // 화면 상단 화살표. 로그인 화면으로 되돌아감
        val goback = findViewById<ImageView>(R.id.goback)
        goback.setOnClickListener {
            startActivity<LoginActivity>()
        }

        // 비밀번호 재설정 버튼
        val resetpwBtn = findViewById<AppCompatButton>(R.id.resetpwBtn)
        resetpwBtn.setOnClickListener {
            if(formCheck(pw.toString(), pwc.toString())) {
                resetPwOk(service, email.toString(), pw.toString(), pwc.toString())
            }
        }
    }

    // 비밀번호 변경 통신 요청
    private fun resetPwOk(service : RetrofitService, email : String, pw : String, pwc : String) {
        service.resetPwOk(email, pw, pwc).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "비번 재설정 요청 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "비번 재설정 요청 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when (result) {
                    true -> {
                        Toast.makeText(this@ResetpwActivity, msg, Toast.LENGTH_SHORT).show()
                        startActivity<LoginActivity>()
                    }
                    else -> {
                        Toast.makeText(this@ResetpwActivity, "에러 : 비밀번호 변경 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // 레트로핏 객체 생성 및 API 연결
//    private fun initRetrofit() {
//        retrofit = RetrofitClient.getInstance()
//        service = retrofit.create(RetrofitService::class.java)
//    }

    // 비밀번호 입력칸 입력 여부 및 형식 검증
    private fun formCheck(pw: String, pwc: String): Boolean {
        if(pw.isEmpty()) {
            Toast.makeText(this@ResetpwActivity, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@!%*?&])[A-Za-z\\d$@!%*?&]{8,16}", pw)) {
            Toast.makeText(this@ResetpwActivity, "비밀번호 형식을 맞춰주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(pwc.isEmpty()) {
            Toast.makeText(this@ResetpwActivity, "비밀번호를 한번 더 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(pw != pwc) {
            Toast.makeText(this@ResetpwActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}