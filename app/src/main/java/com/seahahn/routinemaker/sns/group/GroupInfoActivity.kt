package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.view.Menu
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId

/*
* 그룹 정보 수정
*/
open class GroupInfoActivity : Sns() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.groupInfo) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initGroupActivity(R.id.groupInfo) // 액티비티 구성 요소 초기화하기

        groupId = intent.getIntExtra("id", 0) // DB 내 루틴의 고유 번호 받기
        getGroup(service, groupId) // 고유 번호에 해당하는 그룹 데이터 가져와서 세팅하기
        getGroupMembers(service, groupId, true) // 그룹 멤버 목록 가져오기

        groupMemberViewModel.gottenGroupMemberData.observe(this) { groupMemberDatas ->
            groupMemberListData = groupMemberDatas
        }
//        groupMemberViewModel.gottenNewGroupLeader.observe(this) { newGroupLeader ->
//            newLeaderId = newGroupLeader
//        }
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(leaderId == getUserId(this)) {
            // 그룹 생성자인 경우에만 그룹 정보 수정 가능하게 함
            menuInflater.inflate(R.menu.menu_update, menu)
        }       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }
}