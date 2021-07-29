package com.seahahn.routinemaker.util

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.sns.chat.FullImgAdapter
import com.seahahn.routinemaker.sns.group.*
import com.seahahn.routinemaker.sns.newsfeed.FeedCmtViewModel
import com.seahahn.routinemaker.sns.newsfeed.FeedImgAdapter
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedMakeActivity
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedViewModel
import com.seahahn.routinemaker.sns.others.OtherMypageActivity
import com.seahahn.routinemaker.util.AppVar.getAcceptedList
import com.seahahn.routinemaker.util.AppVar.getNextLeaderId
import com.seahahn.routinemaker.util.AppVar.setOtherUserId
import com.seahahn.routinemaker.util.AppVar.setOtherUserNick
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserNick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import java.util.function.Predicate


open class Sns : Main() {

    private val TAG = this::class.java.simpleName

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_BRING = 2

    val groupListViewModel by viewModels<GroupListViewModel>() // 그룹 목록 데이터
    val groupMemberViewModel by viewModels<GroupMemberViewModel>() // 그룹 멤버 목록 데이터
    val feedListViewModel by viewModels<GroupFeedViewModel>() // 그룹 피드 목록 데이터
    val cmtListViewModel by viewModels<FeedCmtViewModel>() // 그룹 피드 댓글 목록 데이터

    // 그룹 만들기, 정보 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    open var groupId : Int = 0 // DB내 그룹 고유 번호
    open var leaderId : Int = 0 // DB내 그룹 생성자 고유 번호
    var feedId : Int = 0 // 피드 고유 번호
    var feedWriterId : Int = 0 // 피드 작성자 고유 번호
    var challengeId : Int = 0 // DB내 그룹 챌린지 고유 번호

    lateinit var mainTitleTxt : TextView // 그룹명(텍스트뷰)
    open var groupTitle = ""// 그룹명
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

    lateinit var profile_pic : ImageView
    lateinit var nick : TextView
    lateinit var content : EditText
    lateinit var contentTxt : TextView
    lateinit var contentResult : Editable
    lateinit var mViewPager : ViewPager2
    lateinit var addImg : ImageView
    var imgDatas = mutableListOf<Any>() // 그룹 피드에 첨부한 이미지 URL들을 담은 리스트
    var imagesList = mutableListOf<String>()
    var imgDatasCmt = mutableListOf<Any>() // 댓글에 첨부할 이미지 URL을 담은 리스트
    var imgDatasChat = mutableListOf<Any>() // 채팅 메시지에 첨부할 이미지 URL을 담은 리스트
    lateinit var imagesURL : String
    lateinit var createdAt : TextView

    lateinit var feedImgAdapter : FeedImgAdapter
    lateinit var fullImgAdapter : FullImgAdapter

    lateinit var fullBtmChat : ConstraintLayout
    lateinit var chatInput : EditText
    lateinit var photoInput : ImageButton
    lateinit var chatSend : ImageButton

    lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    lateinit var fullImgLayout : ConstraintLayout // 이미지 클릭 시 크게 보여줄 레이아웃
    lateinit var fullImgPager : ViewPager2 // 이미지 클릭 시 크게 보여줄 뷰페이저
    lateinit var fullImgClose : ImageButton // 이미지 클릭 시 크게 보여줄 화면 닫기 버튼

    lateinit var viewEmptyList : LinearLayout

    // 그룹 만들기, 수정 및 그룹 정보 액티비티 내의 공통 요소들 초기화하기
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

    // 그룹 피드 만들기, 수정하기 액티비티 내의 공통 요소들 초기화하기
    fun initFeedActivity(btmBtnId : Int) {
        profile_pic = findViewById(R.id.profile_pic)
        nick  = findViewById(R.id.nick)
        content  = findViewById(R.id.content)
        mViewPager  = findViewById(R.id.mViewPager)
        addImg  = findViewById(R.id.addImg)
        addImg.setOnClickListener(BtnClickListener())

        // 하단 버튼 초기화
        btmBtn = findViewById(btmBtnId)
        setFullBtmBtnText(btmBtn)
        btmBtn.setOnClickListener(BtnClickListener())

        // 이미지 업로드 시 출력할 프로그레스바 초기화
        prograssbar = findViewById(R.id.prograssbar)
        showProgress(false)
    }

    // 채팅 메시지 또는 피드의 댓글을 입력하기 위한 하단의 입력란 요소들 초기화하기
    fun initChatInput(ph : Int) {
        fullBtmChat = findViewById(R.id.full_btm_chat)
        chatInput = findViewById(R.id.chatInput)
        photoInput = findViewById(R.id.photoInput)
        chatSend = findViewById(R.id.chatSend)

        chatInput.setHint(ph)
    }

    // 채팅방 또는 피드 자세히 보기에서 이미지 클릭 시 전체화면으로 출력하기 위한 요소들 초기화하기
    fun initFullImgLayout() {
        fullImgLayout = findViewById(R.id.fullImgLayout)
//        fullImgLayout = findViewById(R.id.fullImgLayout)
        fullImgPager = findViewById(R.id.fullImgPager)
        fullImgClose = findViewById(R.id.fullImgClose)
    }

    // 하단 입력창에 포커스 가면 키보드 올리기
    fun showSoftKeyboard() {
        chatInput.requestFocus()

        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(chatInput, 0)
    }

    // 하단 입력창에 포커스 풀리면 키보드 내리기
    fun hideSoftKeyboard() {
        chatInput.clearFocus()

        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(chatInput.windowToken, 0)
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
            R.id.makeFeed -> btn.text = getString(R.string.makeFeed)
            R.id.updateFeed -> btn.text = getString(R.string.updateFeed)
        }
    }

    // 그룹 만들기 또는 수정하기 액티비티에서 필수 입력 사항들을 입력하지 않은 경우 입력하도록 안내하기
    fun inputCheckGroup(): Boolean {
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

    // 그룹 피드 작성 또는 수정하기 액티비티에서 필수 입력 사항들을 입력하지 않은 경우 입력하도록 안내하기
    fun inputCheckFeed(): Boolean {
        if(contentResult.isBlank()) {
            // 피드 내용을 입력하지 않은 경우 입력하도록 안내하기
            toast(getString(R.string.feedContentEmpty))
            return false
        }
        return true
    }

    // 툴바 우측 버튼 눌렀을 때의 동작 구현
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected Sns")
        d(TAG, "context : $applicationContext")
        when(item.itemId){
            R.id.toolbarSnsTrash -> {
                when(TAG) {
                    "GroupUpdateActivity" -> {
                        d(TAG, "GroupUpdateActivity")
                        showAlert("그룹 해체하기", "정말 해체하시겠어요?")
                    }
                    "GroupFeedUpdateActivity" -> {
                        d(TAG, "GroupFeedUpdateActivity")
                        showAlert("피드 삭제하기", "정말 삭제하시겠어요?")
                    }
                }
            }
            R.id.toolbarSnsUpdate -> {
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
                when (TAG) {
                    "GroupInfoActivity" -> {
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
                        }
                    }
                    "GroupUpdateActivity" -> {
                        deleteGroup(service, groupId, this)
                        finish()
                    }
                    "GroupFeedUpdateActivity" -> {
                        deleteFeed(service, feedId, this)
                        finish()
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
        val groupMemberListAdapter = GroupNextLeaderAdapter() // 어댑터 초기화
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
//                    headLimitResult = headLimit.text
                }
            }
            R.id.onPublic -> onPublicResult = isChecked
        }
    }

    // 액티비티 내 버튼 눌렀을 때의 동작 구현
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                // 우측 하단에 위치한 FAB인 경우


                // 하단에 가로로 꽉 차는 버튼인 경우
                R.id.makeGroup -> {
                    headLimitResult = headLimit.text
                    d(TAG, "그룹 만들기 변수들 : $mainTitle, $tagstxt, $headLimitResult, $onPublicResult, $memotxt")
                    if(inputCheckGroup()) {
                        makeGroup(service, mainTitle.toString(),
                            tagstxt.toString(),
                            headLimitResult.toString().toInt(),
                            onPublicResult,
                            memotxt.toString(),
                            getUserId(applicationContext))
                    }
                }
                R.id.updateGroup -> {
                    mainTitle = mainTitleInput.text!!
                    tagstxt = tags.text
                    headLimitResult = headLimit.text
                    memotxt = memo.text
                    d(TAG, "그룹 수정 변수들 : $groupId, $mainTitle, $tagstxt, $headLimitResult, $onPublicResult, $memotxt")
                    if(inputCheckGroup()) updateGroup(service, groupId, mainTitle.toString(), tagstxt.toString(), headLimitResult.toString().toInt(),
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
                R.id.makeFeed -> {
                    contentResult = content.text
                    if(inputCheckFeed()) {
                        saveImgsURL(imgDatas, imagesList)
                        makeFeed(service, contentResult.toString(), imagesURL, groupId, challengeId)
                    }
                }
                R.id.updateFeed -> {
                    contentResult = content.text
                    if(inputCheckFeed()) {
                        imgDatas = feedImgAdapter.returnList()
                        saveImgsURL(imgDatas, imagesList)
                        updateFeed(service, feedId, contentResult.toString(), imagesURL)
                    }
                }
            }
        }
    }

    // 다른 사용자의 프로필 사진 또는 닉네임을 클릭 시 동작할 내용
    class ProfileClickListener : View.OnClickListener {
        private val TAG = this::class.java.simpleName
        override fun onClick(v: View?) {
            val context = v!!.context
            val otherUserId = ((v.tag as HashMap<*, *>)["id"]).toString().toInt()
            val otherUserNick = ((v.tag as HashMap<*, *>)["nick"]).toString()
            d(TAG, "otherUserInfo : $otherUserId, $otherUserNick")
            if(otherUserId != getUserId(context)) {
                setOtherUserId(context, otherUserId)
                setOtherUserNick(context, otherUserNick)
                val it = Intent(v.context, OtherMypageActivity::class.java)
//                it.putExtra("id", otherUserId)
                context.startActivity(it)
            }
        }
    }

    // FAB 버튼 초기화하기
    fun initFABinSNS() {
        when(TAG) {
            "GroupFeedActivity" -> {
                fabtn = findViewById(R.id.fabtn)
                fabMain = findViewById(R.id.fabMain)
                fabMain.setOnClickListener {
                    startActivity<GroupFeedMakeActivity>("groupId" to groupId, "challengeId" to challengeId) // '피드 작성' 액티비티로 이동
                }
            }
        }
    }

    // 피드 이미지 포지션 저장해두기
    inner class MyOnPageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if(position != 0) AppVar.setPagerPos(applicationContext, position)
        }
    }

    // 피드 작성 또는 수정에서 이미지 추가 버튼 클릭 시 팝업 메뉴 출력
    inner class ImgAddClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            photo(v!!)
        }
    }

    // 이미지 추가 버튼 눌렀을 때의 팝업 메뉴
    inner class ImgMenuItemClickListener(v: View) : PopupMenu.OnMenuItemClickListener {
        private val max = v.tag.toString().toInt()
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.takePhoto -> {
                    capturePhoto()
                    true
                }
                R.id.bringPhoto -> {
                    bringPhoto()
                    true
                }
                else -> false
            }
        }
    }

    // 사진 찍기 혹은 가져오기의 결과물 가져오기
    open val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result : $result")
            if (result.resultCode == RESULT_OK) {
                imgDatas = feedImgAdapter.returnList()
                val data = result.data
                if (data?.clipData != null) { // 사진 여러개 선택한 경우
                    val count = data.clipData!!.itemCount
                    if (count > 5) {
                        toast(getString(R.string.maxFivePicsWarning))
                    } else if(count + imgDatas.size > 5) {
                        toast(getString(R.string.maxFivePicsWarning))

                    } else {
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            imgDatas.add(imageUri)
                        }
                        feedImgAdapter.replaceList(imgDatas)
                    }

                } else {
                    val thumbnail: Bitmap? = data?.getParcelableExtra("data") // 찍은 사진 이미지 썸네일 비트맵 가져오기

                    imgDatas.add(thumbnail!!)
                    feedImgAdapter.replaceList(imgDatas)
                }
                mViewPager.setCurrentItem(imgDatas.size, false)
            }
        }

    // 사진 아이콘 눌렀을 때 팝업 메뉴를 띄워줌
    fun photo(v: View) {
        PopupMenu(v.context, v).apply {
            // MainActivity implements OnMenuItemClickListener
            setOnMenuItemClickListener(ImgMenuItemClickListener(v))
            inflate(R.menu.menu_photo)
            show()
        }
    }

    // 사진 촬영하기
    open fun capturePhoto() {
        Log.d(TAG, "capturePhoto")
        if(imgDatas.size > 5) {
            toast(getString(R.string.maxFivePicsWarning))
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            getContent.launch(intent)
        }
    }

    // 사진 가져오기(피드 최대 5장, 댓글 최대 1장)
    open fun bringPhoto() {
        Log.d(TAG, "bringPhoto")
        if(imgDatas.size > 5) {
            toast(getString(R.string.maxFivePicsWarning))
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            val chooser = Intent.createChooser(intent, getString(R.string.maxFivePics))
            getContent.launch(chooser)
            toast(getString(R.string.maxFivePics))
        }
    }

    // 사진 경로 저장하기
    open fun saveImgsURL(imgDatas : MutableList<Any>, imagesList : MutableList<String>) {
        d(TAG, "imgDatas : $imgDatas")
        d(TAG, "imagesList : $imagesList")
        var imgUri : Any?
        var thumbnail : Bitmap

        var img : String
        var imgFile : File
        var imgPath : String
        val s3url = getString(R.string.s3_bucket_route) // s3 루트 경로(위 imgPath의 앞부분)

        for(i in 0 until imgDatas.size) {
            when {
                imgDatas[i] is Uri -> { // 갤러리에서 사진 가져온 경우
                    imgUri = imgDatas[i]
                    thumbnail = MediaStore.Images.Media.getBitmap(contentResolver, imgUri as Uri)

                    img = saveBitmapToJpg(thumbnail, System.currentTimeMillis().toString(), 100) // 사진 비트맵이 저장된 파일 경로 가져오기
                    imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
                    imgPath = "photo/" + System.currentTimeMillis().toString() + ".jpg" // S3에 저장될 경로 설정

                    imagesList.add("$s3url$imgPath") // 이미지 경로 추가

                    // s3에 저장하기
                    runOnUiThread {
                        showProgress(true)
                        uploadFileToAWS(imgPath, imgFile)
                    }
                }
                imgDatas[i] is Bitmap -> { // 카메라로 사진 찍은 경우
                    img = saveBitmapToJpg(imgDatas[i] as Bitmap, System.currentTimeMillis().toString(), 100) // 사진 비트맵이 저장된 파일 경로 가져오기
                    imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
                    imgPath = "photo/" + System.currentTimeMillis().toString() + ".jpg" // S3에 저장될 경로 설정

                    imagesList.add("$s3url$imgPath") // 이미지 경로 추가

                    // s3에 저장하기
                    runOnUiThread {
                        showProgress(true)
                        uploadFileToAWS(imgPath, imgFile)
                    }
                }
                else -> { // 원래 있던 것 그대로 둔 경우
                    imagesList.add(imgDatas[i].toString())
                }
            }
        }
        if(imagesList.isNotEmpty()) {
            imagesURL = imagesList.toString() // 이미지 경로를 모아둔 리스트를 문자열로 바꾸어 저장
        } else {
            imagesURL = ""
        }
        imgDatas.clear()
        imagesList.clear()
//        val acceptedList = Arrays.stream(acceptedListString.substring(1, acceptedListString.length - 1).split(",").toTypedArray())
//            .map { obj: String -> obj.trim { it <= ' ' } }.mapToInt(Integer::parseInt).toArray()
    }

    open fun uploadFileToAWS(imgPath : String, imgFile : File) {
        // s3에 저장하기
        Amplify.Storage.uploadFile(
            imgPath, // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
            imgFile, // 실제 저장될 파일
            { result ->
                Log.d(TAG, "Successfully uploaded : $result")
                showProgress(false)
                //                changeInfo(service, "photo", "$s3url$imgPath") // DB 내 사용자의 프로필 사진 경로 정보 변경하기
            },
            { error -> Log.d(TAG, "Upload failed", error) }
        )
    }

    override fun showProgress(show : Boolean) {
        if(show) {
            prograssbar.show()
        } else {
            prograssbar.hide()
            if(TAG == "GroupFeedDetailActivity") getCmts(service, feedId)
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

    // 그룹 멤버 목록 불러오기
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

    // 그룹 리더 변경하기
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
                    "GroupUpdateActivity" -> {
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
//                groupTitle = gson.get("title").asString

//                onPublic.isChecked = gson.get("onPublic").asBoolean // 그룹 공개 여부
                onPublicResult = gson.get("onPublic").asBoolean
//                setFullBtmBtnText(btmBtn) // 그룹 공개 여부에 따라서 하단 버튼의 텍스트 변경

//                tags.setText(gson.get("tags").asString) // 그룹 태그
//                memo.setText(gson.get("memo").asString) // 그룹 추가 설명

                leaderId = gson.get("leaderId").asInt // 그룹 생성자
                joined = gson.get("joined").asBoolean // 사용자의 그룹 가입 여부
                applied = gson.get("applied").asBoolean // 사용자의 그룹 가입 신청 여부
                if(TAG != "GroupFeedActivity") {
                    setFullBtmBtnText(btmBtn) // 현재 사용자와 그룹 생성자가 동일한 경우 또는 그룹 가입 여부에 따라 하단 버튼의 텍스트를 세팅함
                    if(headCount == headLimitValue && !joined) btmBtn.isEnabled = false // 그룹 인원이 다 찼고, 사용자가 그룹에 가입한 상태가 아니라면 버튼 비활성화(가입 및 가입 신청 방지)
                }
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

    // 그룹 피드 목록 불러오기
    fun getFeeds(service: RetrofitService, groupId : Int, userId : Int) {
        d(TAG, "getFeeds 변수들 : $groupId, $userId")
        service.getFeeds(groupId, userId).enqueue(object : Callback<MutableList<FeedData>> {
            override fun onFailure(call: Call<MutableList<FeedData>>, t: Throwable) {
                Log.d(TAG, "피드 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<FeedData>>, response: Response<MutableList<FeedData>>) {
                Log.d(TAG, "피드 목록 가져오기 요청 응답 수신 성공")
//                d(TAG, "getRts : "+response.body().toString())
                val feedDatas = response.body()
                try {
                    feedListViewModel.setList(feedDatas!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

    // 피드 작성 시 사용자의 프로필 사진, 닉네임 표시하기
    fun getUserData(service: RetrofitService, writerId : Int) {
        d(TAG, "getUserData 변수들 : $writerId")
        service.getUserData(writerId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val nick = gson.get("nick").asString
                val photo = gson.get("photo").asString
                setUserNickAndPhoto(nick, photo)
            }
        })
    }

    // 사용자의 프로필 사진과 닉네임 표시하기
    fun setUserNickAndPhoto(nickname : String, photoURL : String) {
        nick.text = nickname
        Glide.with(applicationContext).load(photoURL)
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(profile_pic)

        // 프로필 사진 및 닉네임에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
        profile_pic.tag = hashMapOf("id" to feedWriterId, "nick" to nickname)
        nick.tag = hashMapOf("id" to feedWriterId, "nick" to nickname)
    }

    // 그룹 피드 작성하기
    fun makeFeed(service : RetrofitService, content : String, images : String, groupId : Int, challengeId : Int) {
        d(TAG, "makeFeed 변수들 : $content, $images")
        val writerId = getUserId(applicationContext)
        service.makeFeed(writerId, content, images, groupId, challengeId).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 작성 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 작성 요청 응답 수신 성공")
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

    // 그룹 피드 수정하기
    fun updateFeed(service : RetrofitService, feedId : Int, content : String, images : String) {
        d(TAG, "updateFeed 변수들 : $feedId, $content, $images")
        service.updateFeed(feedId, content, images).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 수정 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 수정 요청 응답 수신 성공")
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

    // 그룹 피드 데이터 가져오기
    fun getFeed(service: RetrofitService, feedId : Int, userId : Int) {
        Log.d(TAG, "getFeed 변수들 : $feedId, $userId")
        service.getFeed(feedId, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "getFeed : " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                content.setText(gson.get("content").asString)
                imagesURL = gson.get("images").asString

                if(imagesURL.isNotBlank()) {
                    val imgArray = Arrays.stream(imagesURL.substring(1, imagesURL.length - 1).split(",").toTypedArray())
                        .map { obj: String -> obj.trim { it <= ' ' } }.toArray() // 문자열을 먼저 배열로 변환
//                    val imgList = mutableListOf<Any>()
                    for(element in imgArray){
                        imgDatas.add(element.toString()) // 배열을 다시 리스트로 만듦
                    }
                    feedImgAdapter.replaceList(imgDatas) // 만든 리스트를 목록에 넣음
                }
            }
        })
    }

    // 그룹 피드 삭제하기
    fun deleteFeed(service: RetrofitService, feedId : Int, context : Context) {
        Log.d(TAG, "deleteFeed 변수들 : $feedId")
        service.deleteFeed(feedId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 삭제 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        getFeeds(service, groupId, getUserId(applicationContext))
                    }
                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 그룹 피드 좋아요 기능
    fun setFeedLike(service: RetrofitService, feedId : Int, feedWriterId : Int, writerId : Int, isLiked : Boolean, context : Context) {
        Log.d(TAG, "setFeedLike 변수들 : $feedId, $feedWriterId, $writerId, $isLiked")
        service.setFeedLike(feedId, feedWriterId, writerId, isLiked).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 좋아요 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 좋아요 요청 응답 수신 성공")
                if(isLiked) {
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    d(TAG, "gson : $gson")
                    val alreadyExist = gson.get("alreadyExist").asBoolean // 이전에 좋아요 눌렀는지 아닌지 여부
                    // 이전에 누른 적 있으면 알림 안 뜨게 함
                    when {
                        alreadyExist -> return
                        else -> {
                            val token = gson.get("token").asString // 알림 보낼 대상의 파이어베이스 토큰값
//                            val content = gson.get("content").asString
//                            val images = gson.get("images").asString

                            fcmService = initFCMRetrofit() // FCM 보내기 위한 레트로핏 객체 초기화
                            val title = getUserNick(context)
                                .plus(
                                    context.getString(
                                        R.string.notiLike)) // 알림 제목 R.string.notiLike
                            val body = response.body().toString() // 피드의 내용(텍스트, 이미지)
                            val contents = NotificationContents(
                                token,
                                NotificationData(
                                    getUserId(context),
                                    FCMNotiType.LIKE.type(),
                                    title,
                                    body,
                                    feedId
                                )
                            )
                            sendFCMNotification(fcmService, contents) // FCM 발송
                        }
                    }
                }
            }
        })
    }

    // 그룹 피드의 댓글 목록 불러오기
    fun getCmts(service: RetrofitService, feedId : Int) {
        d(TAG, "getCmts 변수들 : $feedId")
        service.getCmts(feedId).enqueue(object : Callback<MutableList<CmtData>> {
            override fun onFailure(call: Call<MutableList<CmtData>>, t: Throwable) {
                Log.d(TAG, "피드 댓글 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<CmtData>>, response: Response<MutableList<CmtData>>) {
                Log.d(TAG, "피드 댓글 목록 가져오기 요청 응답 수신 성공")
                d(TAG, "getCmts : "+response.body().toString())
                val cmtDatas = response.body()
                try {
                    cmtListViewModel.setList(cmtDatas!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

    // 그룹 피드 댓글 작성하기
    fun makeCmt(service: RetrofitService, feedId: Int, feedWriterId : Int, content: String, image: String, isSub: Boolean, mainCmt: Int?) {
        d(TAG, "makeCmt 변수들 : $feedId, $feedWriterId, $content, $image, $isSub, $mainCmt")
        val writerId = getUserId(applicationContext)
        service.makeCmt(writerId, feedId, feedWriterId, content, image, isSub, mainCmt).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 댓글 작성 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 댓글 작성 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)

                fcmService = initFCMRetrofit() // FCM 보내기 위한 레트로핏 객체 초기화
                var title : String // 알림 제목
                val body = response.body().toString() // 피드의 내용(텍스트, 이미지)

                var type : Int
                var titleTextValue : Int

                // 댓글인지 대댓글인지 구분에 따라 알림에 넣을 타입 값을 다르게 함
                val subCmt = gson.get("subCmt").asBoolean
                if(subCmt) {
                    // 대댓글의 대상이 되는 원 댓글의 작성자에게 알림을 보냄
                    titleTextValue = R.string.notiSubCmt
                    type = FCMNotiType.SUB_CMT.type()

                    title = getUserNick(applicationContext).plus(getString(titleTextValue)) // 알림 제목
                    val mainCmtWriterToken = gson.get("mainCmtWriterToken").asString // 알림 보낼 대상의 파이어베이스 토큰값
                    val contents = NotificationContents(
                        mainCmtWriterToken,
                        NotificationData(
                            getUserId(applicationContext),
                            type,
                            title,
                            body,
                            feedId
                        )
                    )
                    sendFCMNotification(fcmService, contents) // FCM 발송
                }

                // 댓글, 대댓글 상관 없이 해당 피드의 작성자에게 알림을 보냄
                titleTextValue = R.string.notiCmt
                type = FCMNotiType.CMT.type()
                title = getUserNick(applicationContext).plus(getString(titleTextValue)) // 알림 제목

                val token = gson.get("token").asString // 알림 보낼 대상의 파이어베이스 토큰값
//                fcmService = initFCMRetrofit() // FCM 보내기 위한 레트로핏 객체 초기화
//                val title = getUserNick(applicationContext).plus(getString(titleTextValue)) // 알림 제목
//                val body = response.body().toString() // 피드의 내용(텍스트, 이미지)
                val contents = NotificationContents(
                    token,
                    NotificationData(
                        getUserId(applicationContext),
                        type,
                        title,
                        body,
                        feedId
                    )
                )
                sendFCMNotification(fcmService, contents) // FCM 발송
            }
        })
    }

    // 그룹 피드 수정하기
    fun updateCmt(service : RetrofitService, cmtId : Int, content : String) {
        d(TAG, "updateCmt 변수들 : $cmtId, $content")
        service.updateCmt(cmtId, content).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 댓글 수정 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 댓글 수정 요청 응답 수신 성공")
                getCmts(service, feedId)
//                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
//                Log.d(TAG, response.body().toString())
//                val msg = gson.get("msg").asString
//                val result = gson.get("result").asBoolean
//                when(result) {
//                    true -> {
//                        toast(msg)
//                        finish()
//                    }
//                    false -> toast(msg)
//                }
            }
        })
    }

    // 그룹 피드 댓글 삭제하기
    fun deleteCmt(service: RetrofitService, cmtId : Int) {
        Log.d(TAG, "deleteCmt 변수들 : $cmtId")
        service.deleteCmt(cmtId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 댓글 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 댓글 삭제 요청 응답 수신 성공")
                getCmts(service, feedId)
//                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
//                val msg = gson.get("msg").asString
//                val result = gson.get("result").asBoolean
//                when(result) {
//                    true -> {
//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                        getFeeds(service, groupId, getUserId(applicationContext))
//                    }
//                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                }
            }
        })
    }
}