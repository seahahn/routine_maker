package com.seahahn.routinemaker.sns.group

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.chat.ChatActivity
import com.seahahn.routinemaker.sns.chat.ChatDataBase
import com.seahahn.routinemaker.sns.chat.ChatListActivity
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.doAsync

/*
* 가입한 그룹 목록
*/
class GroupListActivity : Sns() {

    private val TAG = this::class.java.simpleName

    private lateinit var viewEmptyList : LinearLayout

    private lateinit var groupListAdapter: GroupListAdapter
    private lateinit var groupList: RecyclerView
    var mDatas = mutableListOf<GroupData>()
    var showDatas = mutableListOf<GroupData>()
    lateinit var it_mDatas : Iterator<GroupData>

//    val chatDB by lazy { ChatDataBase.getInstance(this) } // 채팅 내용 저장해둔 Room DB 객체 가져오기
    lateinit var badgeOfChat: BadgeDrawable // 우상단 채팅 아이콘에 붙어 있는 안 읽은 메시지 갯수 배지

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
        setBtmNavBadge()

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 그룹 찾기 또는 그룹 만들기를 할 수 있는 액티비티로 이동 가능한 FAB 2개가 나타남
        initFAB()

        groupList = findViewById(R.id.groupList) // 리사이클러뷰 초기화
        groupListAdapter = GroupListAdapter() // 어댑터 초기화
        groupList.adapter = groupListAdapter // 어댑터 연결
        groupListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        // 그룹 목록 가져오기
        groupListViewModel.gottenGroupData.observe(this) { groupDatas ->
            d(TAG, "groupDatas : $groupDatas")
            mDatas = groupDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
            showDatas.clear()
            it_mDatas = mDatas.iterator()
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                // 사용자가 가입한 그룹 목록만 추려서 출력
                if (it_mData.joined) {
                    showDatas.add(it_mData)
                }
            }

            groupListAdapter.replaceList(showDatas) // 사용자 고유 번호에 맞춰서 가입한 그룹 목록 띄우기

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(groupListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        d(TAG, "onResume")
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
        getGroups(service, getUserId(this))
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chatlist, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        badgeOfChat = BadgeDrawable.create(this) // 우상단 채팅 안 읽은 메시지 갯수 뱃지
        badgeOfChat.badgeGravity = BadgeDrawable.TOP_END
        badgeOfChat.horizontalOffset = 10
        chatDB!!.chatDao().getNumOfBadge().observe(this) { numberList ->
            badgeOfChat.number = 0
            for(element in numberList) {
                badgeOfChat.number += element
            }
            badgeOfChat.isVisible = badgeOfChat.number != 0
        }
        BadgeUtils.attachBadgeDrawable(badgeOfChat, toolbar, R.id.toolbarChat)
//        if(badgeOfChat.number != 0) {
//        }

        return super.onPrepareOptionsMenu(menu)
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarSearch -> {
                // 검색창 띄우기
            }
            R.id.toolbarChat -> {
                // 채팅 목록으로 이동
                val it = Intent(this, ChatListActivity::class.java)
                startActivity(it)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 뒤로가기 버튼 누르면 좌측 내비게이션 닫기
    override fun onBackPressed() { //뒤로가기 처리
        Log.d(TAG, "onBackPressed")
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
//            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}