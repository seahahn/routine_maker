package com.seahahn.routinemaker.main

import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class RtMakeActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rt_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeRt) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        initRtTodoActivity(R.id.makeRt) // 액티비티 구성 요소 초기화하기
    }
}