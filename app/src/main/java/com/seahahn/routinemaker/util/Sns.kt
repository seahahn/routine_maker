package com.seahahn.routinemaker.util

import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.main.RtTodoViewModel
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.group.GroupListViewModel
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime

open class Sns : Main() {

    private val TAG = this::class.java.simpleName

    val groupListViewModel by viewModels<GroupListViewModel>() // 루틴, 할 일 목록 데이터

    // 그룹 만들기, 정보 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    var groupId : Int = 0 // DB내 루틴 또는 할 일 고유 번호. 루틴 또는 할 일 수정 시에 필요
    lateinit var mainTitleTxt : TextView // 그룹명(텍스트뷰)
    lateinit var headNumberTxt : TextView // 그룹 가입 인원(텍스트뷰)
    lateinit var headLimitTxt : TextView // 그룹 가입 인원 제한(텍스트뷰)
    lateinit var headLimit : EditText // 그룹 가입 인원 제한
    lateinit var headLimitResult : Editable // DB에 저장될 그룹 가입 인원 제한 값
    lateinit var headLimitUnit : TextView // 그룹 가입 인원 제한 우측에 단위 '명' 표시
    lateinit var isLimited : SwitchMaterial // 인원 제한 여부
    var isLimitedResult : Boolean = false
    lateinit var isLocked : SwitchMaterial // 그룹 공개 여부
    var isLockedResult : Boolean = true // DB에 저장될 그룹 공개 여부
    lateinit var tags : EditText // 그룹 태그
    lateinit var tagstxt : Editable // DB에 저장될 그룹 태그

    var tagList = mutableListOf<String>()
    var members = mutableListOf<String>()

    // 루틴, 할 일 만들기 및 수정 액티비티 내의 공통 요소들 초기화하기
    fun initGroupActivity(btmBtnId : Int) {

        // 액티비티별로 별개인 요소 초기화하기
        when(TAG) {
            "GroupMakeActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleInput = findViewById(R.id.mainTitleInput)
                mainTitle = mainTitleInput.text!!

                isLimited = findViewById(R.id.isLimited)
                isLimited.setOnCheckedChangeListener(this)

                // 그룹 가입 인원 제한 값 가져오기
                headLimit = findViewById(R.id.headLimit)
                headLimitUnit = findViewById(R.id.headLimitUnit)
                headLimit.setText(getString(R.string.zero))
                headLimitResult = headLimit.text
            }
            "GroupUpdateActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleInput = findViewById(R.id.mainTitleInput)
                mainTitle = mainTitleInput.text!!

                isLimited = findViewById(R.id.isLimited)
                isLimited.setOnCheckedChangeListener(this)

                // 그룹 가입 인원 제한 값 가져오기
                headLimit = findViewById(R.id.headLimit)
                headLimitUnit = findViewById(R.id.headLimitUnit)
                headLimitResult = headLimit.text
            }
            "GroupInfoActivity" -> {
                // 그룹명 입력값 가져오기
                mainTitleTxt = findViewById(R.id.mainTitleTxt)

                // 그룹 가입 인원 제한 값 가져오기
//                headNumberTxt = findViewById(R.id.headNumberTxt)
                headLimitTxt = findViewById(R.id.headLimitTxt)
            }
        }

        // 그룹 공개 여부 가져오기
        isLocked = findViewById(R.id.isLocked)
        isLocked.setOnCheckedChangeListener(this)

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

    // 그룹 만들기 또는 수정하기 액티비티에서 필수 입력 사항들을 입력하지 않은 경우 입력하도록 안내하기
    fun inputCheckSns(): Boolean {
        if(mainTitle.isBlank()) {
            // 그룹명을 입력하지 않은 경우 입력하도록 안내하기
            toast(getString(R.string.groupTitleEmpty))
            return false
        }
        else if(isLimitedResult && (headLimitResult.isBlank() || headLimitResult.toString().toInt() < 2)) {
            // 인원 제한 있는데 제한할 인원 수를 입력하지 않았거나 2 이상으로 입력하지 않은 경우
            toast(getString(R.string.groupHeadLimitEmpty))
            return false
        }
        return true
    }

    // 그룹 만들기 및 수정하기 액티비티에서 인원 제한 및 그룹 공개 여부 값 가져오기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.isLimited -> {
                isLimitedResult = isChecked
                if(isLimitedResult) {
                    headLimit.visibility = View.VISIBLE
                    headLimitUnit.visibility = View.VISIBLE
                } else {
                    headLimit.visibility = View.INVISIBLE
                    headLimitUnit.visibility = View.INVISIBLE
                    headLimit.setText(getString(R.string.zero))
                    headLimitResult = headLimit.text
                }
            }
            R.id.isLocked -> isLockedResult = isChecked
        }
    }

    // 액티비티 내 버튼 눌렀을 때의 동작 구현
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                // 하단에 가로로 꽉 차는 버튼인 경우
                R.id.makeGroup -> {
//                    tagstxt = tags.text
//                    headLimitResult = headLimit.text
                    d(TAG, "그룹 만들기 변수들 : $mainTitle, $tagstxt, $headLimitResult, $isLockedResult, $memotxt")
                    if(inputCheckSns()) makeGroup(service, mainTitle.toString(), tagstxt.toString(), headLimitResult.toString().toInt(),
                            getUserId(applicationContext).toString(), isLockedResult, memotxt.toString(), getUserId(applicationContext))
                }
                R.id.updateGroup -> {
                    mainTitle = mainTitleInput.text!!
                    tagstxt = tags.text
                    headLimitResult = headLimit.text
                    memotxt = memo.text
                    d(TAG, "그룹 수정 변수들 : $groupId, $mainTitle, $tagstxt, $headLimitResult, $isLockedResult, $memotxt")
                    if(inputCheckSns()) updateGroup(service, groupId, mainTitle.toString(), tagstxt.toString(), headLimitResult.toString().toInt(),
                        isLockedResult, memotxt.toString())
                }
            }
        }
    }

    // '그룹 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(그룹 만들기)
    fun makeGroup(service : RetrofitService, title : String, tags : String,
                  headLimit : Int, members : String, isLocked : Boolean, memo : String, userId : Int) {
        d(TAG, "makeGroup 변수들 : $title, $tags, $headLimit, $members, $isLocked, $memo, $userId")
//        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.makeGroup(title, tags, headLimit, members, isLocked, memo, userId).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 만들기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 만들기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        toast(msg)
                        finish()
                    }
                    false -> toast(msg)
                }
            }
        })
    }

    // '그룹 수정하기' 액티비티의 하단 버튼 눌렀을 때의 동작(그룹 수정하기)
    fun updateGroup(service : RetrofitService, id : Int, title : String, tags : String,
                    headLimit : Int, isLocked : Boolean, memo : String) {
        Log.d(TAG, "updateGroup 변수들 : $id, $title, $tags, $headLimit, $isLocked, $memo")
//        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.updateGroup(id, title, tags, headLimit, isLocked, memo).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 수정하기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 수정하기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        toast(msg)
                        finish()
                    }
                    false -> toast(msg)
                }
            }
        })
    }

    // 그룹 목록 불러오기
    fun getGroups(service: RetrofitService, userId : Int) {
        d(TAG, "getGroups 변수들 : $userId")
        // 현재, 미래 목록 가져오기
        service.getGroups(userId).enqueue(object : Callback<MutableList<GroupData>> {
            override fun onFailure(call: Call<MutableList<GroupData>>, t: Throwable) {
                Log.d(TAG, "그룹 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<GroupData>>, response: Response<MutableList<GroupData>>) {
                Log.d(TAG, "그룹 목록 가져오기 요청 응답 수신 성공")
//                d(TAG, "getRts : "+response.body().toString())
                val groupDatas = response.body()
                try {
                    groupListViewModel.setList(groupDatas!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

    // '그룹 수정하기' 액티비티에 그룹 데이터 세팅하기
    fun getGroup(service: RetrofitService, groupId : Int) {
        Log.d(TAG, "getGroup 변수들 : $groupId")
        service.getGroup(groupId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "getGroup : " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)

                if(TAG == "GroupInfoActivity") {
                    mainTitleTxt.text = gson.get("title").asString // 텍스트뷰인 경우

                    // 그룹 인원 표시하기
                    val memberList = gson.get("members").asString.split(" ") as MutableList<String> // 문자열로 저장되어 있던 그룹 멤버 고유 번호 모음을 리스트로 변환
                    d(TAG, "memberList : $memberList")
                    val headCount = memberList.size // 현재 가입되어 있는 그룹 멤버 수
                    val headLimitValue = gson.get("headLimit").asInt // 인원 제한 수
                    if(headLimitValue > 1) {
                        headLimitTxt.text = "$headCount / $headLimitValue"
                    } else if(headLimitValue == 0){
                        headLimitTxt.text = "$headCount"
                    }

                } else {
                    mainTitleInput.setText(gson.get("title").asString) // 에딧텍스트인 경우
                    if(gson.get("headLimit").asInt == 0) {
                        isLimited.isChecked = false
                        headLimit.setText(getString(R.string.zero))
                    } else {
                        isLimited.isChecked = true
                        headLimit.setText(gson.get("headLimit").asString)
                    }
                }

                isLocked.isChecked = gson.get("isLocked").asInt == 1

                tags.setText(gson.get("tags").asString)

                memo.setText(gson.get("memo").asString)
            }
        })
    }

    // 그룹 삭제하기
    fun deleteGroup(service: RetrofitService, groupId : Int, context : Context) {
        Log.d(TAG, "deleteRt 변수들 : $rtId")
        service.deleteGroup(groupId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 삭제 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        getGroups(service, getUserId(context))
                    }
                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}