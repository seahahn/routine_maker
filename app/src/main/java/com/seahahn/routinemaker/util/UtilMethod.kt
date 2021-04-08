package com.seahahn.routinemaker.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.NaverAPI
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.user.MypageActivity
import com.seahahn.routinemaker.user.SignUpActivity
import org.jetbrains.anko.startActivity
import retrofit2.Retrofit
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

open class UtilMethod  : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private val TAG = this::class.java.simpleName
    open lateinit var drawer_layout : DrawerLayout
    open lateinit var leftnav : NavigationView
    var homeBtn : Int = 0

    lateinit var mOAuthLoginInstance : OAuthLogin

    fun initToolbar(title: TextView, titleText: String, leftIcon: Int) {

        setSupportActionBar(findViewById(R.id.toolbar)) // 커스텀 툴바 설정

        getSupportActionBar()!!.setDisplayShowTitleEnabled(false) //기본 제목을 없애줍니다
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) // 좌측 화살표 누르면 이전 액티비티로 가도록 함

        // 툴바 좌측 아이콘 설정. 0이면 햄버거 메뉴 아이콘, 1이면 좌향 화살표 아이콘.
        if(leftIcon == 0) {
            getSupportActionBar()!!.setHomeAsUpIndicator(R.drawable.hbgmenu) // 왼쪽 버튼 이미지 설정 - 좌측 햄버거 메뉴
            homeBtn = R.drawable.hbgmenu
        } else if(leftIcon == 1){
            getSupportActionBar()!!.setHomeAsUpIndicator(R.drawable.backward_arrow) // 왼쪽 버튼 이미지 설정 - 좌향 화살표
            homeBtn = R.drawable.backward_arrow
        }
        title.setText(titleText) // 제목 설정
    }

    fun showAlert() {
        AlertDialog.Builder(this)
            .setTitle("앱 종료")
            .setPositiveButton("종료") { dialogInterface: DialogInterface, i: Int -> finish() }
            .setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int -> }
            .show()
    }

    // 사용자로부터 메인(루틴 목록), 통계 등의 데이터를 불러오기 위해 필요한 날짜 정보를 받을 때 사용되는 메소드
    fun showDatePicker(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int) {
        Log.d(TAG, "input : "+m+" "+d)
        DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener {
                datePicker, year, month, day->
                val cal = Calendar.getInstance()
                cal.set(year, month, day) // 선택한 날짜를 Calendar 형식으로 가져옴
                val date = cal.time
                Log.d(TAG, "date : "+date)

                val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바에 세팅하기 위한 형식
                val formatted: String = formatter.format(date)
                Log.d(TAG, "formatedDate : "+formatted)

                c.set(year, month, day)
                dd.set(year, month, day)
                title.setText(formatted)
            },
            y, m, d)
            .show()
    }

    // 현재 날짜의 하루 전으로 이동하는 메소드
    fun onedayMove(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int, updown: Int) {
        Log.d(TAG, "input : "+m+" "+d)
        val cal = Calendar.getInstance()
        val day = d + updown
        cal.set(y, m, day) // 선택한 날짜를 Calendar 형식으로 가져옴
        val date = cal.time
        Log.d(TAG, "date : "+date)

        val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바에 세팅하기 위한 형식
        val formatted: String = formatter.format(date)
        Log.d(TAG, "formatedDate : "+formatted)
        c.set(y, m, day)
        dd.set(y, m, day)
        title.setText(formatted)
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener {
                timePicker, h, m -> Toast.makeText(this, "$h:$m",
            Toast.LENGTH_SHORT).show() }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true)
            .show()
    }

    // 툴바 버튼 클릭 시 작동할 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when(item.itemId){
            android.R.id.home->{ // 툴바 좌측 버튼
                if(homeBtn == R.drawable.hbgmenu) { // 햄버거 메뉴 버튼일 경우
                    drawer_layout.openDrawer(GravityCompat.START) // 네비게이션 드로어 열기
                } else if(homeBtn == R.drawable.backward_arrow) { // 좌향 화살표일 경우
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
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

}