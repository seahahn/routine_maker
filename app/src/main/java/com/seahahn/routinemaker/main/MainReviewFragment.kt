package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.seahahn.routinemaker.R


class MainReviewFragment : Fragment() {

    private val TAG = this::class.java.simpleName

    // 액티비티로부터 날짜 데이터를 가져오는 뷰모델
    private val viewModel: DateViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedDate.observe(this, Observer { date ->
            Log.d(TAG, "회고 프래그먼트 date : $date")
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_main_review, container, false)

        return view
    }

}