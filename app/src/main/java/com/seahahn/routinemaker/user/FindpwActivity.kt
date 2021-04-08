package com.seahahn.routinemaker.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitMethod
import com.seahahn.routinemaker.network.RetrofitService
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import android.os.CountDownTimer
import com.seahahn.routinemaker.network.RetrofitClient


class FindpwActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    private var retrofitMethod = RetrofitMethod()
    private lateinit var service : RetrofitService

    private lateinit var countDownTimer: CountDownTimer
    private val MILLISINFUTURE = 300 * 1000 //총 시간 (300초 = 5분)
    private val COUNT_DOWN_INTERVAL = 1000 //onTick 메소드를 호출할 간격 (1초)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findpw)

        // 레트로핏 통신 연결
        initRetrofit()

        // 이메일 입력값 가져오기
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        var email = emailInput.text

        // 인증번호 텍스트, 입력값, 제한시간 가져오기
        val textView2 = findViewById<TextView>(R.id.textView2)
        val verifynumInput = findViewById<TextInputEditText>(R.id.verifynumInput)
        val time = findViewById<TextView>(R.id.time)
        var verifynum = verifynumInput.text

        // 다음 단계(비밀번호 재설정)로 이동 버튼
        val nextBtn = findViewById<Button>(R.id.nextBtn)
        nextBtn.setOnClickListener {
            // 인증 성공 시 비밀번호 재설정 화면으로 이동
            if(resetPwCheck(service, verifynum.toString())) {
                countDownTimer.cancel();
                startActivity<ResetpwActivity>("email" to email)
            }
        }

        // 인증 번호 메일 발송 버튼
        val sendbtn = findViewById<Button>(R.id.sendbtn)
        sendbtn.setOnClickListener {
            // 메일 발송 성공 시 인증 번호 입력란 및 하단 버튼 활성화
            if(resetPw(service, email.toString())) {
                // "전송" 버튼 텍스트 변경
                sendbtn.setText("재전송")

                // 인증 번호 입력란 보여주기
                textView2.setVisibility(View.VISIBLE)
                verifynumInput.setVisibility(View.VISIBLE)
                time.setVisibility(View.VISIBLE)

                // 하단 다음 버튼 활성화
                nextBtn.setEnabled(true)

                // 타이머 활성화
                countDownTimer = object : CountDownTimer(MILLISINFUTURE.toLong(), COUNT_DOWN_INTERVAL.toLong()) {
                    override fun onTick(millisUntilFinished: Long) { //(300초에서 1초 마다 계속 줄어듬)
                        val emailAuthCount = millisUntilFinished / 1000
                        Log.d(TAG, emailAuthCount.toString() + "")
                        if (emailAuthCount - emailAuthCount / 60 * 60 >= 10) { //초가 10보다 크면 그냥 출력
                            time.setText((emailAuthCount / 60).toString() + " : " + (emailAuthCount - emailAuthCount / 60 * 60))
                        } else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                            time.setText((emailAuthCount / 60).toString() + " : 0" + (emailAuthCount - emailAuthCount / 60 * 60))
                        }
                        //emailAuthCount은 종료까지 남은 시간임. 1분 = 60초 되므로,
                        // 분을 나타내기 위해서는 종료까지 남은 총 시간에 60을 나눠주면 그 몫이 분이 된다.
                        // 분을 제외하고 남은 초를 나타내기 위해서는, (총 남은 시간 - (분*60) = 남은 초) 로 하면 된다.
                    }

                    override fun onFinish() { // 시간이 다 되면 타이머 종료
                        // 하단 다음 버튼 비활성화
                        nextBtn.setEnabled(false)
                        cancel()
                    }
                }.start()
            }
        }

        // 화면 상단 화살표. 로그인 화면으로 되돌아감
        val goback = findViewById<ImageView>(R.id.goback)
        goback.setOnClickListener {
            startActivity<LoginActivity>()
        }
    }

    override fun onPause() {
        super.onPause()

        // 다른 액티비티로 이동 시 카운트다운 취소
        countDownTimer.cancel();
    }

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)
    }

    // 인증 번호 메일 발송 요청
    private fun resetPw(service : RetrofitService, email : String): Boolean {
        service.resetPw(email).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "인증 메일 전송 실패 : {$t}")
                return
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "인증 메일 전송 성공")
                var gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                var msg = gson.get("msg").asString
                var result = gson.get("result").asBoolean
                when (result) {
                    true -> {
                        Toast.makeText(this@FindpwActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                    false -> {
                        Toast.makeText(this@FindpwActivity, msg, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
        })
        return true
    }

    // 인증 요청
    private fun resetPwCheck(service : RetrofitService, hash : String): Boolean {
        service.resetPwCheck(hash).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "인증 요청 실패 : {$t}")
                return
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "인증 요청 성공")
                var gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                var msg = gson.get("msg").asString
                var result = gson.get("result").asBoolean
                when (result) {
                    true -> {
                        Toast.makeText(this@FindpwActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                    false -> {
                        Toast.makeText(this@FindpwActivity, msg, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
        })
        return true
    }
}