package com.seahahn.routinemaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.amplifyframework.core.Amplify
import com.seahahn.routinemaker.notice.NoticeActivity
import com.seahahn.routinemaker.user.MypageActivity
import com.seahahn.routinemaker.util.Main
import org.jetbrains.anko.email
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
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
        toast("onCreate")

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(title, formatted, 0) // 툴바 세팅하기

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnav_header.findViewById(R.id.hd_email)
        hd_nick = leftnav_header.findViewById(R.id.hd_nick)
        hd_mbs = leftnav_header.findViewById(R.id.hd_mbs)
        hd_photo = leftnav_header.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 툴바 제목(날짜) 좌우에 위치한 삼각 화살표 초기화
        // 좌측은 1일 전, 우측은 1일 후의 루틴 및 할 일 목록을 보여줌
        leftArrow = findViewById(R.id.left)
        rightArrow = findViewById(R.id.right)

        // 툴바 제목에 위치한 날짜를 누르면 날짜 선택이 가능함
        // 선택한 날짜에 따라 툴바 제목과 함께 날짜 정보가 변경됨
        title.setOnClickListener(DateClickListener())
        leftArrow.setOnClickListener(DateClickListener())
        rightArrow.setOnClickListener(DateClickListener())

//        val exampleFile = File(applicationContext.filesDir, "ExampleKey")
//
//        exampleFile.writeText("Example file contents")
//
//        Amplify.Storage.uploadFile(
//            "test/test.txt", // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
//            exampleFile, // 실제 저장될 파일
//            { result -> Log.d("MyAmplifyApp", "Successfully uploaded: " + result) },
//            { error -> Log.d("MyAmplifyApp", "Upload failed", error) }
//        )
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 좌측 내비의 헤더 부분에 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
    }
}