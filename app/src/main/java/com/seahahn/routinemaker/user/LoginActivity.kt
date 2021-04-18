package com.seahahn.routinemaker.user

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.NaverAPI
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.User
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : User(), View.OnClickListener {
    private val TAG = this::class.java.simpleName
//    private lateinit var service : RetrofitService
    private lateinit var naverAPI : NaverAPI

    // 구글 로그인 API 변수
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private val naver_client_id = "Eqnzx3WTgu0UF1OckiJC"
    private val naver_client_secret = "1yO6vhssgd"
    private val naver_client_name = "루틴 메이커"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 이메일, 비밀번호 입력값 가져오기
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val pwInput = findViewById<TextInputEditText>(R.id.pwInput)
        val email = emailInput.text
        val pw = pwInput.text

        // 회원가입 텍스트 버튼
        val signup = findViewById<TextView>(R.id.signupBtn)
        signup.setOnClickListener {
            startActivity<SignUpActivity>()
        }

        // 비밀번호 찾기 텍스트 버튼
        val findpw = findViewById<TextView>(R.id.findpw)
        findpw.setOnClickListener {
            startActivity<FindpwActivity>()
        }

        // 로그인 버튼
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            when {
                email!!.equals("") -> {
                    Toast.makeText(this@LoginActivity, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                pw!!.equals("") -> {
                    Toast.makeText(this@LoginActivity, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    login(service, email.toString(), pw.toString())
                }
            }
        }

        // 구글 로그인 버튼
        val btn_google_login = findViewById<SignInButton>(R.id.btn_google_login)
        setGoogleButtonText(btn_google_login, getString(R.string.googleLogin))
        btn_google_login.setOnClickListener(this) // 버튼 누르면 이 코드 작동함

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        // 구글 로그인 클라이언트 객체와 파이어베이스 인증 객체 초기화
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        // 네이버 로그인 버튼
        val btn_naver_login = findViewById<ImageButton>(R.id.btn_naver_login)
        btn_naver_login.setOnClickListener {
            mOAuthLoginInstance = OAuthLogin.getInstance()
            mOAuthLoginInstance.init(this@LoginActivity, naver_client_id, naver_client_secret, naver_client_name)
            mOAuthLoginInstance.startOauthLoginActivity(this@LoginActivity, mOAuthLoginHandler);
        }
    }

    // 네이버 로그인 인증 핸들러
    private val mOAuthLoginHandler: OAuthLoginHandler = @SuppressLint("HandlerLeak")
    object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                val accessToken: String = mOAuthLoginInstance.getAccessToken(baseContext) // 성공하면 접근 토큰 가져옴
                val refreshToken: String = mOAuthLoginInstance.getRefreshToken(baseContext)
                val expiresAt: Long = mOAuthLoginInstance.getExpiresAt(baseContext)
                val tokenType: String = mOAuthLoginInstance.getTokenType(baseContext)

                Log.d(TAG, "네이버 accessToken 결과 : $accessToken")
                val token = "Bearer $accessToken" // 가져온 토큰을 헤더 형식에 맞춰서 바꿔줌
                naverAPI = NaverAPI.create() // 네이버 로그인을 위해서 만든 별도의 NaverAPI 인터페이스 객체를 생성함
                // 이 객체를 통해 메인 스레드가 아닌 스레드로 네트워크 통신을 함
                getNaverUserInfo(naverAPI, token) // 네이버 API 객체와 접근 토큰을 이용하여 사용자 정보를 불러옴
            } else {
                val errorCode: String = mOAuthLoginInstance.getLastErrorCode(this@LoginActivity).code
                val errorDesc = mOAuthLoginInstance.getLastErrorDesc(this@LoginActivity)
                Toast.makeText(baseContext, "errorCode:$errorCode, errorDesc:$errorDesc", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 네이버 로그인을 통해 얻은 접근 토큰으로 사용자의 네이버 아이디 정보를 가져옴
    private fun getNaverUserInfo(naverAPI : NaverAPI, accessToken : String) {
        naverAPI.getNaverUserInfo(accessToken).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "네이버 로그인 성공")
                Log.d(TAG, "네이버 로그인 응답 : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val message = gson.get("message").asString
                Log.d(TAG, "네이버 message : $message")
                val res = gson.get("response")
                Log.d(TAG, "네이버 res : $res")
                val gson2 = Gson().fromJson(res, JsonObject::class.java)
                Log.d(TAG, "네이버 gson2 : $gson2")
                when (message) {
                    "success" -> {
                        Log.d(TAG, response.body().toString())
                        val inway = "naver"
                        val id = gson2.get("id").asString
                        val email = gson2.get("email").asString
                        // 사용자가 닉네임 공유에 동의하면 네이버 닉네임을, 아니라면 이메일을 닉네임으로 설정
                        var nick = email.substring(0, email.length-10)
                        try {
                            if(!gson2.get("nickname").asString.isNullOrEmpty()) {
                                nick = gson2.get("nickname").asString
                            }
                        } catch (e : NullPointerException) {
                        }

                        checkEmail(service, email, id, nick, inway)
                    }
                    "fail" -> {
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "네이버 로그인 실패 : {$t}")
            }
        })
    }

    // 구글 로그인 버튼 텍스트 변경 함수
    private fun setGoogleButtonText(loginButton: SignInButton, buttonText: String){
        var i = 0
        while (i < loginButton.childCount){
            val v = loginButton.getChildAt(i)
            if (v is TextView) {
                v.text = buttonText
                v.gravity = Gravity.CENTER
                return
            }
            i++
        }
    }

    // SNS 로그인 버튼 클릭하는 경우
    override fun onClick(v: View) {
        when (v.id) {
            // 구글 로그인 버튼 클릭 시
            R.id.btn_google_login -> signIn()
            // 네이버 로그인 버튼 클릭 시
        }
    }

    // 구글 로그인
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // SNS 로그인 요청 결과 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) { // 구글 로그인
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    // 구글 로그인 시 파이어베이스 인증 받기
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    getGoogleUserInfo(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun getGoogleUserInfo(user: FirebaseUser?) {
        user?.let {
            // Name, email address, and profile photo Url
            val inway = "google"
            val email = user.email
            val nick = user.displayName
//            var token = ""
//            val photoUrl = user.photoUrl

            // Check if user's email is verified
//            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid

//            user?.getIdToken(true)!!.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    token = task.result!!.token!!
//                } else {
//                    // Handle error -> task.getException();
//                }
//            }

            checkEmail(service, email, uid, nick, inway)
        }
    }

    // SNS 로그인 시 회원 정보 없는 경우 회원가입 통신 요청
    private fun snssignup(service : RetrofitService, email : String, uid : String, nick : String, inway : String) {
        service.signup(email, uid, nick, inway, 1).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "sns 회원가입 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "sns 회원가입 요청 응답 수신 성공 " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val status = gson.get("status").asInt
                val msg = gson.get("msg").asString

                when (status) {
                    200 -> {
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                        login(service, email, uid)
                    }
//                    405 -> {
//                        Toast.makeText(this@SignUpActivity, "회원가입 실패 : 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
//                    }
                    500 -> {
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // SNS 가입 시 기존에 가입된 이메일과 중복인 경우 해당 SNS에 연동할지 묻기 위한 메소드
    private fun checkEmail(service : RetrofitService, email : String, uid : String, nick : String, inway : String) {
        // 서버와 통신하여 이메일 중복 확인
        Log.d(TAG, "이메일 체크 : $email")
        service.checkEmail(email).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "이메일 체크 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "이메일 체크 성공 " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                val overlap = gson.get("overlap").asBoolean
                Log.d(TAG, msg)
                if(result && overlap) {
                    // SNS로 가입되어 있는 경우 바로 로그인한다
                    login(service, email, uid)
                }
                else if(overlap) {
                    // 기존에 가입된 이메일인 경우 기존 계정을 SNS와 연동할지 묻는다
                    snsConnect(service, email, uid, inway)
                } else {
                    // 기존에 가입되지 않은 경우 바로 SNS 이용하여 회원가입 진행
                    snssignup(service, email, uid, nick, inway)
                }
            }
        })
    }

    // SNS 가입 시 이메일 중복인 경우 기존 계정과 해당 SNS 연결 여부를 묻는 다이얼로그 띄우기
    fun snsConnect(service : RetrofitService, email : String, uid : String, inway : String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("기존 계정 SNS에 연동하기")
        builder.setMessage("기존에 동일한 이메일로 가입된 계정이 있습니다.\n선택하신 SNS와 연동할까요?")
//        builder.setIcon(R.mipmap.ic_launcher)

        // 버튼 클릭시에 무슨 작업을 할 것인가!
        val listener = DialogInterface.OnClickListener { p0, p1 ->
            when (p1) {
                DialogInterface.BUTTON_POSITIVE -> {
                    service.snsCon(email, uid, inway).enqueue(object : Callback<JsonObject>{
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Log.d(TAG, "SNS 연동 실패 : {$t}")
                        }

                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            Log.d(TAG, "SNS 연동 성공 " + response.body().toString())
                            val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                            val msg = gson.get("msg").asString
                            val result = gson.get("result").asBoolean
                            toast(msg)
                            if(result) login(service, email, uid)
                        }
                    })
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    toast("SNS 연동 취소.\n기존 계정으로 로그인해주세요.")
                    Log.d(TAG, "SNS 연동 취소")
                }
            }
        }
        builder.setPositiveButton("예", listener)
        builder.setNegativeButton("아니오", listener)

        builder.show()
    }
}

