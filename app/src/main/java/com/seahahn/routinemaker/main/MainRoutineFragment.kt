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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            d(TAG, "루틴 프래그먼트 rtDatas : $rtDatas")
            mDatas = rtDatas

            rtList = view.findViewById(R.id.rtList)
            rtAdapter = RtAdapter()
            rtList.adapter = rtAdapter

            d(TAG, "루틴 프래그먼트 mDatas : $mDatas")
            rtAdapter.replaceList(mDatas)
        })



//        for(i in 1..999) {
////            d(TAG, "rt list data $i")
//            mDatas.add(RtData(i, "rt", "test $i",
//                listOf("월", "수", "금"), true, "05:00", true,
//                "테스트 메모", 30, "2021-04-18 16:07:31"))
//        }

    }
}