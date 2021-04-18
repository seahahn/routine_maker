package com.seahahn.routinemaker.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class RtUpdateActivity : Main(), MultiSelectToggleGroup.OnCheckedStateChangeListener, CompoundButton.OnCheckedChangeListener {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rt_update)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.updateRt) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initRtTodoActivity(R.id.updateRt)

        rtId = intent.getIntExtra("id", 0)

        getRt(service, rtId)
    }

//    // 선택된 반복 요일 값 가져오기
//    override fun onCheckedStateChanged(group: MultiSelectToggleGroup?, checkedId: Int, isChecked: Boolean) {
//        val checked = group?.checkedIds
//        Log.d(TAG, "checked : $checked")
//    }
//
//    // 시작 알람 활성화 여부 및 그룹 피드 공개 여부 값 가져오기
//    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//        when(buttonView?.id) {
//            R.id.activateAlarm -> Log.d(TAG, "activateAlarm : $isChecked")
//            R.id.rtOnFeed -> Log.d(TAG, "rtOnFeed : $isChecked")
//        }
//    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_trash, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }
}