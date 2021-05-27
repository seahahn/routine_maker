package com.seahahn.routinemaker.sns

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionAdapter
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.Sns

/*
* 가입한 그룹 목록
*/
class GroupListActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var groupListAdapter: ActionAdapter
//    private lateinit var groupList: RecyclerView
//    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.groupList) // 툴바 제목에 들어갈 텍스트. 루틴 제목을 가져옴
        initToolbar(title, titleText, 0) // 툴바 세팅하기

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnav_header.findViewById(R.id.hd_email)
        hd_nick = leftnav_header.findViewById(R.id.hd_nick)
        hd_mbs = leftnav_header.findViewById(R.id.hd_mbs)
        hd_photo = leftnav_header.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.group
        btmnav.setOnNavigationItemSelectedListener(this)

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 그룹 찾기 또는 그룹 만들기를 할 수 있는 액티비티로 이동 가능한 FAB 2개가 나타남
        initFAB()
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
    }
}