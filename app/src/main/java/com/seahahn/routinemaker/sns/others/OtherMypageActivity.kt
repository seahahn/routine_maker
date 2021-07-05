package com.seahahn.routinemaker.sns.others

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.*
import com.seahahn.routinemaker.util.AppVar.getOtherUserId
import com.seahahn.routinemaker.util.AppVar.getOtherUserNick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtherMypageActivity : Main() {

    private val TAG = this::class.java.simpleName

    lateinit var nick : ToggleEditTextView
    lateinit var intro : ToggleEditTextView
    lateinit var photo : ImageView
    lateinit var photoUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_mypage)

        // 레트로핏 통신 연결
        service = initRetrofit()

        val tbtitle = findViewById<TextView>(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getOtherUserNick(this) // 툴바 제목에 들어갈 텍스트
        initToolbar(tbtitle, titleText, 1) // 툴바 세팅하기

        btmnavOthers = findViewById(R.id.btmnav)
        initOtherBtmNav()

        // 사용자 정보 불러오기
//        val id = intent.getIntExtra("id", 0) // DB 내 사용자의 고유 번호
        val id = getOtherUserId(this)
        nick = findViewById(R.id.nick) // 사용자 닉네임
        val level = findViewById<TextView>(R.id.level) // 사용자 레벨
        val title = findViewById<TextView>(R.id.title) // 사용자의 칭호(타이틀)
        intro = findViewById(R.id.review) // 사용자 자기소개글
        photo = findViewById(R.id.photo) // 사용자 프로필 사진
        mypageInfo(service, id, nick, level, title, intro, photo) // 사용자 정보 불러오기

        // 사용자 정보 수정하기
//        nickupdate = findViewById(R.id.nickupdate) // 사용자 닉네임

//        val titleupdate = findViewById<TextView>(R.id.titleupdate) // 사용자의 칭호(타이틀)

//        introupdate = findViewById(R.id.introupdate) // 사용자 자기소개글
    }

    private fun mypageInfo(service : RetrofitService, id : Int, nick : ToggleEditTextView, lv : TextView,
                           title : TextView, intro : ToggleEditTextView, photo : ImageView) {
        service.mypageInfo(id).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "다른 사용자 프로필 정보 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "다른 사용자 프로필 정보 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())

                nick.setText(gson.get("nick").asString)
                lv.text = (gson.get("lv").asInt).toString()
                title.text = gson.get("title").asString
//                        val cc = gson.get("cc").asInt
                intro.setText(gson.get("intro").asString)

                photoUrl = gson.get("photo").asString
                Glide.with(applicationContext).load(photoUrl)
                    .placeholder(R.drawable.warning)
                    .error(R.drawable.warning)
                    .into(photo)

//                d(TAG, "nick.getText() : "+nick.getText())
//                d(TAG, "intro.getText() : "+intro.getText())
//                nickupdate.bind(nick.getText(), nick)
//                introupdate.bind(intro.getText(), intro)
            }
        })
    }
}