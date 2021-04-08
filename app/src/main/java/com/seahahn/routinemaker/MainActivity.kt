package com.seahahn.routinemaker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.seahahn.routinemaker.network.NaverAPI
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.UtilMethod
import retrofit2.Retrofit
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : UtilMethod() {

    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    private lateinit var service : RetrofitService
    private lateinit var naverAPI : NaverAPI

    // 사용자가 선택한 날짜에 따라 툴바 제목도 그에 맞는 날짜로 변경함
    // 초기값은 오늘 날짜
    val cal = Calendar.getInstance()
    var y = cal.get(Calendar.YEAR)
    var m = cal.get(Calendar.MONTH)
    var d = cal.get(Calendar.DAY_OF_MONTH)
    var dateData = Calendar.getInstance() // 사용자가 선택한 날짜를 yyyy-MM-dd 형식으로 받아온 것. DB에서 데이터 받을 때 사용함
    var dateformatter: DateFormat = SimpleDateFormat("yyyy-MM-dd") // DB 데이터 불러오기 위한 날짜 형식
    var dateDataFormatted = dateformatter.format(dateData.time)
    lateinit var title : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 좌측 Navigation Drawer 초기화
        drawer_layout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)

        // 메인 액티비티에서는 오늘 날짜를 툴바 제목으로 씀
        val current = LocalDate.now()
        Log.d(TAG, "current : "+current.toString())
        val formatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault())
        val formatted = current.format(formatter)
        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = formatted // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 0) // 툴바 세팅하기

        // 툴바 제목(날짜) 좌우에 위치한 삼각 화살표 초기화
        // 좌측은 1일 전, 우측은 1일 후의 루틴 및 할 일 목록을 보여줌
        val leftArrow = findViewById<ImageButton>(R.id.left)
        val rightArrow = findViewById<ImageButton>(R.id.right)

        // 툴바 제목에 위치한 날짜를 누르면 날짜 선택이 가능함
        // 선택한 날짜에 따라 툴바 제목과 함께 날짜 정보가 변경됨
        title.setOnClickListener(dateClickListener())
        leftArrow.setOnClickListener(dateClickListener())
        rightArrow.setOnClickListener(dateClickListener())
    }

    inner class dateClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            y = cal.get(Calendar.YEAR)
            m = cal.get(Calendar.MONTH)
            d = cal.get(Calendar.DAY_OF_MONTH)
            dateDataFormatted = dateformatter.format(dateData.time)
            when(v?.id) {
                R.id.toolbarTitle -> {
                    showDatePicker(title, cal, dateData, y, m, d)
                }
                R.id.left -> {
                    onedayMove(title, cal, dateData, y, m, d, -1)
                }
                R.id.right -> {
                    onedayMove(title, cal, dateData, y, m, d, 1)
                }
            }
            Log.d(TAG, "dateDataFormatted : "+dateDataFormatted)
        }
    }


}