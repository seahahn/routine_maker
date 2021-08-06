package com.seahahn.routinemaker.sns.newsfeed

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.util.AppVar.getPagerPos
import com.seahahn.routinemaker.util.KeyboardVisibilityUtils
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/* 그룹 피드 누르면 들어가는 액티비티
* 해당 피드에 대한 내용만 있고, 여기서만 댓글 확인 및 작성이 가능
* 좋아요는 피드 목록과 동일하게 가능
 */
class GroupFeedDetailActivity : Sns() {

    private val TAG = this::class.java.simpleName
    private val context = this@GroupFeedDetailActivity

    private lateinit var scrollView: NestedScrollView

//    private var imgDatasCmt = mutableListOf<Any>()
    
    lateinit var moreBtn : ImageButton

    lateinit var likeIcon : ImageView
    lateinit var likeTxt : TextView
    lateinit var commentIcon : ImageView
    lateinit var commentTxt : TextView

    private var likeState : Boolean = false
    private var commentState : Boolean = false

    private lateinit var cmtListAdapter: FeedCmtAdapter
    private lateinit var cmtList: RecyclerView
    var mDatas = mutableListOf<CmtData>()
//    var showDatas = mutableListOf<CmtData>()
//    lateinit var it_mDatas : Iterator<CmtData>

    lateinit var previewImgArea : ConstraintLayout
    lateinit var preview : ImageView
    lateinit var imgDelete : ImageButton

    var isSubCmt = false
    var mainCmt = 0
    lateinit var subCmtCtrl : LinearLayout
    lateinit var subCmtInfo : TextView
    lateinit var subCmtCancel : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_feed_detail)

        d(TAG, "imgDatas : $imgDatas")
        d(TAG, "imagesList : $imagesList")

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.detailFeedTitle) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        initActivity() // 액티비티 구성요소 초기화
        initChatInput(R.string.cmtPh) // 하단 댓글 입력창 초기화
        initFullImgLayout() // 이미지 전체 보기 레이아웃 초기화
        photoInput.tag = 1 // 최대 업로드 가능한 사진 개수
        photoInput.setOnClickListener(ImgAddClickListener())
        chatSend.setOnClickListener(BtnClickListener())
        imgDelete.setOnClickListener(BtnClickListener())
        fullImgClose.setOnClickListener(BtnClickListener())

        // 추가된 이미지 보여줄 뷰페이저 초기화
        feedImgAdapter = FeedImgAdapter() // 어댑터 초기화
        mViewPager.adapter = feedImgAdapter // 어댑터 연결
        fullImgPager.adapter = feedImgAdapter // 어댑터 연결(이미지 전체화면)
        feedImgAdapter.isChangableActivity(false)
        feedImgAdapter.isFullScreen(false)
        mViewPager.registerOnPageChangeCallback(MyOnPageChangeCallback())

        cmtList = findViewById(R.id.cmtList) // 리사이클러뷰 초기화
        cmtListAdapter = FeedCmtAdapter() // 어댑터 초기화
        cmtList.adapter = cmtListAdapter // 어댑터 연결
        cmtListAdapter.getService(service)

        feedId = intent.getIntExtra("feedId", 0) // DB 내 그룹 피드의 고유 번호 받기
        getFeedDetail(service, feedId, getUserId(this))
        getCmts(service, feedId)

        cmtListViewModel.gottenCmtData.observe(this) { cmtDatas ->
            mDatas = cmtDatas // 뷰모델에 저장해둔 댓글 목록 데이터 가져오기
            cmtListAdapter.replaceList(mDatas) // 그룹 고유 번호에 맞춰서 피드 목록 띄우기
        }

        // 댓글에 답글 달기 취소 버튼 누를 때 동작
        subCmtCancel.setOnClickListener(BtnClickListener())

        // 키보드 나오면 그 높이에 맞춰서 레이아웃의 스크롤을 움직임
        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
            onShowKeyboard = { keyboardHeight ->
                scrollView.run {
                    smoothScrollTo(scrollX, scrollY + keyboardHeight)
                }
            })

        // 피드 작성자의 프로필 사진 또는 닉네임 클릭 시 해당 사용자의 프로필 방문
        profile_pic.setOnClickListener(ProfileClickListener())
        nick.setOnClickListener(ProfileClickListener())
    }

    // 액티비티 구성요소 초기화
    private fun initActivity() {
        scrollView = findViewById(R.id.scroll_view)

        profile_pic = findViewById(R.id.profile_pic)
        nick  = findViewById(R.id.nick)
        createdAt = findViewById(R.id.createdAt)

        moreBtn = findViewById(R.id.more_btn)
        mViewPager = findViewById(R.id.mViewPager)
        contentTxt = findViewById(R.id.content)

        likeIcon = findViewById(R.id.likeIcon)
        likeTxt = findViewById(R.id.likeTxt)
        commentIcon = findViewById(R.id.commentIcon)
        commentTxt = findViewById(R.id.commentTxt)

        previewImgArea = findViewById(R.id.previewImgArea)
        preview = findViewById(R.id.preview)
        imgDelete = findViewById(R.id.imgDelete)

        subCmtCtrl = findViewById(R.id.subCmtCtrl)
        subCmtInfo = findViewById(R.id.subCmtInfo)
        subCmtCancel = findViewById(R.id.subCmtCancel)

        prograssbar = findViewById(R.id.prograssbar)
        showProgress(false)
    }

    // 선택된 피드에 해당하는 데이터들 가져오기
    fun getFeedDetail(service: RetrofitService, feedIdIn : Int, userId : Int) {
        Log.d(TAG, "getFeedDetail 변수들 : $feedIdIn, $userId")
        service.getFeed(feedIdIn, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "그룹 피드 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "그룹 피드 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "getFeed : " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                feedId = gson.get("id").asInt
                feedWriterId = gson.get("writerId").asInt
                getUserData(service, feedWriterId)
                createdAt.text = gson.get("createdAt").asString
                contentTxt.text = gson.get("content").asString
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

                // 좋아요 수와 사용자의 좋아요 여부 및 댓글 수 불러오기
                val likeCount = gson.get("likeCount").asInt
                val commentCount = gson.get("commentCount").asInt
                val liked = gson.get("liked").asBoolean
                val cmt = gson.get("cmt").asBoolean
                if(liked) { likeIcon.setImageResource(R.drawable.like_black) } else { likeIcon.setImageResource(R.drawable.like_white) }
                likeState = liked
                if(cmt) { commentIcon.setImageResource(R.drawable.comment_black) } else { commentIcon.setImageResource(R.drawable.comment_white) }
                commentState = cmt

                likeIcon.setOnClickListener(BtnClickListener())
                commentIcon.setOnClickListener(BtnClickListener())
                likeTxt.text = likeCount.toString()
                commentTxt.text = commentCount.toString()

                moreBtn.tag = hashMapOf("id" to feedId, "writerId" to feedWriterId)
                moreBtn.setOnClickListener(BtnClickListener())
            }
        })
    }

    // 피드의 좋아요, 댓글, 더보기와 하단의 카메라, 댓글 달기 아이콘 클릭 시 작동할 기능
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v!!.id) {
                R.id.likeIcon -> {
                    setFeedLike(service, feedId, feedWriterId, getUserId(applicationContext), !likeState, applicationContext) // 좋아요 누른 결과 서버에 보내기
                    likeState = !likeState // 기존의 좋아요 상태를 반대로 바꿔줌
                    // 바뀐 좋아요 상태에 따라 좋아요 아이콘의 색상 변경
                    if(likeState) {
                        likeIcon.setImageResource(R.drawable.like_black)
                        likeTxt.text = (likeTxt.text.toString().toInt()+1).toString()
                    } else {
                        likeIcon.setImageResource(R.drawable.like_white)
                        likeTxt.text = (likeTxt.text.toString().toInt()-1).toString()
                    }
                }
                R.id.commentIcon -> showSoftKeyboard()
                R.id.more_btn -> {
                    val popup = PopupMenu(v.context, v)
                    popup.apply {
                        // MainActivity implements OnMenuItemClickListener
                        setOnMenuItemClickListener(GroupFeedPopupMenuListener(v))
                        inflate(R.menu.menu_feed_more)
                        // 사용자가 피드 작성자이면 피드 수정 및 삭제 가능, 아니라면 해당 사용자 글 숨기기 가능
                        if(feedWriterId == getUserId(applicationContext)) {
                            popup.menu.setGroupVisible(R.id.feed_manage, true)
                            popup.menu.setGroupVisible(R.id.feed_unfollow, false)
                        } else {
                            popup.menu.setGroupVisible(R.id.feed_manage, false)
                            popup.menu.setGroupVisible(R.id.feed_unfollow, true)
                        }
                        show()
                    }
                }
                R.id.imgDelete -> {
                    previewImgArea.visibility = View.GONE

                    imgDatasCmt.clear()
                }
                R.id.fullImgClose -> {
                    feedImgAdapter.isFullScreen(false)
                    fullImgLayout.visibility = View.GONE
                    feedImgAdapter.replaceList(imgDatas)
                    d(TAG, "pos : ${getPagerPos(applicationContext)}")
                    if(!feedImgAdapter.isCmt) {
                        mViewPager.setCurrentItem(fullImgPager.currentItem, false)
                    } else {
                        mViewPager.setCurrentItem(getPagerPos(applicationContext), false)
                    }
                    feedImgAdapter.isCmt(false)
                }
                R.id.chatSend -> {
                    val cmt = chatInput.text
                    if(cmt.isNotBlank() || imgDatasCmt.isNotEmpty()) {
                        saveImgsURL(imgDatasCmt, imagesList)
                        makeCmt(service, feedId, feedWriterId, cmt.toString(), imagesURL, isSubCmt, mainCmt, context)
                        chatInput.text = null
                        hideSoftKeyboard()

                        if(isSubCmt) {
                            chatInput.setHint(R.string.cmtPh) // "댓글을 입력하세요"로 힌트 변경
                            chatInput.setText("") // 댓글 입력창 비우기

                            isSubCmt = false
                            mainCmt = 0
                            subCmtCtrl.visibility = View.GONE
                        }
                    }

                    previewImgArea.visibility = View.GONE
                    Glide.with(applicationContext).load("").into(preview)

//                    if(!prograssbar.isShown || imgDatasCmt.isEmpty()) {
//                        getCmts(service, feedId)
//                        getFeedDetail(service, feedId, getUserId(applicationContext))
//                    }
                }
                R.id.subCmtCancel -> {
                    chatInput.setHint(R.string.cmtPh) // "댓글을 입력하세요"로 힌트 변경
                    chatInput.setText("") // 댓글 입력창 비우기

                    isSubCmt = false
                    mainCmt = 0
                    subCmtCtrl.visibility = View.GONE

                    hideSoftKeyboard()
                }
            }
        }
    }

    // 피드 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class GroupFeedPopupMenuListener(v: View) : PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 피드 수정 또는 삭제 시 해당 피드의 DB 내 고유 번호 전달하기
        private val feedItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 피드 데이터를 갖고 오기 위함
        private var context = feedItem.context as GroupFeedDetailActivity

        val id = ((feedItem.tag as HashMap<*, *>)["id"]).toString().toInt()
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.update -> { // 피드 수정하기
                    val it = Intent(context, GroupFeedUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.delete -> { // 피드 삭제하기
                    showAlert("피드 삭제하기", "정말 삭제하시겠어요?", id, service)
                    true
                }
                R.id.unfollow -> { // 해당 사용자의 피드 숨기기

                    true
                }
                else -> false
            }
        }

        // 그룹 해체 시 재확인 받는 다이얼로그 띄우기
        // 해체 후 가입한 그룹 목록 액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, feedId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                    context.deleteFeed(service, feedId, context)
                    finish()
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
                .show()
        }
    }

    // 사진 촬영하기
    override fun capturePhoto() {
        Log.d(TAG, "capturePhoto")
        if(imgDatasCmt.size > 1) {
            toast(getString(R.string.maxOnePicWarning))
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            createImageUri(System.currentTimeMillis().toString(), "image/jpeg")?.let { uri ->
                photoURI = uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                getContent.launch(intent)
            }
        }
    }

    // 사진 가져오기(피드 최대 5장, 댓글 최대 1장)
    override fun bringPhoto() {
        Log.d(TAG, "bringPhoto")
        if(imgDatasCmt.size > 1) {
            toast(getString(R.string.maxOnePicWarning))
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            getContent.launch(intent)
            toast(getString(R.string.maxOnePic))
        }
    }

    // 사진 찍기 혹은 가져오기의 결과물 가져오기
    override val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result : $result")
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) { // 사진 여러개 선택한 경우
                    val count = data.clipData!!.itemCount
                    if (count > 5) {
                        toast(getString(R.string.maxOnePicWarning))
                    } else {
                        val imageUri = data.clipData!!.getItemAt(0).uri
                        imgDatasCmt.add(imageUri)

                        previewImgArea.visibility = View.VISIBLE
                        Glide.with(this).load(imageUri).into(preview)
                    }
                } else if(photoURI != null) {
                    d(TAG, "photoURI : $photoURI")
                    d(TAG, "data : $data")
                    val imageUri = photoURI
                    imgDatasCmt.add(imageUri as Any)

                    previewImgArea.visibility = View.VISIBLE
                    Glide.with(this).load(imageUri).into(preview)
                } else {
                    d(TAG, "photo etc")
                }
            }
        }
}