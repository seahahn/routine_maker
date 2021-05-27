package com.seahahn.routinemaker.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

/*
* 선택한 그룹 뉴스피드
*/
class GroupFeedActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var feedListAdapter: ActionAdapter
//    private lateinit var feedList: RecyclerView
//    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_feed)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = intent.getStringExtra("그룹 고유 번호") // 툴바 제목에 들어갈 텍스트. 그룹명을 가져옴
        initToolbar(title, titleText!!, 0) // 툴바 세팅하기

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.group
        btmnav.setOnNavigationItemSelectedListener(this)

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 피드 작성을 할 수 있는 액티비티로 이동
        initFAB()
    }
}