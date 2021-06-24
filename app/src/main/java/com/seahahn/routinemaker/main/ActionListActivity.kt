package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import androidx.activity.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.ItemMoveCallback
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo.getUserId

class ActionListActivity : Main() {

    private val TAG = this::class.java.simpleName

    private val actionViewModel by viewModels<ActionViewModel>() // 루틴 내 행동 목록 데이터
    private val dateViewModel by viewModels<DateViewModel>() // 날짜 데이터

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
        setBtmNavBadge()

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 루틴 내 행동 추가하기 액티비티로 이동
        initFAB()

        rtId = intent.getIntExtra("id", 0) // DB 내 루틴의 고유 번호 받기
        getActions(service, rtId, getUserId(this)) // 고유 번호에 해당하는 데이터 가져와서 세팅하기

        actionList = findViewById(R.id.actionList) // 리사이클러뷰 초기화
        actionAdapter = ActionAdapter() // 어댑터 초기화
        actionList.adapter = actionAdapter // 어댑터 연결
        actionAdapter.getService(service) // 어댑터에 레트로핏 통신 객체 보내기

        // 어댑터에 사용자가 선택한 루틴의 수행 요일, 수행 예정일, 체크박스 활성화 및 체크 여부를 전달함
        // 이는 뷰홀더까지 전달되어 각 아이템의 완료 처리 및 체크박스 활성화와 체크 여부를 결정할 것임
        val mDays = intent.getStringExtra("mDays")
        val mDate = intent.getStringExtra("mDate")
        val isActionEnabled = intent.getBooleanExtra("isActionEnabled", false)
        actionAdapter.getRtInfo(mDays!!, mDate!!, isActionEnabled)

        // 행동의 순서를 변경하기 위한 헬퍼
        val callback = ItemMoveCallback(actionAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(actionList)    //리사이클러뷰를 넣어준다

        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "행동 목록 date : $date")
            // 사용자가 선택한 날짜를 루틴, 할 일 목록에 보내기
            // 선택한 날짜가 오늘 날짜면 루틴 체크박스 활성화, 다른 날짜면 비활성화
            actionAdapter.replaceDate(date)
        }

        actionViewModel.gottenActionData.observe(this) { actionDatas ->
//            d(TAG, "actionDatas : $actionDatas")
            mDatas = actionDatas // 뷰모델에 저장해둔 루틴 내 행동 목록 데이터 가져오기
//            d(TAG, "mDatas : $mDatas")
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