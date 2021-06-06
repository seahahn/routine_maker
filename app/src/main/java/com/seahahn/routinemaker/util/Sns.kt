package com.seahahn.routinemaker.util

import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.sns.group.*
import com.seahahn.routinemaker.util.AppVar.getAcceptedList
import com.seahahn.routinemaker.util.AppVar.getNextLeaderId
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.function.Predicate

open class Sns : Main() {

    private val TAG = this::class.java.simpleName

    val groupListViewModel by viewModels<GroupListViewModel>() // 루틴, 할 일 목록 데이터
    val groupMemberViewModel by viewModels<GroupMemberViewModel>() // 루틴, 할 일 목록 데이터

    // 그룹 만들기, 정보 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    open var groupId : Int = 0 // DB내 그룹 고유 번호
    open var leaderId : Int = 0 // DB내 그룹 생성자 고유 번호
    lateinit var mainTitleTxt : TextView // 그룹명(텍스트뷰)
    lateinit var headNumberTxt : TextView // 그룹 가입 인원(텍스트뷰)
    var headCount : Int = 0
    var headLimitValue : Int = 0
    lateinit var headLimitTxt : TextView // 그룹 가입 인원 제한(텍스트뷰)
    lateinit var headLimit : EditText // 그룹 가입 인원 제한
    lateinit var headLimitResult : Editable // DB에 저장될 그룹 가입 인원 제한 값
    lateinit var headLimitUnit : TextView // 그룹 가입 인원 제한 우측에 단위 '명' 표시
    lateinit var isLimited : SwitchMaterial // 인원 제한 여부
    var isLimitedResult : Boolean = false
    lateinit var onPublic : SwitchMaterial // 그룹 공개 여부
    var onPublicResult : Boolean = true // DB에 저장될 그룹 공개 여부
    lateinit var tags : EditText // 그룹 태그
    lateinit var tagstxt : Editable // DB에 저장될 그룹 태그
    var joined : Boolean = false // 사용자의 그룹 가입 여부
    var applied : Boolean = false // 사용자의 그룹 가입 신청 여부
    var memberCount : Int = 0

    var groupMemberListData = mutableListOf<GroupMemberData>()
    var newLeaderId : Int = 0

//    var tagList = mutableListOf<String>()
//    var members = mutableListOf<String>()

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
//                headLimit.setText(getString(R.string.zero))
//                headLimitResult = headLimit.text
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
        onPublic = findViewById(R.id.onPublic)
        onPublic.setOnCheckedChangeListener(this)

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

    // 액티비티 하단에 꽉 차는 버튼 있는 경우에 해당 버튼의 텍스트 설정하기
    override fun setFullBtmBtnText(btn: Button) {
        when(btn.id) {
            R.id.makeGroup -> btn.text = getString(R.string.makeGroup)
            R.id.updateGroup -> btn.text = getString(R.string.updateGroup)
            R.id.groupInfo -> {
                when {
                    leaderId == getUserId(this) -> {
                        if(headCount > 1) { // 다른 그룹 멤버가 있으면 그룹 리더를 넘기기
                            btn.text = getString(R.string.changeGroupLeader)
                        } else { // 혼자면 바로 그룹 해체 가능
                            btn.text = getString(R.string.deleteGroup)
                        }
                    }
                    joined -> btn.text = getString(R.string.quitGroup) // 그룹 가입되어 있는 경우
                    applied -> btn.text = getString(R.string.cancelJoinGroup) // 그룹 가입 신청되어 있는 경우
                    !onPublicResult -> btn.text = getString(R.string.applyJoinGroup) // 그룹 가입 안 되어 있고, 그룹이 비공개인 경우
                    else -> btn.text = getString(R.string.joinGroup) // 그룹 가입 안 되어 있고, 그룹이 공개인 경우
                }
            }
            R.id.allowJoin -> btn.text = getString(R.string.acceptJoin)
        }
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

    // 툴바 우측 버튼 눌렀을 때의 동작 구현
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected Sns")
        when(item.itemId){
            R.id.toolbarTrash -> {
                showAlert("그룹 해체하기", "정말 해체하시겠어요?")
            }
            R.id.toolbarUpdate -> {
                when(TAG) {
                    "GroupInfoActivity" -> startActivity<GroupUpdateActivity>("id" to groupId) // '그룹 정보 수정하기' 액티비티로 이동
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 그룹 해체 시 재확인 받는 다이얼로그 띄우기
    override fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                if(TAG == "GroupUpdateActivity") {
                    deleteGroup(service, groupId, this)
                    finish()
                } else if(TAG == "GroupInfoActivity") {
                    when {
                        leaderId == getUserId(applicationContext) -> {
                            if(headCount > 1) { // 리더 넘기기
//                                selectNextLeader()
                            } else { // 그룹 해체하기
                                deleteGroup(service, groupId, this)
                                finish()
                                startActivity<GroupListActivity>()
                            }
                        }
                        joined || applied -> {
                            // 그룹 탈퇴하기
                            quitGroup(service, groupId, getUserId(applicationContext))
                            finish()
                            startActivity<GroupListActivity>()
                        }
//                        else -> {
//                            // 그룹 가입하기
//                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
            .show()
    }

    // 그룹 생성자가 그룹 리더를 넘기고 싶은 경우
    fun selectNextLeader() {
        val inf = layoutInflater
        val dialogView = inf.inflate(R.layout.dialog_select_next_group_leader, null)

        val groupMemberList = dialogView.findViewById<RecyclerView>(R.id.groupMemberList) // 리사이클러뷰 초기화
        val groupMemberListAdapter = GroupMemberListAdapter() // 어댑터 초기화
        groupMemberList.adapter = groupMemberListAdapter // 어댑터 연결
        groupMemberListAdapter.getService(service)
        val predicate: Predicate<GroupMemberData> = Predicate<GroupMemberData> { data -> data.id == getUserId(this) }
        groupMemberListData.removeIf(predicate)
        groupMemberListAdapter.replaceList(groupMemberListData) // 사용자 고유 번호에 맞춰서 가입한 그룹 목록 띄우기

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.selectNextLeaderTitle)
            .setMessage(R.string.selectNextLeaderMsg)
            .setView(dialogView)
            .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                newLeaderId = getNextLeaderId(applicationContext)
                d(TAG, "pos newLeaderId : $newLeaderId")
                setGroupLeader(service, groupId, newLeaderId)
                finish()
                startActivity<GroupListActivity>()
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
        builder.create()
        builder.show()
    }

    // 그룹 만들기 및 수정하기 액티비티에서 인원 제한 및 그룹 공개 여부 값 가져오기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.isLimited -> {
                isLimitedResult = isChecked
                if(isLimitedResult) {
                    headLimit.visibility = View.VISIBLE
                    headLimitUnit.visibility = View.VISIBLE
                    headLimit.setText("")
                } else {
                    headLimit.visibility = View.INVISIBLE
                    headLimitUnit.visibility = View.INVISIBLE
                    headLimit.setText(getString(R.string.zero))
                    headLimitResult = headLimit.text
                }
            }
            R.id.onPublic -> onPublicResult = isChecked
        }
    }

    // 액티비티 내 버튼 눌렀을 때의 동작 구현
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                // 하단에 가로로 꽉 차는 버튼인 경우
                R.id.makeGroup -> {
                    headLimitResult = headLimit.text
                    d(TAG, "그룹 만들기 변수들 : $mainTitle, $tagstxt, $headLimitResult, $onPublicResult, $memotxt")
                    if(inputCheckSns()) makeGroup(service, mainTitle.toString(), tagstxt.toString(), headLimitResult.toString().toInt(),
                        onPublicResult, memotxt.toString(), getUserId(applicationContext))
                }
                R.id.updateGroup -> {
                    mainTitle = mainTitleInput.text!!
                    tagstxt = tags.text
                    headLimitResult = headLimit.text
                    memotxt = memo.text
                    d(TAG, "그룹 수정 변수들 : $groupId, $mainTitle, $tagstxt, $headLimitResult, $onPublicResult, $memotxt")
                    if(inputCheckSns()) updateGroup(service, groupId, mainTitle.toString(), tagstxt.toString(), headLimitResult.toString().toInt(),
                        onPublicResult, memotxt.toString())
                }
                R.id.groupInfo -> {
                    when {
                        leaderId == getUserId(applicationContext) -> {
                            if(headCount > 1) { // 리더 넘기기
//                                showAlert("그룹 리더 변경하기", "정말 변경하시겠어요?")
                                selectNextLeader()
                            } else { // 그룹 해체하기
                                showAlert("그룹 해체하기", "정말 해체하시겠어요?")
                            }
                        }
                        joined -> {
                            // 그룹 탈퇴하기
                            showAlert("그룹 탈퇴하기", "정말 탈퇴하시겠어요?")
                        }
                        applied -> {
                            showAlert("그룹 가입 신청 취소하기", "정말 취소하시겠어요?")
                        }
                        else -> {
                            // 그룹 가입하기
                            joinGroup(service, groupId, getUserId(applicationContext), onPublicResult)
                        }
                    }
                }
                R.id.allowJoin -> {
                    val acceptedListString = getAcceptedList(applicationContext) // 문자열로 저장해뒀던 가입 신청 승인 대상자 목록을 가져옴
                    d(TAG, "getAcceptedList : $acceptedListString")
                    // 문자열로 되어 있는 것을 Int 배열로 변환
                    val acceptedList = Arrays.stream(acceptedListString.substring(1, acceptedListString.length - 1).split(",").toTypedArray())
                        .map { obj: String -> obj.trim { it <= ' ' } }.mapToInt(Integer::parseInt).toArray()
                    val acceptableCount = headLimitValue - headCount
                    d(TAG, "acceptableCount : $acceptableCount")
                    d(TAG, "acceptedList.size : "+acceptedList.size)
                    if(acceptedList.size > acceptableCount) {
                        Toast.makeText(applicationContext, R.string.acceptJoinImpMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        for(element in acceptedList) {
                            acceptJoinGroup(service, groupId, element)
                        }
                        Toast.makeText(applicationContext, R.string.acceptJoinMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // '그룹 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(그룹 만들기)
    fun makeGroup(service : RetrofitService, title : String, tags : String,
                  headLimit : Int, onPublic : Boolean, memo : String, userId : Int) {
        d(TAG, "makeGroup 변수들 : $title, $tags, $headLimit, $onPublic, $memo, $userId")
//        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.makeGroup(title, tags, headLimit, onPublic, memo, userId).enqueue(object :
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
                    headLimit : Int, onPublic : Boolean, memo : String) {
        Log.d(TAG, "updateGroup 변수들 : $id, $title, $tags, $headLimit, $onPublic, $memo")
//        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.updateGroup(id, title, tags, headLimit, onPublic, memo).enqueue(object : Callback<JsonObject> {
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
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        finish()
                        startActivity<GroupListActivity>()
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 그룹 목록 불러오기
    fun getGroups(service: RetrofitService, userId : Int) {
        d(TAG, "getGroups 변수들 : $userId")
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

    fun getGroupMembers(service: RetrofitService, groupId : Int, joined : Boolean) {
        d(TAG, "getGroupMembers 변수들 : $groupId, $joined")
        service.getGroupMembers(groupId, joined).enqueue(object : Callback<MutableList<GroupMemberData>> {
            override fun onFailure(call: Call<MutableList<GroupMemberData>>, t: Throwable) {
                Log.d(TAG, "그룹 멤버 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<GroupMemberData>>, response: Response<MutableList<GroupMemberData>>) {
                Log.d(TAG, "그룹 멤버 목록 가져오기 요청 응답 수신 성공")
                d(TAG, "getGroupMembers : "+response.body().toString())
//                groupMemberListData = response.body()!!
                val groupMemberDatas = response.body()
                try {
                    groupMemberViewModel.setList(groupMemberDatas!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

    fun setGroupLeader(service: RetrofitService, groupId : Int, newLeaderId : Int) {
        d(TAG, "setGroupLeader 변수들 : $groupId, $newLeaderId")
        service.setGroupLeader(groupId, newLeaderId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 리더 변경 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 리더 변경 요청 응답 수신 성공")
                d(TAG, "setGroupLeader : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
//                        quitGroup(service, groupId, getUserId(applicationContext))
//                        getGroups(service, getUserId(applicationContext))
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // '그룹 정보' 또는 '그룹 수정하기' 액티비티에 그룹 데이터 세팅하기
    fun getGroup(service: RetrofitService, groupId : Int) {
        val userId = getUserId(this)
        Log.d(TAG, "getGroup 변수들 : $groupId, $userId")
        service.getGroup(groupId, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "getGroup : " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                when (TAG) {
                    "GroupInfoActivity" -> {
                        mainTitleTxt.text = gson.get("title").asString // 텍스트뷰인 경우

                        // 그룹 인원 표시하기
                        headCount = gson.get("memberCount").asInt // 현재 가입되어 있는 그룹 멤버 수
                        headLimitValue = gson.get("headLimit").asInt // 인원 제한 수
                        if (headLimitValue > 1) {
                            headLimitTxt.text = "$headCount / $headLimitValue"
                        } else if (headLimitValue == 0) {
                            headLimitTxt.text = "$headCount"
                        }

                        onPublic.isChecked = gson.get("onPublic").asBoolean // 그룹 공개 여부
                        tags.setText(gson.get("tags").asString) // 그룹 태그
                        memo.setText(gson.get("memo").asString) // 그룹 추가 설명

                    }
                    "GroupApplicantListActivity" -> {
                        headCount = gson.get("memberCount").asInt // 현재 가입되어 있는 그룹 멤버 수
                        headLimitValue = gson.get("headLimit").asInt // 인원 제한 수

                    }
                    else -> {
                        mainTitleInput.setText(gson.get("title").asString) // 에딧텍스트인 경우

                        if(gson.get("headLimit").asInt == 0) {
                            isLimited.isChecked = false
                            headLimit.setText(getString(R.string.zero))
                        } else {
                            isLimited.isChecked = true
                            headLimit.setText(gson.get("headLimit").asString)
                        }

                        onPublic.isChecked = gson.get("onPublic").asBoolean // 그룹 공개 여부
                        tags.setText(gson.get("tags").asString) // 그룹 태그
                        memo.setText(gson.get("memo").asString) // 그룹 추가 설명
                    }
                }
//                onPublic.isChecked = gson.get("onPublic").asBoolean // 그룹 공개 여부
                onPublicResult = gson.get("onPublic").asBoolean
//                setFullBtmBtnText(btmBtn) // 그룹 공개 여부에 따라서 하단 버튼의 텍스트 변경

//                tags.setText(gson.get("tags").asString) // 그룹 태그
//                memo.setText(gson.get("memo").asString) // 그룹 추가 설명

                leaderId = gson.get("leaderId").asInt // 그룹 생성자
                joined = gson.get("joined").asBoolean // 사용자의 그룹 가입 여부
                applied = gson.get("applied").asBoolean // 사용자의 그룹 가입 신청 여부
                setFullBtmBtnText(btmBtn) // 현재 사용자와 그룹 생성자가 동일한 경우 또는 그룹 가입 여부에 따라 하단 버튼의 텍스트를 세팅함

                if(headCount == headLimitValue && !joined) btmBtn.isEnabled = false // 그룹 인원이 다 찼고, 사용자가 그룹에 가입한 상태가 아니라면 버튼 비활성화(가입 및 가입 신청 방지)
            }
        })
    }

    // 그룹 해체하기
    fun deleteGroup(service: RetrofitService, groupId : Int, context : Context) {
        Log.d(TAG, "deleteGroup 변수들 : $groupId")
        service.deleteGroup(groupId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 해체 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 해체 요청 응답 수신 성공")
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

    // 그룹 가입하기
    fun joinGroup(service: RetrofitService, groupId : Int, userId : Int, joined : Boolean) {
        d(TAG, "joinGroup 변수들 : $groupId, $userId, $joined")
        service.joinGroup(groupId, userId, joined).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 가입 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 가입 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        finish()
                        startActivity<GroupListActivity>()
                        getGroups(service, getUserId(applicationContext))
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 그룹 가입 허용하기
    fun acceptJoinGroup(service: RetrofitService, groupId : Int, userId : Int) {
        d(TAG, "joinGroup 변수들 : $groupId, $userId")
        service.acceptJoinGroup(groupId, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 가입 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 가입 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
//                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        finish()
                        startActivity<GroupListActivity>()
                        getGroups(service, getUserId(applicationContext))
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 그룹 탈퇴하기
    fun quitGroup(service: RetrofitService, groupId : Int, userId : Int) {
        d(TAG, "quitGroup 변수들 : $groupId, $userId")
        service.quitGroup(groupId, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 탈퇴 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 탈퇴 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        getGroups(service, getUserId(applicationContext))
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}