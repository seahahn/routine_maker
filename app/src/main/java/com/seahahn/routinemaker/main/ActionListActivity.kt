package com.seahahn.routinemaker.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo.getUserId

class ActionListActivity : Main() {

    private val TAG = this::class.java.simpleName

    private val actionViewModel by viewModels<ActionViewModel>() // 루틴, 할 일 목록 데이터

    private lateinit var actionAdapter: ActionAdapter
    private lateinit var actionList: RecyclerView
    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = intent.getStringExtra("title").toString() // 툴바 제목에 들어갈 텍스트. 루틴 제목을 가져옴
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.home
        btmnav.setOnNavigationItemSelectedListener(this)

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 루틴 내 행동 추가하기 액티비티로 이동
        initFAB()

        rtId = intent.getIntExtra("id", 0) // DB 내 루틴의 고유 번호 받기
        getActions(service, rtId, getUserId(this)) // 고유 번호에 해당하는 데이터 가져와서 세팅하기

        actionList = findViewById(R.id.actionList) // 리사이클러뷰 초기화
        actionAdapter = ActionAdapter() // 어댑터 초기화
        actionList.adapter = actionAdapter // 어댑터 연결
        actionAdapter.getService(service) // 어댑터에 레트로핏 통신 객체 보내기

        actionViewModel.gottenActionData.observe(this) { actionDatas ->
            mDatas = actionDatas // 뷰모델에 저장해둔 루틴 내 행동 목록 데이터 가져오기
            actionAdapter.replaceList(mDatas) // 목록 데이터 출력하기
        }
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        getActions(service, rtId, getUserId(this)) // 고유 번호에 해당하는 데이터 가져와서 세팅하기
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_update, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }
}