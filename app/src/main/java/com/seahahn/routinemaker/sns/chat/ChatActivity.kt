package com.seahahn.routinemaker.sns.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.SnsChat
import com.seahahn.routinemaker.util.UserInfo.getUserId
import java.io.*
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*


class ChatActivity : SnsChat() {

    private val TAG = this::class.java.simpleName

    private lateinit var chatContentsAdapter: ChatContentsAdapter
    private lateinit var chatContentsView: RecyclerView
    var mDatas = mutableListOf<ChatMsg>()
//    var showDatas = mutableListOf<ChatMsg>()
    var searchedDatas = mutableListOf<ChatMsg>()
    lateinit var it_mDatas : Iterator<ChatMsg>

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 레트로핏 통신 연결
        service = initRetrofit()

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.searchGroup) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹명 검색창
        searchView.setOnQueryTextListener(QueryTextChenageListener())

        initChatInput(R.string.chatPh) // 하단 채팅 입력란 초기화하기
        initFullImgLayout() // 이미지 전체 보기 레이아웃 초기화
        photoInput.tag = 1 // 최대 업로드 가능한 사진 개수
        photoInput.setOnClickListener(ImgAddClickListener())
        chatSend.setOnClickListener(BtnClickListener())
        fullImgClose.setOnClickListener(BtnClickListener())

        chatContentsView = findViewById(R.id.chatContents) // 리사이클러뷰 초기화
        chatContentsAdapter = ChatContentsAdapter() // 어댑터 초기화
        chatContentsView.adapter = chatContentsAdapter // 어댑터 연결
        chatContentsAdapter.getService(service)
        chatContentsAdapter.getContext(this)

        // 채팅 내용 목록 가져오기
        val isGroupchat = intent.getBooleanExtra("isGroupchat", false) // 그룹 채팅 여부 가져오기
        val hostId = intent.getIntExtra("hostId", getUserId(this)) // 그룹 생성자 또는 사용자 본인 고유 번호 가져오기
        val audienceId = intent.getIntExtra("audienceId", 0) // 그룹 고유 번호 또는 1:1 채팅 상대방 고유 번호 가져오기
        getChatRoomData(this, service, isGroupchat, hostId, audienceId) // 채팅방 데이터 가져오기

//        chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(this) { chatMsgs ->
//            d(TAG, "chatMsgs : $chatMsgs")
//            mDatas = chatMsgs // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기
//
//            chatContentsAdapter.replaceList(mDatas)
//        }

        chatContentsViewModel.gottenChatMsg.observe(this) { chatMsgs ->
            Log.d(TAG, "chatMsgs : $chatMsgs")
            mDatas = chatMsgs // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            chatContentsAdapter.replaceList(mDatas) // 사용자 고유 번호에 맞춰서 가입한 그룹 목록 띄우기
            if(!chatContentsView.canScrollVertically(1)) {
                // 최하단일 경우 메시지 오면 맨 아래에 스크롤 가도록 함
                chatContentsView.smoothScrollToPosition(chatContentsAdapter.itemCount)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConnect() // 소켓 연결 해제
    }

    // 검색창에 검색어를 입력할 경우의 동작
    inner class QueryTextChenageListener() : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "text submitted")
            val inputText = query!!.lowercase(Locale.getDefault())

            searchedDatas.clear() // 검색 결과 목록 비우기
            it_mDatas = mDatas.iterator() // 사용자가 가입하지 않았고 그룹 멤버 수가 인원 제한에 도달하지 않은 그룹 목록에서 검색 결과 뽑기
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                // 검색 시 대소문자 구분 없이 검색 결과에 출력되기 위해서 전부 소문자로 변환
                if (it_mData.content.lowercase(Locale.getDefault()).contains(inputText) || it_mData.content.lowercase(Locale.getDefault()).contains(inputText)) {
                    searchedDatas.add(it_mData)
                }
            }
            chatContentsAdapter.replaceList(searchedDatas) // 검색어 결과에 따라 추출된 목록을 보여줌

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    }

    // 피드의 좋아요, 댓글, 더보기와 하단의 카메라, 댓글 달기 아이콘 클릭 시 작동할 기능
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v!!.id) {
//                R.id.imgDelete -> {
//                    previewImgArea.visibility = View.GONE
//
//                    imgDatasCmt.clear()
//                }
//                R.id.fullImgClose -> {
//                    feedImgAdapter.isFullScreen(false)
//                    fullImgLayout.visibility = View.GONE
//                    feedImgAdapter.replaceList(imgDatas)
//                    Logger.d(TAG, "pos : ${AppVar.getPagerPos(applicationContext)}")
//                    if(!feedImgAdapter.isCmt) {
//                        mViewPager.setCurrentItem(fullImgPager.currentItem, false)
//                    } else {
//                        mViewPager.setCurrentItem(AppVar.getPagerPos(applicationContext), false)
//                    }
//                    feedImgAdapter.isCmt(false)
//                }
                R.id.chatSend -> {
//                    val msgInput = chatInput.text
                    if(chatInput.text.isNotBlank()) {
//                        saveImgsURL(imgDatasCmt, imagesList)
                        makeMsg(service, chatInput.text.toString(), 0, chatroomData.id)
                        object : Thread() {
                            override fun run() {
                                val now = LocalDateTime.now()
                                val chatMsg = ChatMsg(0,
                                    getUserId(applicationContext),
                                    chatInput.text.toString(),
                                    0,
                                    chatroomData.id,
                                    now.format(formatterYMDHM).toString())

                                val gson = Gson().toJson(chatMsg)

//                                sendMessageToServer(chatInput.text.toString())
                                sendMessageToServer(gson)
//                                chatInput.text = null
                            }
                        }.start()
//                        hideSoftKeyboard()
                    }


//                    previewImgArea.visibility = View.GONE
//                    Glide.with(applicationContext).load("").into(preview)
//
//                    if(!prograssbar.isShown || imgDatasCmt.isEmpty()) getCmts(service, feedId)
                }
            }
        }
    }
}