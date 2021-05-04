package com.seahahn.routinemaker.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class ActionUpdateActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_update)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.updateAction) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initActionActivity(R.id.updateAction)

        actionId = intent.getIntExtra("id", 0) // DB 내 루틴 내 행동의 고유 번호 받기
        rtId = intent.getIntExtra("rtId", 0) // DB 내 루틴의 고유 번호 받기
        getAction(service, actionId) // 고유 번호에 해당하는 데이터 가져와서 세팅하기
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_trash, menu)       // 선택한 루틴 수정하기 액티비티로 이동
        return true
    }
}