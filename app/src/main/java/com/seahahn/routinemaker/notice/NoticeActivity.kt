package com.seahahn.routinemaker.notice

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

class NoticeActivity : Sns() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.notiList) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 0) // 툴바 세팅하기

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnav_header.findViewById(R.id.hd_email)
        hd_nick = leftnav_header.findViewById(R.id.hd_nick)
        hd_mbs = leftnav_header.findViewById(R.id.hd_mbs)
        hd_photo = leftnav_header.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.notibtm
        btmnav.setOnNavigationItemSelectedListener(this)
        setBtmNavBadge()
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarFilter -> {
                // 알림 설정으로 이동
                val it = Intent(this, NotiSettingActivity::class.java)
                startActivity(it)
                overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}