package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.tabs.TabLayout
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.UserInfo.getUserId

class MainActivity : Main() {

    private val TAG = this::class.java.simpleName

    // 메인 액티비티 상단 툴바 바로 아래의 탭 레이아웃 및 각각의 탭에 해당하는 프래그먼트 초기화
    private lateinit var tabLayout: TabLayout
    private lateinit var mainRoutineFragment: MainRoutineFragment
    private lateinit var mainReviewFragment: MainReviewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(title, formattedMDDoW, 0) // 툴바 세팅하기

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnav_header.findViewById(R.id.hd_email)
        hd_nick = leftnav_header.findViewById(R.id.hd_nick)
        hd_mbs = leftnav_header.findViewById(R.id.hd_mbs)
        hd_photo = leftnav_header.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.home
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
        mainRoutineFragment = MainRoutineFragment()
        mainReviewFragment = MainReviewFragment()

        // 탭 레이아웃 초기화. 탭에 따라 보여줄 프래그먼트가 바뀜
        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(OnTabSelectedListener())

        // '루틴' 탭의 루틴 및 할 일 목록 데이터 불러오기
//        getRts(service, getUserId(this))

        // 프래그먼트에 전달할 날짜 데이터 초기화(오늘 날짜)
        onDateSelected(dateformatter.format(dateData.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기

        // 처음에는 '루틴' 탭에 해당하는 프래그먼트를 보여줌
        supportFragmentManager.commit {
            replace<MainRoutineFragment>(R.id.container)
            setReorderingAllowed(true)
            addToBackStack(null) // name can be null
        }

        // 우측 하단의 FloatingActionButton 초기화
        // 버튼을 누르면 루틴 또는 할 일을 만들 수 있는 액티비티로 이동 가능한 FAB 2개가 나타남
        initFAB()
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)
        getRts(service, getUserId(this))
    }

    // 상단 툴바 바로 아래의 '루틴' 탭과 '회고' 탭 클릭 시 작동할 기능
    inner class OnTabSelectedListener : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            val position = tabLayout.selectedTabPosition
            var selected : Fragment = mainRoutineFragment
            when(position) {
                0 -> {
                    selected = mainRoutineFragment
                    fabtn.visibility = View.VISIBLE
                }
                1 -> {
                    selected = mainReviewFragment
                    fabtn.visibility = View.GONE
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, selected) .commit()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            // do nothing
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            // do nothing
        }
    }

    // 뒤로가기 버튼 누르면 좌측 내비게이션 닫기
    override fun onBackPressed() { //뒤로가기 처리
        d(TAG, "onBackPressed")
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
//            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }


}
