package com.seahahn.routinemaker.user

import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class SignUpActivity : User() {
    private val TAG = this::class.java.simpleName
//    private lateinit var service : RetrofitService

    companion object {
        var emailchecked = false
        var nickcheckd = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 레트로핏 통신 연결
        service = initRetrofit()
        Log.d(TAG, "service : $service")

        // 이메일, 비밀번호, 비밀번호 확인, 닉네임 입력값 가져오기
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val pwInput = findViewById<TextInputEditText>(R.id.pwInput)
        val pwconfirmInput = findViewById<TextInputEditText>(R.id.pwconfirmInput)
        val nickInput = findViewById<TextInputEditText>(R.id.nickInput)
        val email = emailInput.text
        val pw = pwInput.text
        val pwc = pwconfirmInput.text
        val nick = nickInput.text

        // 이메일 및 닉네임 중복 확인
        val checkEmail = findViewById<Button>(R.id.checkemail)
        val checkNick = findViewById<Button>(R.id.checknick)
        checkEmail.setOnClickListener {
            Log.d(TAG, "이메일 : $email")
            checkEmail(service, email.toString())
        }
        checkNick.setOnClickListener {
            checkNick(service, nick.toString())
        }

        // 약관 동의 체크박스 값 가져오기
        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        var check = checkBox.isChecked
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            check = isChecked
        }

        // 화면 상단 화살표. 로그인 화면으로 되돌아감
        val goback = findViewById<ImageView>(R.id.goback)
        goback.setOnClickListener {
            startActivity<LoginActivity>()
        }

        // 약관 동의 체크 부분. 이용약관, 개인정보처리방침 누르면 해당 링크로 이동하게 만듦
        val signupagree = findViewById<TextView>(R.id.signupagree)
        signupagree.movementMethod = LinkMovementMethod.getInstance()

        // 회원가입 버튼
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        signupBtn.setOnClickListener {

            val formcheck = formCheck(email.toString(), pw.toString(), pwc.toString(), nick.toString(), check)

            Log.d(TAG, "formcheck : $formcheck")
            Log.d(TAG, "emailchecked : $emailchecked")
            Log.d(TAG, "nickcheckd : $nickcheckd")

            if(!formcheck) {
            }
            else if(!emailchecked) {
                Toast.makeText(this@SignUpActivity, "이메일 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(!nickcheckd) {
                Toast.makeText(this@SignUpActivity, "닉네임 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(emailchecked && nickcheckd && formcheck) {
                doAsync {
                    signup(service, email.toString(), pw.toString(), nick.toString())
                }
//                showAlert("이메일 인증 필요", "서비스 이용을 위해서 이메일 인증을 해주세요!", "확인", "")
            }
        }
    }

    // 회원가입 통신 요청
    private fun signup(service : RetrofitService, email : String, pw : String, nick : String) {
        service.signup(email, pw, nick, "etc", 0).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "회원가입 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "회원가입 요청 응답 수신 성공 " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val status = gson.get("status").asInt
                val msg = gson.get("msg").asString
                when (status) {
                    200 -> {
                        Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
                        showAlert("이메일 인증 필요", "서비스 이용을 위해서 이메일 인증을 해주세요!")
                    }
//                    405 -> {
//                        Toast.makeText(this@SignUpActivity, "회원가입 실패 : 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
//                    }
                    500 -> {
                        Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun checkEmail(service : RetrofitService, email : String): Boolean {
        // 서버와 통신하여 이메일 중복 확인
        Log.d(TAG, "이메일 체크 : $email")
        if(email.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false // 이메일을 입력하지 않은 경우 여기서 빠져나감. return 안하면 onResponse에서 null받았다고 하면서 에러 발생.
        }
        service.checkEmail(email).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "이메일 체크 실패 : {$t}")
                emailchecked = false
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "이메일 체크 성공 " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                Log.d(TAG, msg)
                Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
                emailchecked = result
            }
        })
        return true
    }

    private fun checkNick(service : RetrofitService, nick : String): Boolean {
        // 서버와 통신하여 닉네임 중복 확인
        Log.d(TAG, "닉네임 체크 : $nick")
        if(nick.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false // 이메일을 입력하지 않은 경우 여기서 빠져나감. return 안하면 onResponse에서 null받았다고 하면서 에러 발생.
        }
        service.checkNick(nick).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "닉네임 체크 실패 : {$t}")
                nickcheckd = false
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "닉네임 체크 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                Log.d(TAG, msg)
                Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
                nickcheckd = result
            }
        })
        return true
    }

    // 입력칸 입력 여부 및 형식 검증
    private fun formCheck(email: String, pw: String, pwc: String, nick: String, check: Boolean): Boolean {
        if(email.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        } else if(pw.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@!%*?&])[A-Za-z\\d$@!%*?&]{8,16}", pw)) {
            Toast.makeText(this@SignUpActivity, "비밀번호 형식을 맞춰주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(pwc.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "비밀번호를 한번 더 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(pw != pwc) {
            Log.d(TAG, pw)
            Log.d(TAG, pwc)
            Toast.makeText(this@SignUpActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(nick.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(!check) {
            Log.d(TAG, check.toString())
            Toast.makeText(this@SignUpActivity, "이용약관 및 개인정보처리방침에 동의해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int -> startActivity<LoginActivity>() }
            .show()
    }

}