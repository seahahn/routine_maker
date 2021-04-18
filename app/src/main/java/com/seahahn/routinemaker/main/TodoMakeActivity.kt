package com.seahahn.routinemaker.main

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main


class TodoMakeActivity : Main() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_make)

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeTodo) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

//        // 할 일 이름 입력값 가져오기
//        val mainTitleInput = findViewById<TextInputEditText>(R.id.mainTitleInput)
//        val mainTitle = mainTitleInput.text
//
//        // 할 일 반복 요일 선택값 가져오기
//        val mainDays = findViewById<MultiSelectToggleGroup>(R.id.mainDays)
//        mainDays.setOnCheckedChangeListener(this)
//
//        // 알람 활성화 여부 가져오기
//        val activateAlarm = findViewById<SwitchMaterial>(R.id.activateAlarm)
//        activateAlarm.setOnCheckedChangeListener(this)
//
//        // 수행 예정 시각 값 가져오기
//        val startTime = findViewById<TextView>(R.id.startTime)
//        startTime.text = formattedHM
//        startTime.setOnClickListener(BtnClickListener())
//
//        // 메모 값 가져오기
//        val memo = findViewById<EditText>(R.id.memo)
//        val memotxt = memo.text
//
//        // 하단 '할 일 만들기' 버튼 초기화
//        val makeTodo = findViewById<Button>(R.id.makeTodo)
//        setFullBtmBtnText(makeTodo)
//        makeTodo.setOnClickListener(BtnClickListener())

        initRtTodoActivity(R.id.makeTodo)
    }

//    // 선택된 반복 요일 값 가져오기
//    override fun onCheckedStateChanged(group: MultiSelectToggleGroup?, checkedId: Int, isChecked: Boolean) {
//        val checked = group?.checkedIds
//        Log.d(TAG, "checked : $checked")
//    }
//
//    // 알람 활성화 여부 값 가져오기
//    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
//        when(buttonView?.id) {
//            R.id.activateAlarm -> Log.d(TAG, "activateAlarm : $isChecked")
//            R.id.rtOnFeed -> Log.d(TAG, "rtOnFeed : $isChecked")
//        }
//    }
}