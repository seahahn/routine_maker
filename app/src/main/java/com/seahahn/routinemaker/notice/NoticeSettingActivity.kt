package com.seahahn.routinemaker.notice

import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.CompoundButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserSetting.getNotiSetChat
import com.seahahn.routinemaker.util.UserSetting.getNotiSetCmt
import com.seahahn.routinemaker.util.UserSetting.getNotiSetLike
import com.seahahn.routinemaker.util.UserSetting.getNotiSetRtStart
import com.seahahn.routinemaker.util.UserSetting.setNotiSetChat
import com.seahahn.routinemaker.util.UserSetting.setNotiSetCmt
import com.seahahn.routinemaker.util.UserSetting.setNotiSetLike
import com.seahahn.routinemaker.util.UserSetting.setNotiSetRtDoneYet
import com.seahahn.routinemaker.util.UserSetting.setNotiSetRtStart

class NoticeSettingActivity : Sns(), View.OnClickListener {

    private val TAG = this::class.java.simpleName

    lateinit var notiSetAll : SwitchMaterial
    lateinit var notiSetRtStart : SwitchMaterial
//    lateinit var notiSetRtDoneYet : SwitchMaterial
    lateinit var notiSetCmt : SwitchMaterial
    lateinit var notiSetLike : SwitchMaterial
    lateinit var notiSetChat : SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noti_setting)

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.notiSetting) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.notibtm
        btmnav.setOnNavigationItemSelectedListener(this)
        setBtmNavBadge()

        initSwitches()
    }

    // 각각의 알림 설정을 위한 스위치 초기화
    private fun initSwitches() {
        notiSetAll = findViewById(R.id.notiSetAll)
        notiSetRtStart = findViewById(R.id.notiSetRtStart)
//        notiSetRtDoneYet = findViewById(R.id.notiSetRtDoneYet)
        notiSetCmt = findViewById(R.id.notiSetCmt)
        notiSetLike = findViewById(R.id.notiSetLike)
        notiSetChat = findViewById(R.id.notiSetChat)

//        notiSetAll.setOnCheckedChangeListener(this)
        notiSetAll.setOnClickListener(this)
        notiSetRtStart.setOnCheckedChangeListener(this)
//        notiSetRtDoneYet.setOnCheckedChangeListener(this)
        notiSetCmt.setOnCheckedChangeListener(this)
        notiSetLike.setOnCheckedChangeListener(this)
        notiSetChat.setOnCheckedChangeListener(this)

        notiSetRtStart.isChecked = getNotiSetRtStart(this)
//        notiSetRtDoneYet.isChecked = getNotiSetRtDoneYet(this)
        notiSetCmt.isChecked = getNotiSetCmt(this)
        notiSetLike.isChecked = getNotiSetLike(this)
        notiSetChat.isChecked = getNotiSetChat(this)

        // 모두 true 이면 전체 알림 스위치도 true로, 하나라도 false이면 전체 알림 스위치도 false로 초기화
        isAllSwitchesChecked()
    }

    // 모든 스위치 값이 true인지 아닌지 확인하여 전체 알림 스위치의 값도 그에 맞게 만듦
    private fun isAllSwitchesChecked() {
        notiSetAll.isChecked = notiSetRtStart.isChecked &&
//                notiSetRtDoneYet.isChecked &&
                notiSetCmt.isChecked && notiSetLike.isChecked && notiSetChat.isChecked
    }

    private fun checkForNotiAllSwitch(isChecked: Boolean) {
        if(isChecked) { // true이면 모든 스위치가 다 true인지 확인 후에 이에 맞춰 전체 알림 스위치 값도 조정함
            isAllSwitchesChecked()
        } else { // 하나라도 false이면 전체 알림 스위치도 false로 만듦
            notiSetAll.isChecked = false
        }
    }

    // 각각의 스위치의 값에 따라 설정값을 업데이트함
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        d(TAG, "isChecked : $isChecked")
        when(buttonView?.id) {
            R.id.notiSetRtStart -> {
                d(TAG, "notiSetRtStart isChecked : $isChecked")
                notiSetRtStart.isChecked = isChecked
                setNotiSetRtStart(this, isChecked)
                checkForNotiAllSwitch(isChecked)
            }
//            R.id.notiSetRtDoneYet -> {
//                d(TAG, "notiSetRtDoneYet isChecked : $isChecked")
//                notiSetRtDoneYet.isChecked = isChecked
//                setNotiSetRtDoneYet(this, isChecked)
//                checkForNotiAllSwitch(isChecked)
//            }
            R.id.notiSetCmt -> {
                d(TAG, "notiSetCmt isChecked : $isChecked")
                notiSetCmt.isChecked = isChecked
                setNotiSetCmt(this, isChecked)
                checkForNotiAllSwitch(isChecked)
            }
            R.id.notiSetLike -> {
                d(TAG, "notiSetLike isChecked : $isChecked")
                notiSetLike.isChecked = isChecked
                setNotiSetLike(this, isChecked)
                checkForNotiAllSwitch(isChecked)
            }
            R.id.notiSetChat -> {
                d(TAG, "notiSetChat isChecked : $isChecked")
                notiSetChat.isChecked = isChecked
                setNotiSetChat(this, isChecked)
                checkForNotiAllSwitch(isChecked)
            }
        }
    }

    // 전체 알림의 경우 스위치 값의 상태가 아닌 클릭할 경우에 스위치들의 값을 바꾸도록 함
    // 다른 스위치의 값을 바꾸는 과정에서 전체 알림 스위치의 값이 바뀔 경우, 이로 인해 건드리지 않은 나머지 스위치들의 값까지 같이 변동하기 때문
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.notiSetAll -> {
                val isChecked = notiSetAll.isChecked

                notiSetRtStart.isChecked = isChecked
//                notiSetRtDoneYet.isChecked = isChecked
                notiSetCmt.isChecked = isChecked
                notiSetLike.isChecked = isChecked
                notiSetChat.isChecked = isChecked

                setNotiSetRtStart(this, isChecked)
                setNotiSetRtDoneYet(this, isChecked)
                setNotiSetCmt(this, isChecked)
                setNotiSetLike(this, isChecked)
                setNotiSetChat(this, isChecked)
            }
        }
    }
}