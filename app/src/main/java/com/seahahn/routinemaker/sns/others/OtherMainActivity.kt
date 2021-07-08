package com.seahahn.routinemaker.sns.others

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.AppVar.getOtherUserId
import com.seahahn.routinemaker.util.AppVar.getOtherUserPic
import com.seahahn.routinemaker.util.Sns

class OtherMainActivity : Sns() {

    private val TAG = this::class.java.simpleName

    // 메인 액티비티 상단 툴바 바로 아래의 탭 레이아웃 및 각각의 탭에 해당하는 프래그먼트 초기화
    private lateinit var tabLayout: TabLayout
    private lateinit var mainRoutineFragment: OtherMainRoutineFragment
    private lateinit var mainReviewFragment: OtherMainReviewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_main)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        initToolbar(title, formattedMDDoW, 2) // 툴바 세팅하기

        // 하단 내비 초기화
        btmnavOthers = findViewById(R.id.btmnav)
        initOtherBtmNav()
        otherHomeIcon.alpha = 1f // 현재 보고 있는 액티비티의 위치를 나타내기 위해 액티비티에 연결된 아이콘의 투명도 조정
        // 하단 내비 우측에 방문한 사용자의 프로필 사진 넣기
        Glide.with(applicationContext).load(getOtherUserPic(this))
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(otherProfilePic)

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
        mainRoutineFragment = OtherMainRoutineFragment()
        mainReviewFragment = OtherMainReviewFragment()

        // 탭 레이아웃 초기화. 탭에 따라 보여줄 프래그먼트가 바뀜
        tabLayout = findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(OnTabSelectedListener())

        // '루틴' 탭의 루틴 및 할 일 목록 데이터 불러오기
//        getRts(service, getUserId(this))

        // 프래그먼트에 전달할 날짜 데이터 초기화(오늘 날짜)
        onDateSelected(dateformatter.format(dateData.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기

        // 처음에는 '루틴' 탭에 해당하는 프래그먼트를 보여줌
        supportFragmentManager.commit {
            replace<OtherMainRoutineFragment>(R.id.container)
            setReorderingAllowed(true)
            addToBackStack(null) // name can be null
        }
    }

    override fun onResume() {
        super.onResume()

        // 정보 변경된 경우 바뀐 정보를 적용하기 위해서 다시 초기화해줌
        getRts(service, getOtherUserId(this))
    }

    // 상단 툴바 바로 아래의 '루틴' 탭과 '회고' 탭 클릭 시 작동할 기능
    inner class OnTabSelectedListener : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            val position = tabLayout.selectedTabPosition
            var selected : Fragment = mainRoutineFragment
            when(position) {
                0 -> {
                    selected = mainRoutineFragment
//                    fabtn.visibility = View.VISIBLE
                }
                1 -> {
                    selected = mainReviewFragment
//                    fabtn.visibility = View.GONE
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
}