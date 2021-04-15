package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.DateViewModel


class MainRoutineFragment : Fragment() {

    private val TAG = this::class.java.simpleName

    // 액티비티로부터 날짜 데이터를 가져오는 뷰모델
    private val viewModel by activityViewModels<DateViewModel>()

    private lateinit var rtAdapter: RtAdapter
    private lateinit var rtList: RecyclerView
    var mDatas = mutableListOf<RtData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedDate.observe(this, Observer { date ->
            d(TAG, "루틴 프래그먼트 date : $date")
        })
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

        rtList = view.findViewById(R.id.rtList)
        rtAdapter = RtAdapter()
        rtList.adapter = rtAdapter

        for(i in 1..9) {
            d(TAG, "rt list data $i")
            mDatas.add(RtData(i, "test $i", listOf("월", "수", "금"), "05:0$i"))
        }

        rtAdapter.replaceList(mDatas)
    }
}