package com.seahahn.routinemaker.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.user.MypageActivity
import org.jetbrains.anko.startActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

open class Main  : Util(), NavigationView.OnNavigationItemSelectedListener{

    private val TAG = this::class.java.simpleName
    open lateinit var drawerLayout : DrawerLayout // 좌측 내비게이션 메뉴가 포함된 액티비티의 경우 DrawerLayout을 포함하고 있음
    open lateinit var leftnav : NavigationView // 좌측 내비게이션 메뉴
    private var homeBtn : Int = 0 // 상단 툴바의 좌측 버튼 이미지를 결정하기 위한 변수

    // 사용자가 선택한 날짜에 따라 툴바 제목도 그에 맞는 날짜로 변경함
    // 초기값은 오늘 날짜
    val cal: Calendar = Calendar.getInstance()
    var y = cal.get(Calendar.YEAR)
    var m = cal.get(Calendar.MONTH)
    var d = cal.get(Calendar.DAY_OF_MONTH)
    var dateData: Calendar = Calendar.getInstance() // 사용자가 선택한 날짜를 yyyy-MM-dd 형식으로 받아온 것. DB에서 데이터 받을 때 사용함
    var dateformatter: DateFormat = SimpleDateFormat("yyyy-MM-dd") // DB 데이터 불러오기 위한 날짜 형식
    var dateDataFormatted: String = dateformatter.format(dateData.time)
    lateinit var title : TextView

    // 메인 액티비티에서는 오늘 날짜를 툴바 제목으로 씀
    private val current = LocalDate.now()
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault())
    val formatted: String = current.format(formatter)

    lateinit var leftArrow : ImageButton
    lateinit var rightArrow : ImageButton


    // 상단 툴바를 초기화하기 위한 메소드
    fun initToolbar(title: TextView, titleText: String, leftIcon: Int) {

        setSupportActionBar(findViewById(R.id.toolbar)) // 커스텀 툴바 설정

        supportActionBar!!.setDisplayShowTitleEnabled(false) //기본 제목을 없애줍니다
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // 좌측 화살표 누르면 이전 액티비티로 가도록 함

        // 툴바 좌측 아이콘 설정. 0이면 햄버거 메뉴 아이콘, 1이면 좌향 화살표 아이콘.
        if(leftIcon == 0) {
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.hbgmenu) // 왼쪽 버튼 이미지 설정 - 좌측 햄버거 메뉴
            homeBtn = R.drawable.hbgmenu
        } else if(leftIcon == 1){
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.backward_arrow) // 왼쪽 버튼 이미지 설정 - 좌향 화살표
            homeBtn = R.drawable.backward_arrow
        }
        title.text = titleText // 제목 설정
    }

    // 사용자로부터 메인(루틴 목록), 통계 등의 데이터를 불러오기 위해 필요한 날짜 정보를 받을 때 사용되는 메소드
    fun showDatePicker(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int) {
        Log.d(TAG, "input : $m $d")
        DatePickerDialog(this,
            {
                    _, year, month, day->
                val cal = Calendar.getInstance()
                cal.set(year, month, day) // 선택한 날짜를 Calendar 형식으로 가져옴
                val date = cal.time
                Log.d(TAG, "date : $date")

                val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바에 세팅하기 위한 형식
                val formatted: String = formatter.format(date)
                Log.d(TAG, "formatedDate : $formatted")

                c.set(year, month, day) // 사용자의 선택에 따라 툴바에 출력되는 날짜를 변경하기 위함
                dd.set(year, month, day) // 이는 yyyy-MM-dd 형식으로 DB에서 데이터를 가져오기 위함
                title.text = formatted
            },
            y, m, d)
            .show()
    }

    // 현재 날짜의 하루 전으로 이동하는 메소드
    fun onedayMove(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int, updown: Int) {
        Log.d(TAG, "input : $m $d")
        val cal = Calendar.getInstance()
        val day = d + updown
        cal.set(y, m, day) // 선택한 날짜를 Calendar 형식으로 가져옴
        val date = cal.time
        Log.d(TAG, "date : $date")

        val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바에 세팅하기 위한 형식
        val formatted: String = formatter.format(date)
        Log.d(TAG, "formatedDate : $formatted")
        c.set(y, m, day)
        dd.set(y, m, day)
        title.text = formatted
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, {
                _, h, m -> Toast.makeText(this, "$h:$m",
            Toast.LENGTH_SHORT).show() }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true)
            .show()
    }

    // 상단 툴바의 날짜를 변경하기 위한 클릭 리스너
    inner class DateClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            y = cal.get(Calendar.YEAR)
            m = cal.get(Calendar.MONTH)
            d = cal.get(Calendar.DAY_OF_MONTH)
            dateDataFormatted = dateformatter.format(dateData.time)
            when(v?.id) {
                R.id.toolbarTitle -> { // 제목 클릭한 경우에는 날짜 선택 가능하게 달력(DatePickerDialog)을 띄움
                    showDatePicker(title, cal, dateData, y, m, d)
                }
                R.id.left -> { // 좌측 화살표 누르면 하루 전으로
                    onedayMove(title, cal, dateData, y, m, d, -1)
                }
                R.id.right -> { // 우측 화살표 누르면 하루 뒤로
                    onedayMove(title, cal, dateData, y, m, d, 1)
                }
            }
            Log.d(TAG, "dateDataFormatted : $dateDataFormatted")
        }
    }

    // 툴바 버튼 클릭 시 작동할 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when(item.itemId){
            android.R.id.home->{ // 툴바 좌측 버튼
                if(homeBtn == R.drawable.hbgmenu) { // 햄버거 메뉴 버튼일 경우
                    drawerLayout.openDrawer(GravityCompat.START) // 네비게이션 드로어 열기
                } else if(homeBtn == R.drawable.backward_arrow) { // 좌향 화살표일 경우
                    Log.d(TAG, "뒤로 가기")
                    finish() // 액티비티 종료하기
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 좌측 내비 메뉴 클릭 시 작동할 기능
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected")
        when(item.itemId){
            R.id.mypage-> startActivity<MypageActivity>()
            R.id.notice-> Toast.makeText(this,"notice clicked",Toast.LENGTH_SHORT).show()
            R.id.faq-> Toast.makeText(this,"faq clicked",Toast.LENGTH_SHORT).show()
            R.id.qna-> Toast.makeText(this,"qna clicked",Toast.LENGTH_SHORT).show()
            R.id.writeReview-> Toast.makeText(this,"writeReview clicked",Toast.LENGTH_SHORT).show()
        }
        return false
    }

    // 뒤로가기 버튼 누르면 좌측 내비게이션 닫기
    override fun onBackPressed() { //뒤로가기 처리
        Log.d(TAG, "onBackPressed")
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

}