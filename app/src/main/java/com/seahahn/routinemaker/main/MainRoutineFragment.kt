package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R


class MainRoutineFragment : Fragment() {

    private val TAG = this::class.java.simpleName

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val dateViewModel by activityViewModels<DateViewModel>() // 날짜 데이터
    private val rtTodoViewModel by activityViewModels<RtTodoViewModel>() // 루틴, 할 일 목록 데이터

    private lateinit var rtAdapter: RtAdapter
    private lateinit var rtList: RecyclerView
    var mDatas = mutableListOf<RtData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_main_routine, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        d(TAG, "rt fragment onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        dateViewModel.selectedDate.observe(this, Observer { date ->
            d(TAG, "루틴 프래그먼트 date : $date")
        })

        rtTodoViewModel.gottenRtData.observe(this, Observer { rtDatas ->
//            d(TAG, "루틴 프래그먼트 rtDatas : $rtDatas")
            mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            rtList = view.findViewById(R.id.rtList) // 리사이클러뷰 초기화
            rtAdapter = RtAdapter() // 어댑터 초기화
            rtList.adapter = rtAdapter // 어댑터 연결

//            d(TAG, "루틴 프래그먼트 mDatas : $mDatas")
            rtAdapter.replaceList(mDatas) // 어댑터에 가져온 데이터 연결하여 출력하기
        })
    }
}