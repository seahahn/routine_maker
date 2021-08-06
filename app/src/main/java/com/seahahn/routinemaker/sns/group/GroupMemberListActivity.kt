package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.Sns
import java.util.*

/*
* 그룹 가입 신청자 목록
*/
class GroupMemberListActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var viewEmptyList : LinearLayout

    private lateinit var searchView: SearchView
    var searchedDatas = mutableListOf<GroupMemberData>()
    lateinit var it_mDatas : Iterator<GroupMemberData>

    lateinit var memberList : RecyclerView
    lateinit var memberListAdapter : GroupMemberListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_member_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.memberList) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹 가입 신청자 검색창
        searchView.setOnQueryTextListener(QueryTextChenageListener())

        memberList = findViewById(R.id.memberList) // 리사이클러뷰 초기화
        memberListAdapter = GroupMemberListAdapter() // 어댑터 초기화
        memberList.adapter = memberListAdapter // 어댑터 연결
        memberListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        groupId = intent.getIntExtra("groupId", 0) // DB 내 그룹의 고유 번호 받기
//        getGroup(service, groupId) // 고유 번호에 해당하는 그룹 데이터 가져와서 세팅하기
        getGroupMembers(service, groupId, true) // 그룹에 가입한 사람 목록 가져오기

        // 그룹 가입 신청자 목록 출력하기
        groupMemberViewModel.gottenGroupMemberData.observe(this) { groupMemberDatas ->
            groupMemberListData = groupMemberDatas

            memberListAdapter.replaceList(groupMemberListData)

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(memberListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        }
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

            searchedDatas.clear() // 검색 결과 목록 비우기
            it_mDatas = groupMemberListData.iterator() // 사용자가 가입하지 않았고 그룹 멤버 수가 인원 제한에 도달하지 않은 그룹 목록에서 검색 결과 뽑기
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                if (it_mData.nick.lowercase(Locale.getDefault()).contains(inputText)) {
                    searchedDatas.add(it_mData)
                }
            }

            memberListAdapter.replaceList(searchedDatas) // 사용자 고유 번호에 맞춰서 가입한 그룹 목록 띄우기

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if (memberListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }

            if (newText == "") {
                viewEmptyList.visibility = View.GONE
                memberListAdapter.replaceList(groupMemberListData) // 검색창이 비었으면 다시 전체 목록을 출력함
            }

            return true
        }
    }

}