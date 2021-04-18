package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log.d
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.ToggleEditTextView
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RtMakeActivity : Main(), MultiSelectToggleGroup.OnCheckedStateChangeListener, CompoundButton.OnCheckedChangeListener {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rt_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeRt) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initRtTodoActivity(R.id.makeRt) // 액티비티 구성 요소 초기화하기
    }

//    // 선택된 반복 요일 값 가져오기
//    override fun onCheckedStateChanged(group: MultiSelectToggleGroup?, checkedId: Int, isChecked: Boolean) {
//        val checked = group?.checkedIds
//        d(TAG, "checked : $checked")
//    }
//
//    // 시작 알람 활성화 여부 및 그룹 피드 공개 여부 값 가져오기
//    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//        when(buttonView?.id) {
//            R.id.activateAlarm -> d(TAG, "activateAlarm : $isChecked")
//            R.id.rtOnFeed -> d(TAG, "rtOnFeed : $isChecked")
//        }
//    }
}