package com.seahahn.routinemaker.stts

import android.os.Bundle
import android.util.Log
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.DateViewModel
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.stts.week.RecordRtAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*

class SttsWeekFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    private val rfServiceViewModel by activityViewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델
    lateinit var service : RetrofitService

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val recordViewModel by activityViewModels<RecordViewModel>() // 루틴, 할 일 목록 데이터(과거)
    private val dateViewModel by activityViewModels<DateViewModel>()

    lateinit var parsedDate : LocalDate
    lateinit var firstDayOfWeek : LocalDate

    private lateinit var tableLayout : TableLayout
    val layoutParams = TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
    val formatterWeek : DateTimeFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())

    lateinit var sun : TextView
    lateinit var mon : TextView
    lateinit var tue : TextView
    lateinit var wed : TextView
    lateinit var thu : TextView
    lateinit var fri : TextView
    lateinit var sat : TextView
    var dayOfWeekList = mutableListOf<TextView>()

    lateinit var sunTotal : TextView
    lateinit var monTotal : TextView
    lateinit var tueTotal : TextView
    lateinit var wedTotal : TextView
    lateinit var thuTotal : TextView
    lateinit var friTotal : TextView
    lateinit var satTotal : TextView
    var dayOfWeekTotalList = mutableListOf<TextView>()

    lateinit var sunDone : TextView
    lateinit var monDone : TextView
    lateinit var tueDone : TextView
    lateinit var wedDone : TextView
    lateinit var thuDone : TextView
    lateinit var friDone : TextView
    lateinit var satDone : TextView
    var dayOfWeekDoneList = mutableListOf<TextView>()

    private lateinit var viewEmptyList : LinearLayout

    private lateinit var recordList: RecyclerView
    private lateinit var recordRtAdapter: RecordRtAdapter
    var mDatas = mutableListOf<RtData>()
    var showDatas = mutableListOf<RtData>()
    lateinit var it_mDatas : Iterator<RtData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stts_week, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.table_layout) // 총 루틴 수 / 수행 완료 루틴 표시할 테이블
        sun = view.findViewById(R.id.sun)
        mon = view.findViewById(R.id.mon)
        tue = view.findViewById(R.id.tue)
        wed = view.findViewById(R.id.wed)
        thu = view.findViewById(R.id.thu)
        fri = view.findViewById(R.id.fri)
        sat = view.findViewById(R.id.sat)
        dayOfWeekList = mutableListOf(sun, mon, tue, wed, thu, fri, sat)

        sunTotal = view.findViewById(R.id.sunTotal)
        monTotal = view.findViewById(R.id.monTotal)
        tueTotal = view.findViewById(R.id.tueTotal)
        wedTotal = view.findViewById(R.id.wedTotal)
        thuTotal = view.findViewById(R.id.thuTotal)
        friTotal = view.findViewById(R.id.friTotal)
        satTotal = view.findViewById(R.id.satTotal)
        dayOfWeekTotalList = mutableListOf(sunTotal, monTotal, tueTotal, wedTotal, thuTotal, friTotal, satTotal)

        sunDone = view.findViewById(R.id.sunDone)
        monDone = view.findViewById(R.id.monDone)
        tueDone = view.findViewById(R.id.tueDone)
        wedDone = view.findViewById(R.id.wedDone)
        thuDone = view.findViewById(R.id.thuDone)
        friDone = view.findViewById(R.id.friDone)
        satDone = view.findViewById(R.id.satDone)
        dayOfWeekDoneList = mutableListOf(sunDone, monDone, tueDone, wedDone, thuDone, friDone, satDone)

        viewEmptyList = view.findViewById(R.id.view_empty_list) // 데이터 없으면 없다고 보여줄 화면

        recordList = view.findViewById(R.id.recordList) // 리사이클러뷰 초기화
        recordRtAdapter = RecordRtAdapter() // 어댑터 초기화
        recordList.adapter = recordRtAdapter // 어댑터 연결

        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "주간 통계 프래그먼트 date : $date")
            recordRtAdapter.replaceDate(date) // 루틴 수행 기록 목록에 사용자가 선택한 날짜 값 전달하기
            parsedDate = LocalDate.parse(date) // 문자열 형태의 날짜값을 LocalDate 형식으로 변환
            setShowDatas(false) // 날짜에 맞는 데이터만 목록에 출력하기
        }

        recordViewModel.recordRtData.observe(this) { rtDatas ->
            mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기
            recordRtAdapter.getAllDatas(mDatas)
            setShowDatas(true) // 날짜에 맞는 데이터만 목록에 출력하기
        }

        recordViewModel.recordActionPast.observe(this) { actionDatas ->
            // 날짜에 맞는 루틴 내 행동 데이터 목록을 행동 수행 내역을 보여주는 어댑터로 보냄
            // 현재 프래그먼트 -> 루틴 목록 어댑터 -> 루틴 목록 뷰홀더 -> 루틴 내 행동 어댑터
            recordRtAdapter.replaceActionList(actionDatas)
        }
    }

    // 날짜에 맞는 데이터만 골라서 보여주기 위한 메소드
    private fun setShowDatas(initData : Boolean) {
        showDatas.clear() // 기존 목록 비우기
//        it_mDatas = mDatas.iterator()
//        while (it_mDatas.hasNext()) {
//            val it_mData = it_mDatas.next()
//            // 사용자가 선택한 날짜에 해당하는 데이터만 출력 목록에 포함시킴
//            if (parsedDate.isEqual(LocalDate.parse(it_mData.mDate))) {
//                showDatas.add(it_mData)
//            }
//        }
        setTable()
        recordRtAdapter.replaceList(showDatas) // 추려낸 데이터의 목록을 어댑터에 보내줌


        // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
        if(recordRtAdapter.itemCount == 0) {
            if(initData) viewEmptyList.visibility = View.VISIBLE
        } else {
            viewEmptyList.visibility = View.GONE
        }
    }

    fun setTable() {
        firstDayOfWeek = parsedDate.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1)
        var totalCount: Int
        var doneCount: Int
        for(i in 0 until dayOfWeekList.size) {
            totalCount = 0
            doneCount = 0

            // 첫 행에 날짜 넣기
            dayOfWeekList[i].text = firstDayOfWeek.format(formatterWeek)

            // 선택된 날짜가 포함된 주에 해당되는 데이터들을 루틴 수행 기록 목록에 보내기 위해 따로 저장해둠
            it_mDatas = mDatas.iterator()
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                // 사용자가 선택한 날짜에 해당하는 데이터만 출력 목록에 포함시킴
                if (firstDayOfWeek.isEqual(LocalDate.parse(it_mData.mDate))) {
                    totalCount++ // 해당 날짜의 총 루틴 수
                    if(it_mData.done == 1) doneCount++ // 해당 날짜의 수행 완료 루틴 수

                    var check: Boolean // 루틴 수행 기록 목록에 들어갔는지 아닌지 여부 확인하기
                    var result = false
                    for(j in 0 until showDatas.size) {
                        check = showDatas[j].id == it_mData.id
                        if(check) result = check
                    }
                    if(!result || showDatas.size == 0) showDatas.add(it_mData) // 아이템이 없거나 루틴 고유 번호 기준으로 중복되면 포함시키지 않음
                }
            }
            // 두, 세 번째 행에 각각 총 루틴 수와 수행 완료 루틴 수 넣기
            dayOfWeekTotalList[i].text = totalCount.toString()
            dayOfWeekDoneList[i].text = doneCount.toString()

            firstDayOfWeek = firstDayOfWeek.plusDays(1)
        }
    }
}