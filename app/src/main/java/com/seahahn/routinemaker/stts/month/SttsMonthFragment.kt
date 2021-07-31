package com.seahahn.routinemaker.stts.month

import android.os.Bundle
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.DateViewModel
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.stts.RecordViewModel
import com.seahahn.routinemaker.util.Stts
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class SttsMonthFragment(mContext : Stts) : Fragment() {

    private val TAG = this::class.java.simpleName
    private val rfServiceViewModel by activityViewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델
    lateinit var service : RetrofitService
    private val context = mContext

    lateinit var parsedDate : LocalDate
//    lateinit var firstDayOfWeek : LocalDate

//    lateinit var calendar : MaterialCalendarView
    lateinit var calendarView : CalendarView
    private lateinit var currentMonth: YearMonth
    private lateinit var firstMonth: YearMonth
    private lateinit var lastMonth: YearMonth
    private val firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.KOREA).firstDayOfWeek // 한 주의 시작은 일요일
    private val formatterYM : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val recordViewModel by activityViewModels<RecordViewModel>() // 루틴, 할 일 목록 데이터(과거)
    private val dateViewModel by activityViewModels<DateViewModel>()

    // 리사이클러뷰에 데이터가 없을 때 보여줄 뷰
    private lateinit var viewEmptyList : LinearLayout

    // 루틴 수행 기록 리사이클러뷰 및 이에 들어갈 데이터와 어댑터
    private lateinit var recordList: RecyclerView
    private lateinit var recordRtAdapter: RecordRtAdapter
    private var mDatas = mutableListOf<RtData>() // 과거 루틴 수행 내역 전체
    private var showDatas = mutableListOf<RtData>() // 사용자가 선택한 기간 조건에 맞는 데이터만 골라낸 것
    private lateinit var it_mDatas : Iterator<RtData>
    private lateinit var it_mDatasForimg : Iterator<RtData>

    // 프래그먼트 첫 출력 시에 현재 날짜로 달력 초기화하고, 그 다음부터는 선택된 날짜에 따라 달력이 변동될 수 있도록 조건문을 걸기 위해 만든 변수
    private var init = false
    var totalCount = 0
    var doneCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stts_month, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context.showProgress(true)

        // 월간 수행 통계 보여줄 달력 뷰 초기화
        calendarView = view.findViewById(R.id.calendarView)
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
            }
        }
        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
//                d(TAG, "bind : " + day.date.toString())
                val date = day.date.toString()
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString() // 선택된 월에 맞는 날짜들을 각각의 자리에 넣음
                textView.alpha = if (day.owner == DayOwner.THIS_MONTH) 1f else 0.3f // 선택된 월이 아닌 월의 날짜들은 흐리게 표시

                // 날짜별 루틴 수행 결과를 날짜를 둘러싼 백그라운드 이미지를 통해서 보여줌
                if(mDatas.isNotEmpty()) bindImageToDate(day, date, textView)
            }
        }
        // 처음 초기화될 때는 오늘 날짜를 보여줌
        currentMonth = YearMonth.now()
        firstMonth = currentMonth.minusMonths(1)
        lastMonth = currentMonth.plusMonths(1)
        d(TAG, "calendarView setup : $currentMonth, $firstMonth, $lastMonth, $firstDayOfWeek")
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        viewEmptyList = view.findViewById(R.id.view_empty_list) // 데이터 없으면 없다고 보여줄 화면

        recordList = view.findViewById(R.id.recordList) // 리사이클러뷰 초기화
        recordRtAdapter = RecordRtAdapter(context) // 어댑터 초기화
        recordList.adapter = recordRtAdapter // 어댑터 연결

        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "월간 통계 프래그먼트 date : $date")
            context.showProgress(true)
            context.needToBeLoadedDataCount = 0
            context.loadedDataCount = 0

            recordRtAdapter.replaceDate(date) // 루틴 수행 기록 목록에 사용자가 선택한 날짜 값 전달하기
            val inDate = LocalDate.parse(date).format(formatterYM)

            // 프래그먼트가 처음 초기화된 경우에는 생략하고, 그 다음에 다른 월을 선택하는 등의 경우는 바뀐 날짜 값에 따라 달력을 변동시킴
            if (init) {
                currentMonth = YearMonth.parse(inDate)
                firstMonth = currentMonth.minusMonths(1)
                lastMonth = currentMonth.plusMonths(1)

                d(TAG, "calendarView setup in viewModel : $currentMonth, $firstMonth, $lastMonth, $firstDayOfWeek")
                calendarView.updateMonthRange(firstMonth, lastMonth)
                calendarView.scrollToMonth(currentMonth)
            }
            init = true // 프래그먼트가 처음 출력되면 false였던 것을 true로 바꿔서 위의 달력 변동 기능이 작동하게 함
            if(mDatas.isNotEmpty()) setShowDatas(false) // 날짜에 맞는 데이터만 목록에 출력하기
        }

        recordViewModel.recordRtData.observe(this) { rtDatas ->
            context.needToBeLoadedDataCount = 0
            context.loadedDataCount = 0

            mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기
            recordRtAdapter.getAllDatas(mDatas)
            if(mDatas.isNotEmpty()) setShowDatas(true) // 날짜에 맞는 데이터만 목록에 출력하기
        }

        recordViewModel.recordActionPast.observe(this) { actionDatas ->
            // 날짜에 맞는 루틴 내 행동 데이터 목록을 행동 수행 내역을 보여주는 어댑터로 보냄
            // 현재 프래그먼트 -> 루틴 목록 어댑터 -> 루틴 목록 뷰홀더 -> 루틴 내 행동 어댑터
            recordRtAdapter.replaceActionList(actionDatas)
        }
    }

    private fun bindImageToDate(day: CalendarDay, date: String, textView: TextView) {
        // 날짜별 루틴 수행 결과를 날짜를 둘러싼 백그라운드 이미지를 통해서 보여줌
        object : Thread() {
            override fun run() {
                d(TAG, "bindImageToDate start")
                if (day.owner == DayOwner.THIS_MONTH) {
                    it_mDatasForimg = mDatas.iterator() // 여기서는 사용자가 선택한 월에 해당하는 데이터만을 대상으로 함
                    var total = 0 // 해당 날짜의 총 루틴 수
                    var done = 0 // 해당 날짜의 수행 완료 루틴 수
                    while (it_mDatasForimg.hasNext()) {
                        val it_mData = it_mDatasForimg.next()
                        // 사용자가 선택한 월에 해당하는 데이터만 통계 수치에 반영
                        if (date == it_mData.mDate) {
                            total++
                            if(it_mData.done == 1) done++
                        }
                    }

                    // 루틴 수행 결과에 따라 날짜 배경에 표시할 이미지 모양을 다르게 출력함
                    context.runOnUiThread {
                        when {
                            total == 0 -> textView.background = null // 해당 날짜에 수행했어야 할 루틴이 없는 경우
                            done == 0 -> textView.setBackgroundResource(R.drawable.stts_red) // 루틴 한 번도 제대로 수행하지 않은 경우
                            done >= 1 && done != total -> textView.setBackgroundResource(R.drawable.stts_yellow) // 1번 이상 수행했으나 전부 다 수행하진 않은 경우
                            done == total -> textView.setBackgroundResource(R.drawable.stts_green) // 전부 다 수행한 경우
                        }
                    }
                } else {
                    context.runOnUiThread {
                        textView.background = null
                    }
                }
            }
        }.start()
    }

    // 날짜에 맞는 데이터만 골라서 보여주기 위한 메소드
    private fun setShowDatas(initData : Boolean) {
//        d(TAG, "setTable")
        showDatas.clear() // 기존 목록 비우기

        var totalCount: Int
        var doneCount: Int
        object : Thread() {
            override fun run() {
                for(i in 0 until currentMonth.lengthOfMonth()) {
                    totalCount = 0
                    doneCount = 0

                    // 선택된 날짜가 포함된 주에 해당되는 데이터들을 루틴 수행 기록 목록에 보내기 위해 따로 저장해둠
                    it_mDatas = mDatas.iterator()
                    while (it_mDatas.hasNext()) {
                        val it_mData = it_mDatas.next()
                        // 사용자가 선택한 월에 해당하는 데이터만 출력 목록에 포함시킴
                        if (currentMonth == YearMonth.parse(LocalDate.parse(it_mData.mDate).format(formatterYM).toString())) {
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
                }

                context.needToBeLoadedDataCount = showDatas.size // 로딩되어야 할 데이터 갯수
                if(showDatas.isEmpty()) context.showProgress(false) // 보여줄 데이터 없으면 프로그레스바 출력 안함

                context.runOnUiThread {
                    recordRtAdapter.replaceList(showDatas) // 추려낸 데이터의 목록을 어댑터에 보내줌

                    // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
                    if(recordRtAdapter.itemCount == 0) {
                        if(initData) viewEmptyList.visibility = View.VISIBLE
                    } else {
                        viewEmptyList.visibility = View.GONE
                    }
                }
            }
        }.start()
    }
}