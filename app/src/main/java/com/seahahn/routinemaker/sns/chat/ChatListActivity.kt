package com.seahahn.routinemaker.sns.chat

import android.os.Bundle
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.ChatroomsAdapterItemMoveCallback
import com.seahahn.routinemaker.util.SnsChat
import java.util.*

class ChatListActivity : SnsChat() {

    private val TAG = this::class.java.simpleName

    private val context = this

//    private lateinit var viewEmptyList : LinearLayout

    private lateinit var chatroomsAdapter: ChatroomsAdapter
    private lateinit var chatroomList: RecyclerView
    var mDatas = mutableListOf<ChatRoom>()
    var showDatas = mutableListOf<ChatRoom>()
    var searchedDatas = mutableListOf<ChatRoom>()
    lateinit var it_mDatas : Iterator<ChatRoom>

    private lateinit var searchView: SearchView
    private var isSearchViewShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // 레트로핏 통신 연결
        service = initRetrofit()

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.chatList) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 1) // 툴바 세팅하기

        searchView = findViewById(R.id.searchView) // 그룹명 검색창
        searchView.setOnQueryTextListener(QueryTextChenageListener())

        chatroomList = findViewById(R.id.chatList) // 리사이클러뷰 초기화
        chatroomsAdapter = ChatroomsAdapter(this) // 어댑터 초기화
        chatroomList.adapter = chatroomsAdapter // 어댑터 연결
        chatroomsAdapter.getService(service)

        // 채팅방 스와이프 후 나가기 버튼 나오도록 ItemMoveCallback에 연결결
        val chatroomsAdapterItemMoveCallback = ChatroomsAdapterItemMoveCallback().apply {
            setClamp(200f)
        }
        val itemTouchHelper = ItemTouchHelper(chatroomsAdapterItemMoveCallback)
        itemTouchHelper.attachToRecyclerView(chatroomList)
        chatroomList.apply {
            setOnTouchListener { _, _ ->
                chatroomsAdapterItemMoveCallback.removePreviousClamp(this)
                false
            }
        }

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰
    }

    override fun onResume() {
        super.onResume()
        d(TAG, "ChatList onResume")

        // 채팅 목록 가져오기
        chatDB!!.chatDao().getChatrooms().observe(context) { chatroomData ->
            d(TAG, "chatroomData : $chatroomData")
            mDatas = chatroomData // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            chatroomsAdapter.replaceList(mDatas) // 사용자 고유 번호에 맞춰서 가입한 그룹 목록 띄우기
            chatroomsAdapter.saveOriginalList(mDatas) // 원본 목록 저장하기(검색 이후 다시 제자리로 돌려놓기 위함)

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(chatroomsAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        }
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarSearch -> {
                // 검색창 띄우기
                isSearchViewShown = !isSearchViewShown
                if(isSearchViewShown) {
                    searchView.visibility = View.VISIBLE
                } else {
                    searchView.visibility = View.GONE
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 검색창에 검색어를 입력할 경우의 동작
    inner class QueryTextChenageListener() : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            d(TAG, "text submitted")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
//            d(TAG, "text changed")
            val inputText = newText!!.lowercase(Locale.getDefault())
            chatroomsAdapter.filter.filter(inputText) // 검색어 결과에 따라 추출된 목록을 보여줌

//            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
//            if (chatroomsAdapter.itemCount == 0) {
//                viewEmptyList.visibility = View.VISIBLE
//            } else {
//                viewEmptyList.visibility = View.GONE
//            }

//            if (newText == "") {
//                viewEmptyList.visibility = View.GONE
//                chatroomsAdapter.replaceList(mDatas) // 검색창이 비었으면 다시 전체 목록을 출력함
//            }

            return true
        }
    }
}