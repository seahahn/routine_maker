package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo

/*
* 그룹 찾기
*/
class GroupSearchActivity : Sns() {

    private val TAG = this::class.java.simpleName

    private lateinit var viewEmptyList : LinearLayout

    private lateinit var groupListAdapter: GroupListAdapter
    private lateinit var groupList: RecyclerView
    var mDatas = mutableListOf<GroupData>()
    var showDatas = mutableListOf<GroupData>()
    lateinit var it_mDatas : Iterator<GroupData>

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_search)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.searchGroup) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹명 검색창

        groupList = findViewById(R.id.groupList) // 리사이클러뷰 초기화
        groupListAdapter = GroupListAdapter() // 어댑터 초기화
        groupList.adapter = groupListAdapter // 어댑터 연결
        groupListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        // 그룹 목록 가져오기
        getGroups(service, UserInfo.getUserId(this))
        groupListViewModel.gottenGroupData.observe(this) { groupDatas ->
            d(TAG, "groupDatas : $groupDatas")
            mDatas = groupDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
            showDatas.clear()
            it_mDatas = mDatas.iterator()
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                // 사용자가 가입하지 않았고 그룹 멤버 수가 인원 제한에 도달하지 않은 그룹 목록만 추려서 출력
                if (!it_mData.joined && (it_mData.headLimit > it_mData.memberCount || it_mData.headLimit == 0)) {
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
}