package com.seahahn.routinemaker.sns.others

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.bumptech.glide.Glide
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.stts.day.SttsDayFragment
import com.seahahn.routinemaker.stts.month.SttsMonthFragment
import com.seahahn.routinemaker.stts.week.SttsWeekFragment
import com.seahahn.routinemaker.util.AppVar
import com.seahahn.routinemaker.util.AppVar.getOtherUserId
import com.seahahn.routinemaker.util.Stts

class OtherSttsActivity : Stts() {

    private val TAG = this::class.java.simpleName

    //    private lateinit var tabLayout: TabLayout
    private lateinit var sttsDayFragment: SttsDayFragment
    private lateinit var sttsWeekFragment: SttsWeekFragment
    private lateinit var sttsMonthFragment: SttsMonthFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_stts)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 프로그레스바 초기화
        prograssbar = findViewById(R.id.prograssbar)
        showProgress(false)

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(toolbarTitle, formattedMDDoW, 2) // 툴바 세팅하기
        toolbar.overflowIcon = getDrawable(R.drawable.letter_d)

        // 하단 BottomNavigationView 초기화
        btmnavOthers = findViewById(R.id.btmnav)
        initOtherBtmNav()
        otherSttsIcon.alpha = 1f // 현재 보고 있는 액티비티의 위치를 나타내기 위해 액티비티에 연결된 아이콘의 투명도 조정
        // 하단 내비 우측에 방문한 사용자의 프로필 사진 넣기
        Glide.with(applicationContext).load(AppVar.getOtherUserPic(this))
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(otherProfilePic)

        // 툴바 제목(날짜) 좌우에 위치한 삼각 화살표 초기화
        // 좌측은 1일 전, 우측은 1일 후의 루틴 및 할 일 목록을 보여줌
        leftArrow = findViewById(R.id.left)
        rightArrow = findViewById(R.id.right)

        // 툴바 제목에 위치한 날짜를 누르면 날짜 선택이 가능함
        // 선택한 날짜에 따라 툴바 제목과 함께 날짜 정보가 변경됨
        toolbarTitle.setOnClickListener(DateClickListener())
        leftArrow.setOnClickListener(DateClickListener())
        rightArrow.setOnClickListener(DateClickListener())
        rightArrow.isEnabled = false

        // 액티비티에 포함될 프래그먼트 초기화
        sttsDayFragment = SttsDayFragment()
        sttsWeekFragment = SttsWeekFragment()
        sttsMonthFragment = SttsMonthFragment(this)

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

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        getActionRtRecords(service, getOtherUserId(this))
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_time_select, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 우측 메뉴(일간, 주간, 월간) 선택 시 각각에 해당하는 프래그먼트로 전환시킴
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        d(TAG, "onOptionsItemSelected Stts")
        when(item.itemId) {
            R.id.toolbarDay -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_d)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsDayFragment) .commit()
                selectedTime = 0
                initToolbarDate(cal, selectedTime, dateData.time, toolbarTitle)
            }
            R.id.toolbarWeek -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_w)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsWeekFragment) .commit()
                selectedTime = 1
                initToolbarDate(cal, selectedTime, dateData.time, toolbarTitle)
            }
            R.id.toolbarMonth -> {
                toolbar.overflowIcon = getDrawable(R.drawable.letter_m)
                supportFragmentManager.beginTransaction().replace(R.id.container, sttsMonthFragment) .commit()
                selectedTime = 2
                initToolbarDate(cal, selectedTime, dateData.time, toolbarTitle)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}