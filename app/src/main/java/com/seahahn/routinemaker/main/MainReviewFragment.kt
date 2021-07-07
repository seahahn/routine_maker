package com.seahahn.routinemaker.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.util.UserInfo.getUserId


class MainReviewFragment : Fragment(), View.OnClickListener {

    private val TAG = this::class.java.simpleName
    private val rfServiceViewModel by activityViewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델
    lateinit var service : RetrofitService

    private lateinit var review : EditText // 회고 내용
    private lateinit var onPublicSwitch : SwitchMaterial // 공개 여부 스위치
    private lateinit var save : Button // 저장하기 버튼

    private lateinit var mDate : String // 사용자가 선택한 날짜

    // 액티비티로부터 날짜 데이터를 가져오는 뷰모델
    private val dateViewModel: DateViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 컨텍스트 세팅하기(메인 액티비티)
        val context = view.context as MainActivity

        // 뷰모델에 저장해둔 레트로핏 객체 가져오기
        rfServiceViewModel.rfService.observe(this) { rfService ->
            service = rfService
        }

        review = view.findViewById(R.id.review)
        onPublicSwitch = view.findViewById(R.id.onPublic)
        save = view.findViewById(R.id.save)
        save.setOnClickListener(this)

        dateViewModel.selectedDate.observe(this) { date ->
            mDate = date
            context.getReview(service, date, getUserId(context), review, onPublicSwitch) // 회고 데이터를 JsonObject(Gson)으로 받아옴
        }
    }

    override fun onClick(v: View?) {
        val context = v!!.context as MainActivity
        context.setReview(service, review.text.toString(), onPublicSwitch.isChecked, mDate)
    }
}