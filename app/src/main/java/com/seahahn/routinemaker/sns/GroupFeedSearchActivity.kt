package com.seahahn.routinemaker.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

class GroupFeedSearchActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var feedListAdapter: ActionAdapter
//    private lateinit var feedList: RecyclerView
//    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_feed_search)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.searchFeedTitle) // 툴바 제목에 들어갈 텍스트. 루틴 제목을 가져옴
        initToolbar(title, titleText, 0) // 툴바 세팅하기
    }
}