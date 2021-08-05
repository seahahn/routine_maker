package com.seahahn.routinemaker.main

import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class ActionMakeActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeAction) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        initActionActivity(R.id.makeAction)

        rtId = intent.getIntExtra("id", 0) // 행동을 추가하려는 루틴의 DB내 고유 번호 받기
    }
}