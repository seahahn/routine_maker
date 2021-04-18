package com.seahahn.routinemaker.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class ActionMakeActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_make)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeAction) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initRtTodoActivity(R.id.makeAction)
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_trash, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }
}