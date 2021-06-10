package com.seahahn.routinemaker.sns.newsfeed

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.group.GroupListAdapter
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.UserInfo.getUserId

/*
* 선택한 그룹 뉴스피드
*/
class GroupFeedActivity : Sns() {

    private val TAG = this::class.java.simpleName

    private lateinit var viewEmptyList : LinearLayout

    private lateinit var feedListAdapter: GroupFeedAdapter
    private lateinit var feedList: RecyclerView
    var mDatas = mutableListOf<FeedData>()
    var showDatas = mutableListOf<FeedData>()
    lateinit var it_mDatas : Iterator<FeedData>

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

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = groupTitle // 툴바 제목에 들어갈 텍스트. 그룹명을 가져옴
        initToolbar(title, titleText, 1) // 툴바 세팅하기

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
        feedListAdapter = GroupFeedAdapter() // 어댑터 초기화
        feedList.adapter = feedListAdapter // 어댑터 연결
        feedListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        // 피드 목록 가져오기
        feedListViewModel.gottenFeedData.observe(this) { feedDatas ->
            Logger.d(TAG, "feedDatas : $feedDatas")
            mDatas = feedDatas // 뷰모델에 저장해둔 피드 목록 데이터 가져오기

//            showDatas.clear()
//            it_mDatas = mDatas.iterator()
//            while (it_mDatas.hasNext()) {
//                val it_mData = it_mDatas.next()
//                // 사용자가 가입한 그룹 목록만 추려서 출력
//                if (it_mData.joined) {
//                    showDatas.add(it_mData)
//                }
//            }

            feedListAdapter.replaceList(mDatas) // 그룹 고유 번호에 맞춰서 피드 목록 띄우기

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