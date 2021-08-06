package com.seahahn.routinemaker.notice

import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeActivity : Sns() {

    private val TAG = this::class.java.simpleName

    val noticeListViewModel by viewModels<NoticeListViewModel>() // 알림 목록 데이터

    private lateinit var noticeListAdapter: NoticeListAdapter
    private lateinit var noticeList: RecyclerView
    var mDatas = mutableListOf<NoticeData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        // 레트로핏 통신 연결
        service = initRetrofit()

        // 사용자의 알림 목록 불러오기
        getNotices(service, getUserId(this))

        // 좌측 Navigation Drawer 초기화
        drawerLayout = findViewById(R.id.drawer_layout)
        leftnav = findViewById(R.id.leftnav)
        leftnav.setNavigationItemSelectedListener(this)
        val leftnav_header = leftnav.getHeaderView(0)

        toolbarTitle = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.notiList) // 툴바 제목에 들어갈 텍스트
        initToolbar(toolbarTitle, titleText, 0) // 툴바 세팅하기

        // 좌측 내비 메뉴의 헤더 부분에 사용자 정보 넣기
        hd_email = leftnav_header.findViewById(R.id.hd_email)
        hd_nick = leftnav_header.findViewById(R.id.hd_nick)
        hd_mbs = leftnav_header.findViewById(R.id.hd_mbs)
        hd_photo = leftnav_header.findViewById(R.id.hd_photo)
        initLeftNav(hd_email, hd_nick, hd_mbs, hd_photo)

        // 하단 BottomNavigationView 초기화
        btmnav = findViewById(R.id.btmnav)
        btmnav.selectedItemId = R.id.notibtm
        btmnav.setOnNavigationItemSelectedListener(this)
        setBtmNavBadge()

        noticeList = findViewById(R.id.noticeList) // 리사이클러뷰 초기화
        noticeListAdapter = NoticeListAdapter(this) // 어댑터 초기화
        noticeList.adapter = noticeListAdapter // 어댑터 연결
        noticeListAdapter.getService(service)

        viewEmptyList = findViewById(R.id.view_empty_list) // 보여줄 데이터 없을 때 출력할 뷰

        noticeListViewModel.gottenNoticeData.observe(this) { noticeDatas ->
            Logger.d(TAG, "noticeDatas : $noticeDatas")
            mDatas = noticeDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기
            noticeListAdapter.replaceList(mDatas) // 사용자 고유 번호에 맞춰서 알림 목록 띄우기

            // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
            if(noticeListAdapter.itemCount == 0) {
                viewEmptyList.visibility = View.VISIBLE
            } else {
                viewEmptyList.visibility = View.GONE
            }
        }

        // 알림 수신 시 알림 목록 갱신을 위한 브로드캐스트 리시버를 초기화 및 등록함
        val receiver = RefreshReceiver()
        val filter = IntentFilter()
        filter.addAction("refresh") // 수신할 액션 종류 넣기
        registerReceiver(receiver, filter)
    }

    override fun onResume() {
        super.onResume()

        // 알림 목록 들어오면 상단 알림 없애기
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    // 툴바 우측 메뉴 버튼 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)       // 시간대 선택 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 우측 메뉴 눌렀을 때 동작할 내용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.toolbarFilter -> {
                // 알림 설정으로 이동
                val it = Intent(this, NoticeSettingActivity::class.java)
                startActivity(it)
                overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 사용자의 알림 목록 불러오기
    fun getNotices(service: RetrofitService, userId : Int) {
        Logger.d(TAG, "getNotices 변수들 : $userId")
        service.getNotices(userId).enqueue(object : Callback<MutableList<NoticeData>> {
            override fun onFailure(call: Call<MutableList<NoticeData>>, t: Throwable) {
                Log.d(TAG, "피드 댓글 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<NoticeData>>, response: Response<MutableList<NoticeData>>) {
                Log.d(TAG, "피드 댓글 목록 가져오기 요청 응답 수신 성공")
                Logger.d(TAG, "getNotices : " + response.body().toString())
                val noticeDatas = response.body()
                try {
                    noticeListViewModel.setList(noticeDatas!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

    // SNS 관련 알림 수신 시 알림을 보여주면서 알림 목록도 함께 갱신시키기 위한 브로드캐스트 리시버
    inner class RefreshReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "refresh") getNotices(service, getUserId(applicationContext))
        }

    }
}