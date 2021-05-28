package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import android.widget.SearchView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

/*
* 그룹 찾기
*/
class GroupSearchActivity : Sns() {

    private val TAG = this::class.java.simpleName

    private lateinit var searchView: SearchView

//    private lateinit var groupListAdapter: ActionAdapter
//    private lateinit var groupList: RecyclerView
//    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_search)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeGroup) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹명 검색창
    }
}