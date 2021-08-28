package com.seahahn.routinemaker.sns.challenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.ChallengeData
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.chat.ChatListActivity
import com.seahahn.routinemaker.sns.group.GroupListAdapter
import com.seahahn.routinemaker.util.SnsChallenge
import com.seahahn.routinemaker.util.UserInfo
import java.util.*

class ChallengeListActivity : SnsChallenge() {

    private val TAG = this::class.java.simpleName

    private lateinit var clgListAdapter: ClgListAdapter
    private lateinit var clgList: RecyclerView
    var mDatas = mutableListOf<ChallengeData>()
    var showDatas = mutableListOf<ChallengeData>()
    lateinit var it_mDatas : Iterator<ChallengeData>

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        groupId = intent.getIntExtra("id", 0) // DB 내 그룹의 고유 번호 받기

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.groupList) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 챌린지명 검색창
        searchView.setOnQueryTextListener(QueryTextChenageListener())

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.group
        btmnav.setOnNavigationItemSelectedListener(this)
        setBtmNavBadge()

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 챌린지 만들기를 할 수 있는 액티비티로 이동
        initFABinSNSClg()

        clgList = findViewById(R.id.groupList) // 리사이클러뷰 초기화
        clgListAdapter = ClgListAdapter(this) // 어댑터 초기화
        clgList.adapter = clgListAdapter // 어댑터 연결
        clgListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        // 챌린지 목록 가져오기
        clgListViewModel.gottenClgData.observe(this) { clgData ->
            Logger.d(TAG, "clgData : $clgData")
            mDatas = clgData // 뷰모델에 저장해둔 챌린지 목록 데이터 가져오기

            // 사용자가 가입한 그룹 목록만 추려서 출력
//            showDatas.clear()
//            it_mDatas = mDatas.iterator()
//            while (it_mDatas.hasNext()) {
//                val it_mData = it_mDatas.next()
//                if (it_mData.joined) {
//                    showDatas.add(it_mData)
//                }
//            }

            clgListAdapter.replaceList(mDatas) // 그룹 내 모든 챌린지 목록 보여주기
            clgListAdapter.saveOriginalList(mDatas) // 원본 목록 저장하기(검색 이후 다시 제자리로 돌려놓기 위함)

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(clgListAdapter.itemCount == 0) {
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
        getClgs(service, groupId)
    }

    // 검색창에 검색어를 입력할 경우의 동작
    inner class QueryTextChenageListener() : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "text submitted")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
//            d(TAG, "text changed")
            val inputText = newText!!.lowercase(Locale.getDefault())
            clgListAdapter.filter.filter(inputText) // 검색어 결과에 따라 추출된 목록을 보여줌

            return true
        }
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarFilter -> {
                // 챌린지 필터링 창 띄우기
            }
        }
        return super.onOptionsItemSelected(item)
    }
}