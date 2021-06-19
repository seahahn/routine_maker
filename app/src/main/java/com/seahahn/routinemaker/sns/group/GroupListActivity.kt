package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId

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