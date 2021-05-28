package com.seahahn.routinemaker.stts.day

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
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.*
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.stts.RecordViewModel
import java.time.LocalDate

class SttsDayFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    private val rfServiceViewModel by activityViewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델
    lateinit var service : RetrofitService

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val recordViewModel by activityViewModels<RecordViewModel>() // 루틴, 할 일 목록 데이터(과거)
    private val dateViewModel by activityViewModels<DateViewModel>()

    lateinit var parsedDate : LocalDate

    private lateinit var totalRtNum : TextView
    private lateinit var totalRtDoneNum : TextView

    private lateinit var viewEmptyList : LinearLayout

    private lateinit var recordList: RecyclerView
    private lateinit var recordRtAdapter: RecordRtAdapter
    var mDatas = mutableListOf<RtData>()
    var showDatas = mutableListOf<RtData>()
    lateinit var it_mDatas : Iterator<RtData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stts_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalRtNum = view.findViewById(R.id.totalRtNum) // 총 루틴 갯수 표시
        totalRtDoneNum = view.findViewById(R.id.totalRtDoneNum) // 수행 완료 루틴 갯수 표시
        viewEmptyList = view.findViewById(R.id.view_empty_list) // 데이터 없을 때 표시될 뷰

        recordList = view.findViewById(R.id.recordList) // 리사이클러뷰 초기화
        recordRtAdapter = RecordRtAdapter() // 어댑터 초기화
        recordList.adapter = recordRtAdapter // 어댑터 연결

        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "일간 통계 프래그먼트 date : $date")
            recordRtAdapter.replaceDate(date) // 루틴 수행 기록 목록에 사용자가 선택한 날짜 값 전달하기
            parsedDate = LocalDate.parse(date) // 문자열 형태의 날짜값을 LocalDate 형식으로 변환
            setShowDatas(false) // 날짜에 맞는 데이터만 목록에 출력하기
        }

        recordViewModel.recordRtData.observe(this) { rtDatas ->
            mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기
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
        it_mDatas = mDatas.iterator()
        while (it_mDatas.hasNext()) {
            val it_mData = it_mDatas.next()
            // 사용자가 선택한 날짜에 해당하는 데이터만 출력 목록에 포함시킴
            if (parsedDate.isEqual(LocalDate.parse(it_mData.mDate))) {
                showDatas.add(it_mData)
            }
        }
        recordRtAdapter.replaceList(showDatas) // 추려낸 데이터의 목록을 어댑터에 보내줌

        totalRtNum.text = recordRtAdapter.itemCount.toString() // 총 루틴 갯수 값 넣기
        totalRtDoneNum.text = recordRtAdapter.getItemDoneCount().toString() // 수행 완료 루틴 갯수 값 넣기

        // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
        if(recordRtAdapter.itemCount == 0) {
            if(initData) viewEmptyList.visibility = View.VISIBLE
        } else {
            viewEmptyList.visibility = View.GONE
        }
    }
}