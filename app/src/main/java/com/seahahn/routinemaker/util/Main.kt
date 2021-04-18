package com.seahahn.routinemaker.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.text.Editable
import android.util.Log
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.*
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.notice.NoticeActivity
import com.seahahn.routinemaker.stts.SttsActivity
import com.seahahn.routinemaker.user.MypageActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


open class Main  : Util(), NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, MultiSelectToggleGroup.OnCheckedStateChangeListener, CompoundButton.OnCheckedChangeListener{

    private val TAG = this::class.java.simpleName

    // 좌측 내비게이션 메뉴 항목들 초기화하기
    open lateinit var leftnav : NavigationView // 좌측 내비게이션 메뉴
    lateinit var hd_photo : ImageView // 좌측 내비게이션 메뉴 헤더 이미지
    lateinit var hd_email : TextView // 좌측 내비게이션 메뉴 헤더 이메일
    lateinit var hd_nick : TextView // 좌측 내비게이션 메뉴 헤더 닉네임
    lateinit var hd_mbs : TextView // 좌측 내비게이션 메뉴 헤더 멤버십

    // BottomNavigationView 초기화하기
    lateinit var btmnav : BottomNavigationView

    // 사용자가 선택한 날짜에 따라 툴바 제목도 그에 맞는 날짜로 변경함
    // 초기값은 오늘 날짜
    val cal: Calendar = Calendar.getInstance()
    var y = cal.get(Calendar.YEAR)
    var m = cal.get(Calendar.MONTH)
    var d = cal.get(Calendar.DAY_OF_MONTH)
    var dateData: Calendar = Calendar.getInstance() // 사용자가 선택한 날짜를 yyyy-MM-dd 형식으로 받아온 것. DB에서 데이터 받을 때 사용함
    var dateformatter: DateFormat = SimpleDateFormat("yyyy-MM-dd") // DB 데이터 불러오기 위한 날짜 형식
    var dateDataFormatted: String = dateformatter.format(dateData.time)
    lateinit var title : TextView

    // 메인 액티비티에서는 오늘 날짜를 툴바 제목으로 씀
    private val current = LocalDate.now() // 오늘 날짜 데이터
    private val currentHM = LocalTime.now() // 현재 시각 데이터
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault()) // 문자열 형식(월 일)
    private val formatterHM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) // 문자열 형식(시 분)
    val formatted: String = current.format(formatter) // 툴바 제목에 들어가는 문자열
    val formattedHM: String = currentHM.format(formatterHM) // 툴바 제목에 들어가는 문자열

    // 상단 툴바에 날짜 나올 경우 양쪽에 좌우 화살표 초기화함. 좌는 하루 전, 우는 하루 뒤로 이동시킴
    lateinit var leftArrow : ImageButton
    lateinit var rightArrow : ImageButton

    // MainActivity의 프래그먼트들에 데이터 전달하기 위한 뷰모델
    private val dateViewModel by viewModels<DateViewModel>() // 날짜 데이터
    private val rtTodoViewModel by viewModels<RtTodoViewModel>() // 루틴, 할 일 목록 데이터

    // 루틴과 할 일, 루틴 내 행동 만들기 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    var rtId : Int = 0// DB내 루틴 또는 할 일 고유 번호. 루틴 또는 할 일 수정 시에 필요
    lateinit var mainTitleInput : TextInputEditText
    lateinit var mainTitle : Editable
    lateinit var mainDays : MultiSelectToggleGroup
    lateinit var mainDaysResult : MutableList<String>
    lateinit var activateAlarm : SwitchMaterial
    var activateAlarmResult : Boolean = true
    lateinit var startTime : TextView
    lateinit var startTimeResult : String
    lateinit var memo : EditText
    lateinit var memotxt : Editable
    lateinit var btmBtn : Button
    lateinit var rtOnFeed : SwitchMaterial
    var rtOnFeedResult : Boolean = true

    // FAB가 필요한 액티비티인 경우 초기화하기
    lateinit var fabtn : ConstraintLayout // 여러 개의 FAB 포함한 레이아웃
    lateinit var fabMain : FloatingActionButton
    lateinit var fabTodo : ExtendedFloatingActionButton
    lateinit var fabRt : ExtendedFloatingActionButton
    lateinit var fabAction : FloatingActionButton

    // 우측 하단의 FAB 관련 애니메이션 초기화하기
    // creating variable that handles Animations loading
    // and initializing it with animation files that we have created
    private  val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private  val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private  val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private  val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }
    //used to check if fab menu are opened or closed
    private var fabClosed = false

    // 좌측 내비게이션 메뉴 초기화하기
    fun initLeftNav(hd_email: TextView, hd_nick: TextView, hd_mbs: TextView, hd_photo: ImageView) {

        hd_email.text = UserInfo.getUserEmail(applicationContext)
        hd_nick.text = UserInfo.getUserNick(applicationContext)

//        d(TAG, "mbs : " + UserInfo.getUserMbs(applicationContext))
        val mbs = UserInfo.getUserMbs(applicationContext)
        if(mbs == 0) {
            hd_mbs.text = getString(R.string.mbsBasic)
        } else if(mbs == 1) {
            hd_mbs.text = getString(R.string.mbsPremium)
        }

        val photoUrl = UserInfo.getUserPhoto(applicationContext)
        Glide.with(applicationContext).load(photoUrl)
                .placeholder(R.drawable.warning)
                .error(R.drawable.warning)
                .into(hd_photo)
    }

    // 사용자로부터 메인(루틴 목록), 통계 등의 데이터를 불러오기 위해 필요한 날짜 정보를 받을 때 사용되는 메소드
    fun showDatePicker(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int) {
//        Log.d(TAG, "input : $m $d")
        DatePickerDialog(this,
            { _, year, month, day->
                val cal = Calendar.getInstance()
                cal.set(year, month, day) // 선택한 날짜를 Calendar 형식으로 가져옴
                val date = cal.time // Calendar를 Fri Apr 16 08:11:10 GMT 2021 의 형식으로 변환
//                Log.d(TAG, "date : $date")

                val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바 제목에 세팅하기 위한 형식
                val formatted: String = formatter.format(date)
//                Log.d(TAG, "formatedDate : $formatted")

                c.set(year, month, day) // 사용자의 선택에 따라 툴바에 출력되는 날짜를 변경하기 위함
                dd.set(year, month, day) // 이는 yyyy-MM-dd 형식으로 DB에서 데이터를 가져오기 위함
                title.text = formatted // 툴바 제목에 사용자가 선택한 날짜 집어넣기

                onDateSelected(dateformatter.format(dd.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기
            },
            y, m, d) // DatePicker가 열리면 보여줄 날짜 초기값
            .show()
    }

    // 현재 날짜의 하루 전으로 이동하는 메소드
    fun onedayMove(title: TextView, c: Calendar, dd: Calendar ,y: Int, m: Int, d: Int, updown: Int) {
//        Log.d(TAG, "input : $m $d")
        val cal = Calendar.getInstance()
        val day = d + updown
        cal.set(y, m, day) // 선택한 날짜를 Calendar 형식으로 가져옴
        val date = cal.time
//        Log.d(TAG, "date : $date")

        val formatter: DateFormat = SimpleDateFormat("M월 d일 EEE", Locale.getDefault()) // 툴바에 세팅하기 위한 형식
        val formatted: String = formatter.format(date)
//        Log.d(TAG, "formatedDate : $formatted")
        c.set(y, m, day)
        dd.set(y, m, day)
        title.text = formatted

        onDateSelected(dateformatter.format(dd.time)) // 날짜 데이터 저장하는 뷰모델에 날짜 보내기
    }

    // 뷰모델에 날짜 데이터 저장하기
    fun onDateSelected(date : String) {
        dateViewModel.selectDate(date)
    }

    // 루틴 또는 할 일 만들거나 수정할 경우에 '수행 예정 시각' 선택을 받기 위해서 사용되는 메소드
    fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this,
            {
                _, h, m ->
                startTimeResult = LocalTime.of(h, m).format(formatterHM) // 데이터 저장할 때 쓸 시작 시각 변수
                startTime.text = startTimeResult // 사용자가 선택한 시각에 맞게 화면의 '수행 예정 시각'도 변경시킴
                d(TAG, "startTimeResult : $startTimeResult")
           },
            cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), // timepicker 출력 시의 시각으로 초기값 설정함
            true)
            .show()
    }

    // 상단 툴바의 날짜를 변경하기 위한 클릭 리스너
    inner class DateClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            y = cal.get(Calendar.YEAR)
            m = cal.get(Calendar.MONTH)
            d = cal.get(Calendar.DAY_OF_MONTH)
            dateDataFormatted = dateformatter.format(dateData.time)
            when(v?.id) {
                R.id.toolbarTitle -> { // 제목 클릭한 경우에는 날짜 선택 가능하게 달력(DatePickerDialog)을 띄움
                    showDatePicker(title, cal, dateData, y, m, d)
                }
                R.id.left -> { // 좌측 화살표 누르면 하루 전으로
                    onedayMove(title, cal, dateData, y, m, d, -1)
                }
                R.id.right -> { // 우측 화살표 누르면 하루 뒤로
                    onedayMove(title, cal, dateData, y, m, d, 1)
                }
            }
//            Log.d(TAG, "dateDataFormatted : $dateDataFormatted")
        }
    }

    // 내비(좌측, 하단) 메뉴 클릭 시 작동할 기능
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected")
        when(item.itemId){
            R.id.mypage-> startActivity<MypageActivity>()
            R.id.notice-> startActivity<NoticeActivity>("url" to "https://www.notion.so/e37b0494d08549a2984c58a7962b7a72") // 노션에 만들어둔 공지사항 페이지로 이동
            R.id.faq-> startActivity<NoticeActivity>("url" to "https://www.notion.so/FAQ-cc9c2747132248fab49b2ea1b9079117") // 노션에 만들어둔 FAQ 페이지로 이동
            R.id.qna-> {
                val address : Array<String> = arrayOf(getString(R.string.qnaMail))
                val subject = getString(R.string.qnaSubject)
                val content = getString(R.string.qnaContent)

                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_SUBJECT, subject)
                email.putExtra(Intent.EXTRA_EMAIL, address)
                email.putExtra(Intent.EXTRA_TEXT, content)
                email.type = "message/rfc822"
                startActivity(email)
            }
            R.id.writeReview-> Toast.makeText(this,"writeReview clicked",Toast.LENGTH_SHORT).show()

            R.id.home -> {
                startActivity<MainActivity>()
                overridePendingTransition(0, 0)
            }
            R.id.stts -> {
                startActivity<SttsActivity>()
                overridePendingTransition(0, 0)
            }
            R.id.group -> toast("group")
            R.id.notibtm -> toast("noti")
        }
        return false
    }

    // 뒤로가기 버튼 누르면 좌측 내비게이션 닫기
    override fun onBackPressed() { //뒤로가기 처리
        Log.d(TAG, "onBackPressed")
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

    // 루틴, 할 일 만들기 및 수정 액티비티 내의 공통 요소들 초기화하기
    fun initRtTodoActivity(btmBtnId : Int) {
        // 할 일 이름 입력값 가져오기
        mainTitleInput = findViewById(R.id.mainTitleInput)
        mainTitle = mainTitleInput.text!!

        // 할 일 반복 요일 선택값 가져오기
        mainDays = findViewById(R.id.mainDays)
        mainDays.setOnCheckedChangeListener(this)

        // 알람 활성화 여부 가져오기
        activateAlarm = findViewById(R.id.activateAlarm)
        activateAlarm.setOnCheckedChangeListener(this)

        // 수행 예정 시각 값 가져오기
        startTime = findViewById(R.id.startTime)
        startTime.text = formattedHM
        startTimeResult = formattedHM
        startTime.setOnClickListener(BtnClickListener())

        // 그룹 피드 공개 여부 가져오기
        when(TAG) {
            "RtMakeActivity" -> {
                rtOnFeed = findViewById(R.id.rtOnFeed)
                rtOnFeed.setOnCheckedChangeListener(this)
            }
            "RtUpdateActivity" -> {
                rtOnFeed = findViewById(R.id.rtOnFeed)
                rtOnFeed.setOnCheckedChangeListener(this)
            }
        }

        // 메모 값 가져오기
        memo = findViewById(R.id.memo)
        memotxt = memo.text

        // 하단 '할 일 만들기' 버튼 초기화
        btmBtn = findViewById(btmBtnId)
        setFullBtmBtnText(btmBtn)
        btmBtn.setOnClickListener(BtnClickListener())
    }

    // FAB 버튼 초기화하기
    fun initFAB() {
        when(TAG) {
            "MainActivity" -> {
                fabtn = findViewById(R.id.fabtn)
                fabMain = findViewById(R.id.fabMain)
                fabRt = findViewById(R.id.fabRt)
                fabTodo = findViewById(R.id.fabTodo)
                fabMain.setOnClickListener(BtnClickListener())
                fabRt.setOnClickListener(BtnClickListener())
                fabTodo.setOnClickListener(BtnClickListener())
            }
        }
    }

    // fabMain 클릭 시 작동할 기능 초기화
    private fun onFABMainClick() {
        setVisibility(fabClosed)
        setAnimation(fabClosed)
        fabClosed = !fabClosed
    }

    // FAB 누르면 동작할 애니메이션 세팅하기
    // A Function used to set the Animation effect
    private fun setAnimation(closed:Boolean) {
        if(!closed){
            fabTodo.startAnimation(fromBottom)
            fabRt.startAnimation(fromBottom)
            fabMain.startAnimation(rotateOpen)
        }else{
            fabTodo.startAnimation(toBottom)
            fabRt.startAnimation(toBottom)
            fabMain.startAnimation(rotateClose)
        }
    }
    // FAB 누르면 숨겨진 버튼들 보이거나 가리기
    // used to set visibility to VISIBLE / INVISIBLE
    private fun setVisibility(closed:Boolean) {
        if(!closed)
        {
            fabTodo.visibility = View.VISIBLE
            fabRt.visibility = View.VISIBLE
        }else{
            fabTodo.visibility = View.INVISIBLE
            fabRt.visibility = View.INVISIBLE
        }
    }

    // 액티비티 내 버튼 눌렀을 때의 동작 구현
    inner class BtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                // 우측 하단에 위치한 FAB인 경우
                R.id.fabMain -> onFABMainClick()
                R.id.fabRt -> startActivity<RtMakeActivity>() // '루틴 만들기' 액티비티로 이동
                R.id.fabTodo -> startActivity<TodoMakeActivity>() // '할 일 만들기' 액티비티로 이동

                // 하단에 가로로 꽉 차는 버튼인 경우
                R.id.makeRt -> {
                    val type = getString(R.string.rt) // 루틴 만들기 액티비티이므로 루틴("rt")로 설정함
                    makeRt(service, type, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, startTimeResult, rtOnFeedResult,
                        memotxt.toString(), UserInfo.getUserId(applicationContext))
                }
                R.id.updateRt -> {
                    mainTitle = mainTitleInput.text!!
                    memotxt = memo.text
                    updateRt(service, rtId, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, startTimeResult, rtOnFeedResult,
                        memotxt.toString())
                }
                R.id.makeTodo -> toast("makeTodo")
                R.id.updateTodo -> toast("updateTodo")

                // 루틴, 할 일 만들기 or 수정에서 "수행 예정 시각" 받아오기
                R.id.startTime -> showTimePicker()
            }
        }
    }

    // 루틴, 할 일, 루틴 내 행동 만들기 및 수정하기 액티비티에서 선택된 반복 요일 값 가져오기
    override fun onCheckedStateChanged(group: MultiSelectToggleGroup?, checkedId: Int, isChecked: Boolean) {
        val checkedIds = group?.checkedIds // 선택된 요일들의 id값 가져오기
        val array = checkedIds?.toIntArray() // 가져온 id값을 Array로 변환

        val days: MutableList<String> = mutableListOf() // 선택된 요일들을 문자열로 바꿔서 담을 리스트
        for (i in 0 until array!!.size) {
            var checkedDay = ""
            when(array[i]) {
                2131231309 -> checkedDay = getString(R.string.sunday)
                2131231103 -> checkedDay = getString(R.string.monday)
                2131231380 -> checkedDay = getString(R.string.tuesday)
                2131231432 -> checkedDay = getString(R.string.wednesday)
                2131231347 -> checkedDay = getString(R.string.thursday)
                2131230985 -> checkedDay = getString(R.string.friday)
                2131231231 -> checkedDay = getString(R.string.saturday)
            }
            days.add(checkedDay)
        }

        mainDaysResult = days // 일, 월, ... , 토 의 순서대로 정렬한 결과를 액티비티에서 사용할 변수에 담아줌
        /* 일 월 화 수 목 금 토
        * 2131231309 2131231103 2131231380 2131231432 2131231347 2131230985 2131231231
        */
        Log.d(TAG, "mainDaysResult : $mainDaysResult")
    }

    // 루틴, 할 일, 루틴 내 행동 만들기 및 수정하기 액티비티에서 시작 알람 활성화 여부 및 그룹 피드 공개 여부 값 가져오기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.activateAlarm -> {
                activateAlarmResult = isChecked
                d(TAG, "체크 여부 : $activateAlarmResult, $rtOnFeedResult")
            }
            R.id.rtOnFeed -> {
                rtOnFeedResult = isChecked
                d(TAG, "체크 여부 : $activateAlarmResult, $rtOnFeedResult")
            }
        }
    }

    // '루틴 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(루틴 만들기)
    fun makeRt(
        service : RetrofitService,
        mType : String,
        title : String,
        mDays : List<String>,
        alarm : Boolean,
        time : String,
        onFeed : Boolean,
        memo : String,
        userId : Int) {
        d(TAG, "변수들 : $mType, $title, $mDays, $alarm, $time, $onFeed, $memo, $userId")
        val days = mDays.joinToString(separator = " ") // 리스트를 문자열로 바꿔서 보내기 위함
        d(TAG, "days : $days")
        service.makeRt(mType, title, days, alarm, time, onFeed, memo, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 저장하기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 저장하기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                d(TAG, response.body().toString())
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

    // '루틴 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(루틴 만들기)
    fun updateRt(
        service : RetrofitService,
        id : Int,
        title : String,
        mDays : List<String>,
        alarm : Boolean,
        time : String,
        onFeed : Boolean,
        memo : String) {
        d(TAG, "변수들 : $id, $title, $mDays, $alarm, $time, $onFeed, $memo")
        val days = mDays.joinToString(separator = " ") // 리스트를 문자열로 바꿔서 보내기 위함
        d(TAG, "days : $days")
        service.updateRt(id, title, days, alarm, time, onFeed, memo).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 수정하기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 수정하기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                d(TAG, response.body().toString())
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

    // '루틴 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(루틴 만들기)
    fun getRts(service: RetrofitService, userId : Int) {
        d(TAG, "변수들 : $userId")
        service.getRts(userId).enqueue(object : Callback<MutableList<RtData>> {
            override fun onFailure(call: Call<MutableList<RtData>>, t: Throwable) {
                d(TAG, "루틴(할 일) 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<RtData>>, response: Response<MutableList<RtData>>) {
                d(TAG, "루틴(할 일) 목록 가져오기 요청 응답 수신 성공")
                d(TAG, "getRt : "+response.body().toString())
                val rtdatas = response.body()
                rtTodoViewModel.setList(rtdatas!!)
            }
        })
    }

    // '루틴 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(루틴 만들기)
    fun getRt(service: RetrofitService, rtId : Int) {
        d(TAG, "변수들 : $rtId")
        service.getRt(rtId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 데이터 가져오기 요청 응답 수신 성공")
                d(TAG, "getRt : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)

                mainTitleInput.setText(gson.get("rtTitle").asString)
//                mainDays mDays
                activateAlarm.isChecked = gson.get("alarm").asInt == 1
                startTime.text = gson.get("time").asString
                rtOnFeed.isChecked = gson.get("onFeed").asInt == 1
                memo.setText(gson.get("memo").asString)
            }
        })
    }
}