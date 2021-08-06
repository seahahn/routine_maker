package com.seahahn.routinemaker.sns.newsfeed

import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.sns.chat.ChatActivity
import com.seahahn.routinemaker.sns.group.GroupApplicantListActivity
import com.seahahn.routinemaker.sns.group.GroupInfoActivity
import com.seahahn.routinemaker.sns.group.GroupMemberListActivity
import com.seahahn.routinemaker.sns.group.GroupUpdateActivity
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import java.util.*

/*
* 선택한 그룹 뉴스피드
*/
class GroupFeedActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var toolbar : Toolbar

//    private lateinit var viewEmptyList : LinearLayout

    private lateinit var feedListAdapter: GroupFeedAdapter
    private lateinit var feedList: RecyclerView
    var mDatas = mutableListOf<FeedData>()
    var showDatas = mutableListOf<FeedData>()
    lateinit var it_mDatas : Iterator<FeedData>

    private lateinit var searchView: SearchView

    private lateinit var showOnlyMyFeed: MaterialCheckBox // 내 피드만 보기 체크박스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_feed)

        // 레트로핏 통신 연결
        service = initRetrofit()

        groupId = intent.getIntExtra("id", 0) // DB 내 그룹의 고유 번호 받기
        groupTitle = intent.getStringExtra("title").toString()
        getGroup(service, groupId) // 고유 번호에 해당하는 그룹 데이터 가져와서 세팅하기
        getGroupMembers(service, groupId, true) // 그룹 멤버 목록 가져오기

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = groupTitle // 툴바 제목에 들어갈 텍스트. 그룹명을 가져옴
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기
//        toolbar = findViewById(R.id.toolbar)
        toolbar.overflowIcon = getDrawable(R.drawable.more) // 우측 상단의 더보기 아이콘 모양 변경

        searchView = findViewById(R.id.searchView) // 그룹명 검색창
        searchView.setOnQueryTextListener(QueryTextChenageListener())

        showOnlyMyFeed = findViewById(R.id.showOnlyMyFeed)
        showOnlyMyFeed.setOnCheckedChangeListener(this)

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
        // 버튼을 누르면 피드 작성을 할 수 있는 액티비티로 이동
        initFABinSNS()

        feedList = findViewById(R.id.feedList) // 리사이클러뷰 초기화
        feedListAdapter = GroupFeedAdapter(this) // 어댑터 초기화
        feedList.adapter = feedListAdapter // 어댑터 연결
        feedListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        // 피드 목록 가져오기
        feedListViewModel.gottenFeedData.observe(this) { feedDatas ->
            Logger.d(TAG, "feedDatas : $feedDatas")
            mDatas = feedDatas // 뷰모델에 저장해둔 피드 목록 데이터 가져오기

            // 사용자가 작성한 피드 목록을 별도로 저장해둠(내 피드만 보기 체크박스에 체크 시 출력할 내용)
            object : Thread() {
                override fun run() {
                    showDatas.clear()
                    it_mDatas = mDatas.iterator()
                    while (it_mDatas.hasNext()) {
                        val it_mData = it_mDatas.next()
                        // 사용자가 가입한 그룹 목록만 추려서 출력
                        if (it_mData.writerId == getUserId(applicationContext)) {
                            showDatas.add(it_mData)
                        }
                    }
                }
            }.start()

            feedListAdapter.replaceList(mDatas) // 그룹 고유 번호에 맞춰서 피드 목록 띄우기
            feedListAdapter.saveOriginalList(mDatas) // 원본 목록 저장하기(검색 이후 다시 제자리로 돌려놓기 위함)

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(feedListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        Logger.d(TAG, "onResume")
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
        getFeeds(service, groupId, getUserId(this))
    }

    // 검색창에 검색어를 입력할 경우의 동작
    inner class QueryTextChenageListener() : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            d(TAG, "text submitted")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
//            d(TAG, "text changed")
            val inputText = newText!!.lowercase(Locale.getDefault())
            feedListAdapter.filter.filter(inputText) // 검색어 결과에 따라 추출된 목록을 보여줌

//            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
//            d(TAG, "groupListAdapter.itemCount : ${groupListAdapter.itemCount}")
//            if (groupListAdapter.itemCount == 0) {
//                viewEmptyList.visibility = View.VISIBLE
//            } else {
//                viewEmptyList.visibility = View.GONE
//            }

//            if (newText == "") {
//                viewEmptyList.visibility = View.GONE
//                groupListAdapter.replaceList(mDatas) // 검색창이 비었으면 다시 전체 목록을 출력함
//            }

            return true
        }
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_group_feed, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        if(leaderId == getUserId(this)) {
            menu?.setGroupVisible(R.id.group_manage, true)
            if(!onPublicResult) { // 그룹이 비공개일 경우에만 그룹 가입 신청자 목록 보기 가능
                menu?.setGroupVisible(R.id.group_applicant, true)
            } else {
                menu?.setGroupVisible(R.id.group_applicant, false)
            }
        } else {
            menu?.setGroupVisible(R.id.group_manage, false)
            menu?.setGroupVisible(R.id.group_applicant, false)
        }
        return true
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarSearch -> {
                // 검색창 띄우기
                searchView.isVisible = !searchView.isVisible
            }
            R.id.info -> {
                d(TAG, "그룹 정보 보기")
                val it = Intent(this, GroupInfoActivity::class.java)
                it.putExtra("id", groupId)
                startActivity(it)
            }
            R.id.chat -> {
                d(TAG, "그룹 채팅 참여하기")
                val it = Intent(this, ChatActivity::class.java)
                it.putExtra("title", groupTitle)
                it.putExtra("isGroupchat", true)
                it.putExtra("hostId", leaderId)
                it.putExtra("audienceId", groupId)
                startActivity(it)
            }
            R.id.memberList -> { // 그룹원 목록 보기
                val it = Intent(this, GroupMemberListActivity::class.java)
                it.putExtra("groupId", groupId)
                startActivity(it)
            }
            R.id.update -> {
                d(TAG, "그룹 정보 수정")
                if(leaderId == getUserId(this)) {
                    item.isVisible
                } else {
                    !item.isVisible
                }
                val it = Intent(this, GroupUpdateActivity::class.java)
                it.putExtra("id", groupId)
                startActivity(it)
            }
            R.id.applicants -> {
                d(TAG, "그룹 가입 신청자 목록 보기")
                if(leaderId == getUserId(this)) {
                    item.isVisible
                } else {
                    !item.isVisible
                }
                val it = Intent(this, GroupApplicantListActivity::class.java)
                it.putExtra("id", groupId)
                startActivity(it)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 내 피드만 보기 체크 여부에 따른 목록 출력 방식 정하기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) { // 사용자가 작성한 피드만 보여줌
            feedListAdapter.replaceList(showDatas)
            feedListAdapter.saveOriginalList(showDatas)

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(feedListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        } else { // 모든 피드를 보여줌
            feedListAdapter.replaceList(mDatas)
            feedListAdapter.saveOriginalList(mDatas)
        }
    }

    // 뒤로가기 버튼 누르면 좌측 내비게이션 닫기
    override fun onBackPressed() { //뒤로가기 처리
        d(TAG, "onBackPressed")
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
//            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}