package com.seahahn.routinemaker.stts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.DateViewModel

class SttsCountFragment : Fragment() {

    private val TAG = this::class.java.simpleName

    // 액티비티로부터 날짜 데이터를 가져오는 뷰모델
    private val viewModel by activityViewModels<DateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedDate.observe(this, Observer { date ->
            Log.d(TAG, "통계 프래그먼트 date : $date")
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stts_count, container, false)
    }

}