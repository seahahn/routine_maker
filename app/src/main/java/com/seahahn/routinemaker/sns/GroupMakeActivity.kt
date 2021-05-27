package com.seahahn.routinemaker.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

/*
* 그룹 만들기
*/
class GroupMakeActivity : Sns() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeGroup) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initGroupActivity(R.id.makeGroup) // 액티비티 구성 요소 초기화하기
    }
}