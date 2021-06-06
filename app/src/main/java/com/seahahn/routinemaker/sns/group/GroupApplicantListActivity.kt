package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

/*
* 그룹 가입 신청자 목록
*/
class GroupApplicantListActivity : Sns(), View.OnClickListener {

    private val TAG = this::class.java.simpleName

    private lateinit var searchView: SearchView

    private lateinit var checkAll: MaterialCheckBox
    var checkAllChecked = false

    lateinit var applicantList : RecyclerView
    lateinit var applicantListAdapter : GroupApplicantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_member_applicant_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.applicantList) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹 가입 신청자 검색창

        checkAll = findViewById(R.id.checkAll) // 목록 아이템 전체 선택 체크박스
        checkAll.setOnClickListener(this)

        // 하단 버튼 초기화
        btmBtn = findViewById(R.id.allowJoin)
        setFullBtmBtnText(btmBtn)
        btmBtn.setOnClickListener(BtnClickListener())

        applicantList = findViewById(R.id.applicantList) // 리사이클러뷰 초기화
        applicantListAdapter = GroupApplicantListAdapter() // 어댑터 초기화
        applicantList.adapter = applicantListAdapter // 어댑터 연결
        applicantListAdapter.getService(service)

        // 어댑터에 전체 선택하기 체크박스 전달(개별 아이템 선택 해제하면 전체 선택하기 체크박스의 체크도 해제하기 위해서임)
        applicantListAdapter.setCheckAllCheckbox(checkAll)

        groupId = intent.getIntExtra("id", 0) // DB 내 루틴의 고유 번호 받기
        getGroup(service, groupId) // 고유 번호에 해당하는 그룹 데이터 가져와서 세팅하기
        getGroupMembers(service, groupId, false) // 그룹에 가입 신청한 사람 목록 가져오기

        // 그룹 가입 신청자 목록 출력하기
        groupMemberViewModel.gottenGroupMemberData.observe(this) { groupMemberDatas ->
            groupMemberListData = groupMemberDatas

            applicantListAdapter.replaceList(groupMemberListData)
        }
    }

    // 전체 선택하기 체크박스의 체크 여부에 따라 아이템 전체의 체크 여부를 결정함
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.checkAll -> {
                val checkbox = v as MaterialCheckBox
                applicantListAdapter.checkAll(checkbox.isChecked)
            }
        }
    }
}