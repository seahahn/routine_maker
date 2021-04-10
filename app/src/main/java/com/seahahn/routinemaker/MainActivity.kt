package com.seahahn.routinemaker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.seahahn.routinemaker.util.Main
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(title, formatted, 0) // 툴바 세팅하기

        // 툴바 제목(날짜) 좌우에 위치한 삼각 화살표 초기화
        // 좌측은 1일 전, 우측은 1일 후의 루틴 및 할 일 목록을 보여줌
        leftArrow = findViewById(R.id.left)
        rightArrow = findViewById(R.id.right)

        // 툴바 제목에 위치한 날짜를 누르면 날짜 선택이 가능함
        // 선택한 날짜에 따라 툴바 제목과 함께 날짜 정보가 변경됨
        title.setOnClickListener(DateClickListener())
        leftArrow.setOnClickListener(DateClickListener())
        rightArrow.setOnClickListener(DateClickListener())
    }
}