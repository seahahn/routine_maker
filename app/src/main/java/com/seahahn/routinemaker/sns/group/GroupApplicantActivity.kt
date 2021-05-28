package com.seahahn.routinemaker.sns.group

import android.os.Bundle
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns

/*
* 그룹 가입 신청자 목록
*/
class GroupApplicantActivity : Sns() {

    private val TAG = this::class.java.simpleName

//    private lateinit var joinListAdapter: ActionAdapter
//    private lateinit var joinList: RecyclerView
//    var mDatas = mutableListOf<ActionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_applicant)

        // 레트로핏 통신 연결
        service = initRetrofit()
    }
}