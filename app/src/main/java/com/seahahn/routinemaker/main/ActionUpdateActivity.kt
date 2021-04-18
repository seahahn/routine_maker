package com.seahahn.routinemaker.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class ActionUpdateActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_update)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeAction) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initRtTodoActivity(R.id.makeAction)
    }
}