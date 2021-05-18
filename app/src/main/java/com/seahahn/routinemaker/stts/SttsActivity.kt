package com.seahahn.routinemaker.stts

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class SttsActivity : Main() {

    private val TAG = this::class.java.simpleName

//    private lateinit var tabLayout: TabLayout
    private lateinit var sttsDayFragment: SttsDayFragment
    private lateinit var sttsWeekFragment: SttsWeekFragment
    private lateinit var sttsMonthFragment: SttsMonthFragment

    private lateinit var toolbar : Toolbar

    // 사용자가 선택한 시간대에 따라 다른 값을 가짐
    // 일간(0), 주간(1), 월간(2). 초기값은 일간(0)임
    private var selectedTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stts)

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnavHeader = leftnav.getHeaderView(0)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(title, formattedMDDoW, 0) // 툴바 세팅하기
        toolbar = findViewById(R.id.toolbar)
        toolbar.overflowIcon = getDrawable(R.drawable.letter_d)

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnavHeader.findViewById(R.id.hd_email)
        hd_nick = leftnavHeader.findViewById(R.id.hd_nick)
        hd_mbs = leftnavHeader.findViewById(R.id.hd_mbs)
        hd_photo = leftnavHeader.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.stts
        btmnav.setOnNavigationItemSelectedListener(this)

        // 툴바 제목(날짜) 좌우에 위치한 삼각 화살표 초기화
        // 좌측은 1일 전, 우측은 1일 후의 루틴 및 할 일 목록을 보여줌
        leftArrow = findViewById(R.id.left)
        rightArrow = findViewById(R.id.right)

        // 툴바 제목에 위치한 날짜를 누르면 날짜 선택이 가능함
        // 선택한 날짜에 따라 툴바 제목과 함께 날짜 정보가 변경됨
        title.setOnClickListener(DateClickListener())
        leftArrow.setOnClickListener(DateClickListener())
        rightArrow.setOnClickListener(DateClickListener())

        // 액티비티에 포함될 프래그먼트 초기화
        sttsDayFragment = SttsDayFragment()
        sttsWeekFragment = SttsWeekFragment()
        sttsMonthFragment = SttsMonthFragment()

        // 탭 레이아웃 초기화. 탭에 따라 보여줄 프래그먼트가 바뀜
//        tabLayout = findViewById(R.id.tabLayout)
//        tabLayout.addOnTabSelectedListener(OnTabSelectedListener())

        // 프래그먼트에 전달할 날짜 데이터 초기화(오늘 날짜)
        onDateSelected(dateformatter.format(dateData.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기

        // 처음에는 '루틴' 탭에 해당하는 프래그먼트를 보여줌
        supportFragmentManager.commit {
            replace<SttsDayFragment>(R.id.container)
            setReorderingAllowed(true)
            addToBackStack(null) // name can be null
        }
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 좌측 내비의 헤더 부분에 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_time_select, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected Stts")
        when(item.itemId) {
            R.id.toolbarDay -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_d)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsDayFragment) .commit()
            }
            R.id.toolbarWeek -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_w)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsWeekFragment) .commit()
            }
            R.id.toolbarMonth -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_m)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsMonthFragment) .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}