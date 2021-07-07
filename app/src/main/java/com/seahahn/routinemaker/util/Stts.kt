package com.seahahn.routinemaker.util

import android.app.DatePickerDialog
import android.util.Log.d
import android.view.View
import android.widget.TextView
import com.seahahn.routinemaker.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

open class Stts : Main() {

    private val TAG = this::class.java.simpleName

    lateinit var formatter: DateFormat
    lateinit var formatted: String

    // 사용자가 선택한 시간대에 따라 다른 값을 가짐
    // 일간(0), 주간(1), 월간(2). 초기값은 일간(0)임
    var selectedTime = 0

    // 월간 통계에서 하단의 루틴 수행 기록 데이터가 전부 로딩되었는지 체크하기 위한 변수
    var needToBeLoadedDataCount = 0 // 로딩되어야 할 데이터 수
    var loadedDataCount = 0 // 로딩된 데이터 수

    // 사용자로부터 메인(루틴 목록), 통계 등의 데이터를 불러오기 위해 필요한 날짜 정보를 받을 때 사용되는 메소드
    fun setToolbarDate(selectedTime : Int, title: TextView, c: Calendar, dd: Calendar, y: Int, m: Int, d: Int) {
        datePicker = DatePickerDialog(this,
            { _, year, month, day->
                val cal = Calendar.getInstance()
                cal.set(year, month, day) // 선택한 날짜를 Calendar 형식으로 가져옴
                val date = cal.time // Calendar를 Fri Apr 16 08:11:10 GMT 2021 의 형식으로 변환

                initToolbarDate(cal, selectedTime, date, title)

                c.set(year, month, day) // 사용자의 선택에 따라 툴바에 출력되는 날짜를 변경하기 위함
                dd.set(year, month, day) // 이는 yyyy-MM-dd 형식으로 DB에서 데이터를 가져오기 위함

                onDateSelected(dateformatter.format(dd.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기

                // 선택된 날짜가 오늘 날짜인 경우 또는 주/월간에서 선택된 날짜의 주 또는 월이 오늘 날짜가 포함된 주 또는 월인 경우 툴바 우측 화살표 비활성화시킴
//                val isMaxWeekOrMonth = isMaxWeekOrMonth(c, selectedTime)
//                if(TAG == "SttsActivity") rightArrow.isEnabled = dateformatter.format(dd.time) != formattedYMD && !isMaxWeekOrMonth
//                setEnabledRightArrow(c, dd.time, selectedTime)
            },
            y, m, d) // DatePicker가 열리면 보여줄 날짜 초기값
        if(TAG == "SttsActivity") datePicker.datePicker.maxDate = maxDate
        datePicker.show()
    }

    // 일간인 경우 하루 전후, 주간인 경우 일주일 전후, 월간인 경우 1개월 전후로 이동하는 메소드
    fun oneTimeMove(selectedTime : Int, title: TextView, c: Calendar, dd: Calendar, y: Int, m: Int, d: Int, updown: Int) {
        val cal = Calendar.getInstance()
        val time: Int
        when(selectedTime) {
            0 -> {
                time = d + updown
                cal.set(y, m, time) // 선택한 날짜를 Calendar 형식으로 가져옴
                c.set(y, m, time)
                dd.set(y, m, time)
            }
            1 -> {
                time = d + updown*7
                cal.set(y, m, time) // 선택한 날짜를 Calendar 형식으로 가져옴
                c.set(y, m, time)
                dd.set(y, m, time)
            }
            2 -> {
                time = m + updown
                cal.set(y, time, d) // 선택한 날짜를 Calendar 형식으로 가져옴
                c.set(y, time, d)
                dd.set(y, time, d)
            }
        }
        val date = cal.time

        initToolbarDate(cal, selectedTime, date, title)

        onDateSelected(dateformatter.format(dd.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기
        // 선택된 날짜가 오늘 날짜인 경우 또는 주/월간에서 선택된 날짜의 주 또는 월이 오늘 날짜가 포함된 주 또는 월인 경우 툴바 우측 화살표 비활성화시킴
//        val isMaxWeekOrMonth = isMaxWeekOrMonth(c, selectedTime)
//        rightArrow.isEnabled = dateformatter.format(dd.time) != formattedYMD && !isMaxWeekOrMonth
//        setEnabledRightArrow(c, dd.time, selectedTime)
    }

    // 선택된 시간대(일간 0, 주간 1, 월간 2)에 따라 툴바에 표시될 날짜의 형식을 변경시키는 메소드
    fun initToolbarDate(cal: Calendar, selectedTime : Int, date : Date, title : TextView) {
        val dummyCal = Calendar.getInstance()
        dummyCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
        val year = dummyCal.get(Calendar.YEAR) // 이게 없으면 주간에서 sat, sun이 바뀌질 않음. 원인 불명.
//        val month = dummyCal.get(Calendar.MONTH)
//        val day = dummyCal.get(Calendar.DAY_OF_MONTH)
//        d(TAG, "dummyCal : $year, $month, $day")
        when(selectedTime) {
            0 -> {
                formatter = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바 제목에 세팅하기 위한 형식
                formatted = formatter.format(date)
            }
            1 -> {
                dummyCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                val sat = dummyCal.time
//                d(TAG, "sat : $sat")
                dummyCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                val sun = dummyCal.time
//                d(TAG, "sun : $sun")
                formatter = SimpleDateFormat("M/d", Locale.getDefault()) // 툴바 제목에 세팅하기 위한 형식
                formatted = formatter.format(sun) + " ~ " + formatter.format(sat)
            }
            2 -> {
                formatter = SimpleDateFormat("yyyy년 M월", Locale.getDefault()) // 툴바 제목에 세팅하기 위한 형식
                formatted = formatter.format(date)
            }
        }
        setEnabledRightArrow(cal, date, selectedTime)
        title.text = formatted // 툴바 제목에 사용자가 선택한 날짜 집어넣기
    }

    private fun setEnabledRightArrow(cal: Calendar, date : Date, selectedTime : Int) {
        val isMaxWeekOrMonth = isMaxWeekOrMonth(cal, selectedTime)
        rightArrow.isEnabled = dateformatter.format(date) != formattedYMD && !isMaxWeekOrMonth
    }

    // 선택된 날짜가 오늘 날짜 기준으로 동일한 주 또는 월인지 확인하는 메소드. 만약 동일하다면 true, 선택된 날짜가 오늘 날짜 이전의 주 또는 월이라면 false
    private fun isMaxWeekOrMonth(c: Calendar, selectedTime : Int) : Boolean {
        val cal = Calendar.getInstance() // 오늘 날짜
        val dummyCal = Calendar.getInstance()
        dummyCal.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE)) // 사용자가 선택한 날짜
        val year = dummyCal.get(Calendar.YEAR) // 이게 없으면 주간에서 값이 바뀌질 않음. 원인 불명.
//        d(TAG, "dummyCal : "+ dummyCal.get(Calendar.MONTH) + " " + dummyCal.get(Calendar.DAY_OF_MONTH))

        when(selectedTime) {
            1 -> {
                // 오늘 날짜 기준으로 해당 주의 마지막 날짜 구하기
                cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK))
                d(TAG, "cal : "+ cal.get(Calendar.MONTH) + " " + cal.get(Calendar.DAY_OF_MONTH))

                // 사용자가 선택한 날짜 기준으로 해당 주의 마지막 날짜 구하기
                dummyCal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK))
                d(TAG, "dummyCal Max : "+ dummyCal.get(Calendar.MONTH) + " " + dummyCal.get(Calendar.DAY_OF_MONTH))
            }
            2 -> {
                // 오늘 날짜 기준으로 해당 월의 마지막 날짜 구하기
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
//                d(TAG, "cal : "+ cal.get(Calendar.MONTH) + " " + cal.get(Calendar.DAY_OF_MONTH))

                // 사용자가 선택한 날짜 기준으로 해당 월의 마지막 날짜 구하기
                dummyCal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
//                d(TAG, "dummyCal Max : "+ dummyCal.get(Calendar.MONTH) + " " + dummyCal.get(Calendar.DAY_OF_MONTH))
            }
        }

//        d(TAG, "compare : "+dummyCal.compareTo(cal))
        // 위 둘을 비교하여 같으면 true, 사용자가 선택한 쪽이 보다 과거면 false
        return when(dummyCal.compareTo(cal)) {
            0 -> true
            -1 -> false
            else -> false
        }
//        1. dummyCal == cal 일 경우 return 0
//        2. dummyCal > cal 일 경우 return 1
//        3. dummyCal < cal 일 경우 return -1
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
                    setToolbarDate(selectedTime, title, cal, dateData, y, m, d)
                }
                R.id.left -> { // 좌측 화살표 누르면 하루 전으로
                    oneTimeMove(selectedTime, title, cal, dateData, y, m, d, -1)
                }
                R.id.right -> { // 우측 화살표 누르면 하루 뒤로
                    oneTimeMove(selectedTime, title, cal, dateData, y, m, d, 1)
                }
            }
//            Log.d(TAG, "dateDataFormatted : $dateDataFormatted")
        }
    }
}