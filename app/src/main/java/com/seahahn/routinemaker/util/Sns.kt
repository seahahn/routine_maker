package com.seahahn.routinemaker.util

import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.seahahn.routinemaker.R
import java.time.LocalDate
import java.time.LocalTime

open class Sns : Main() {

    private val TAG = this::class.java.simpleName

    // 그룹 만들기, 정보 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    lateinit var mainTitleTxt : TextView // 그룹명(텍스트뷰)
    lateinit var headNumberTxt : TextView // 그룹 가입 인원(텍스트뷰)
    lateinit var headLimitTxt : TextView // 그룹 가입 인원 제한(텍스트뷰)
    lateinit var headLimit : EditText // 그룹 가입 인원 제한
    lateinit var headLimitResult : Editable // DB에 저장될 그룹 가입 인원 제한 값
    lateinit var isLocked : SwitchMaterial // 그룹 공개 여부
    var isLockedResult : Boolean = true // DB에 저장될 그룹 공개 여부
    lateinit var tags : EditText // 그룹 태그
    lateinit var tagstxt : Editable // DB에 저장될 그룹 태그

    // 루틴, 할 일 만들기 및 수정 액티비티 내의 공통 요소들 초기화하기
    fun initGroupActivity(btmBtnId : Int) {

        // 그룹 공개 여부 가져오기
        isLocked = findViewById(R.id.is_locked)
        isLocked.setOnCheckedChangeListener(this)

        // 액티비티별로 별개인 요소 초기화하기
        when(TAG) {
            "GroupMakeActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleInput = findViewById(R.id.mainTitleInput)
                mainTitle = mainTitleInput.text!!

                // 그룹 가입 인원 제한 값 가져오기
                headLimit = findViewById(R.id.headLimit)
                headLimitResult = headLimit.text
            }
            "GroupUpdateActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleInput = findViewById(R.id.mainTitleInput)
                mainTitle = mainTitleInput.text!!

                // 그룹 가입 인원 제한 값 가져오기
                headLimit = findViewById(R.id.headLimit)
                headLimitResult = headLimit.text
            }
            "GroupInfoActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleTxt = findViewById(R.id.mainTitleTxt)

                // 그룹 가입 인원 제한 값 가져오기
                headNumberTxt = findViewById(R.id.headNumberTxt)
                headLimitTxt = findViewById(R.id.headLimitTxt)
            }
        }

        // 태그 값 가져오기
        tags = findViewById(R.id.tags)
        tagstxt = tags.text

        // 메모 값 가져오기
        memo = findViewById(R.id.memo)
        memotxt = memo.text

        // 하단 버튼 초기화
        btmBtn = findViewById(btmBtnId)
        setFullBtmBtnText(btmBtn)
        btmBtn.setOnClickListener(BtnClickListener())
    }
}