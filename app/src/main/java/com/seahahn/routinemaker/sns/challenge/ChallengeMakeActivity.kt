package com.seahahn.routinemaker.sns.challenge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.SnsChallenge

class ChallengeMakeActivity : SnsChallenge() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        groupId = intent.getIntExtra("id", 0) // DB 내 그룹의 고유 번호 받기

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeClgTitle) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

//        initClgActivity(R.id.makeClg) // 액티비티 구성 요소 초기화하기
    }
}