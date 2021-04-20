package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import java.time.LocalDate


class MainRoutineFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    private val TAG = this::class.java.simpleName

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val rtTodoViewModel by activityViewModels<RtTodoViewModel>() // 루틴, 할 일 목록 데이터
    private val dateViewModel by activityViewModels<DateViewModel>() // 날짜 데이터

    private lateinit var showAll: CheckBox

    private lateinit var rtAdapter: RtAdapter
    private lateinit var rtList: RecyclerView
    var mDatas = mutableListOf<RtData>()
    var showDatas = mutableListOf<RtData>()
    lateinit var it_mDatas : Iterator<RtData>

    val today = LocalDate.now()
    lateinit var parsedDate : LocalDate
    lateinit var dayOfWeek : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        d(TAG, "rt fragment onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        showAll = view.findViewById(R.id.showAll) // 전체 목록 보기 체크박스 초기화하기
        showAll.setOnCheckedChangeListener(this)

        // 루틴, 할 일 목록 리사이클러뷰 및 어댑터 초기화하기
        rtList = view.findViewById(R.id.rtList) // 리사이클러뷰 초기화
        rtAdapter = RtAdapter() // 어댑터 초기화
        rtList.adapter = rtAdapter // 어댑터 연결

        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "루틴 프래그먼트 date : $date")
            parsedDate = LocalDate.parse(date)
            d(TAG, "루틴 프래그먼트 parsedDate 날짜: $parsedDate")
            d(TAG, "루틴 프래그먼트 parsedDate 요일 : " + parsedDate.dayOfWeek)

            when (parsedDate.dayOfWeek.toString()) {
                "SUNDAY" -> dayOfWeek = "일"
                "MONDAY" -> dayOfWeek = "월"
                "TUESDAY" -> dayOfWeek = "화"
                "WEDNESDAY" -> dayOfWeek = "수"
                "THURSDAY" -> dayOfWeek = "목"
                "FRIDAY" -> dayOfWeek = "금"
                "SATURDAY" -> dayOfWeek = "토"
            }

            // 루틴, 할 일 전체 목록 데이터 가져오기
            rtTodoViewModel.gottenRtData.observe(this) { rtDatas ->
                d(TAG, "루틴 프래그먼트 rtDatas : $rtDatas")
                mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

                // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
                showDatas.clear()
                it_mDatas = mDatas.iterator()
                d(TAG, "it_mDatas : $it_mDatas")
                while (it_mDatas.hasNext()) {
                    val it_mData = it_mDatas.next()
                    d(TAG, "it_mData : $it_mData")
                    // 루틴일 경우 : 오늘 날짜의 요일이 루틴 수행 요일과 맞으면 보여주기 / 할 일인 경우 : 날짜가 맞으면 보여주기
                    if (it_mData.mType == "rt" && dayOfWeek in it_mData.mDays && it_mData.date == today.toString()) {
                        showDatas.add(it_mData)
                    } else if (it_mData.mType == "todo" && it_mData.date == parsedDate.toString()) {
                        showDatas.add(it_mData)
                    }
                    // 할 일에 반복 여부 있는 경우에는 해당 할 일을 수행하고 나면 그 다음에 반복해야 할 요일의 날짜를 date에 집어넣어 수정한다
                }

                // 어댑터에 가져온 데이터 연결하여 출력하기
                // '전체 목록 보기' 여부에 따라 전체를 보여줄지, 해당 요일 데이터만 보여줄지 결정함
                if (showAll.isChecked) {
                    rtAdapter.replaceList(mDatas)
                } else {
                    rtAdapter.replaceList(showDatas) // 받은 날짜 정보에 맞춰서 목록 띄우기
                }

//            rtAdapter.replaceList(showDatas)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) {
            rtAdapter.replaceList(mDatas)
        } else {
            rtAdapter.replaceList(showDatas)
        }
    }
}
