package com.seahahn.routinemaker.util

import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.seahahn.routinemaker.util.UserInfo.getUserId
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
    private val formatterMDDoW: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault()) // 문자열 형식(월 일 요일)
    private val formatterMonthDay: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault()) // 문자열 형식(월 일)
    private val formatterymd : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatterHM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) // 문자열 형식(시 분)
    val formattedMDDoW: String = current.format(formatterMDDoW) // 툴바 제목에 들어가는 문자열
    val formattedMonthDay: String = current.format(formatterMonthDay) // 할 일 수행예정일에 들어가는 문자열(월 일)
    val formattedymd: String = current.format(formatterymd) // 할 일 수행예정일에 해당하는 날짜 데이터 값(yyyy-MM-dd)
    val formattedHM: String = currentHM.format(formatterHM) // 툴바 제목에 들어가는 문자열

    // 상단 툴바에 날짜 나올 경우 양쪽에 좌우 화살표 초기화함. 좌는 하루 전, 우는 하루 뒤로 이동시킴
    lateinit var leftArrow : ImageButton
    lateinit var rightArrow : ImageButton

    // MainActivity의 프래그먼트들에 데이터 전달하기 위한 뷰모델
    private val dateViewModel by viewModels<DateViewModel>() // 날짜 데이터
    private val rtTodoViewModel by viewModels<RtTodoViewModel>() // 루틴, 할 일 목록 데이터
    private val actionViewModel by viewModels<ActionViewModel>() // 루틴, 할 일 목록 데이터

    // 루틴과 할 일, 루틴 내 행동 만들기 및 수정에 관한 액티비티에 포함된 요소들 초기화하기
    var rtId : Int = 0// DB내 루틴 또는 할 일 고유 번호. 루틴 또는 할 일 수정 시에 필요
    var actionId : Int = 0// DB내 루틴 내 행동 고유 번호. 루틴 내 행동 수정 시에 필요
    lateinit var mainTitleInput : TextInputEditText
    lateinit var mainTitle : Editable
    lateinit var mainDays : MultiSelectToggleGroup
    var mainDaysResult : MutableList<String> = mutableListOf()
    lateinit var activateAlarm : SwitchMaterial
    var activateAlarmResult : Boolean = true
    lateinit var startTime : TextView // 루틴, 할 일의 수행 예정 시각
    lateinit var startTimeResult : String // DB에 저장될 수행 예정 시각 값
    var startTimeResultParsed : LocalTime = LocalTime.now() // TimePicker에 보여주기 위해서 LocalTime 형식으로 바꾼 수행 예정 시각 값
    lateinit var startDate : TextView // 할 일의 수행 예정일
    lateinit var startDateResult : String // DB에 저장될 수행 예정일 값
    var startDateResultParsed : LocalDate = LocalDate.now() // DatePicker에 보여주기 위해서 LocalTime 형식으로 바꾼 수행 예정 시각 값
    lateinit var timecost : EditText // 루틴 내 행동의 예상 소요 시간
    lateinit var timecostResult : Editable // DB에 저장될 예상 소요 시간 값
    lateinit var memo : EditText
    lateinit var memotxt : Editable
    lateinit var btmBtn : Button
    lateinit var rtOnFeed : SwitchMaterial
    var rtOnFeedResult : Boolean = true
    lateinit var repeat : SwitchMaterial
    var repeatResult : Boolean = true

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
    fun setToolbarDate(title: TextView, c: Calendar, dd: Calendar, y: Int, m: Int, d: Int) {
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

    // 할 일 만들기, 수정하기의 수행 예정일 선택을 받기 위한 메소드
    fun setStartDate() {
        DatePickerDialog(this,
            { _, year, month, day->
                val date = LocalDate.of(year, month+1, day)
                val formatted: String = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                startDate.text = date.format(formatterMonthDay) // 수행 예정일에 사용자가 선택한 날짜 집어넣기
                startDateResult = formatted
            },
            startDateResultParsed.year, startDateResultParsed.monthValue-1, startDateResultParsed.dayOfMonth) // DatePicker가 열리면 보여줄 날짜 초기값
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
        TimePickerDialog(this,
            {
                _, h, m ->
                startTimeResult = LocalTime.of(h, m).format(formatterHM) // 데이터 저장할 때 쓸 시작 시각 변수
                startTime.text = startTimeResult // 사용자가 선택한 시각에 맞게 화면의 '수행 예정 시각'도 변경시킴
                d(TAG, "startTimeResult : $startTimeResult")
           },
            startTimeResultParsed.hour, startTimeResultParsed.minute, // timepicker 출력 시의 시각으로 초기값 설정함
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
                    setToolbarDate(title, cal, dateData, y, m, d)
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
        d(TAG, "onNavigationItemSelected")
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
        d(TAG, "onBackPressed")
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
        // 루틴 또는 할 일 제목 입력값 가져오기
        mainTitleInput = findViewById(R.id.mainTitleInput)
        mainTitle = mainTitleInput.text!!

        // 루틴 또는 할 일 반복 요일 선택값 가져오기
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

        // 액티비티별로 별개인 요소 초기화하기
        when(TAG) {
            // 루틴 만들기, 수정하기 액티비티는 그룹 피드 공개 여부 초기화하기
            "RtMakeActivity" -> {
                rtOnFeed = findViewById(R.id.rtOnFeed)
                rtOnFeed.setOnCheckedChangeListener(this)
            }
            "RtUpdateActivity" -> {
                rtOnFeed = findViewById(R.id.rtOnFeed)
                rtOnFeed.setOnCheckedChangeListener(this)
            }

            // 할 일 만들기, 수정하기 액티비티는 반복 여부 및 수행 예정일 초기화하기
            "TodoMakeActivity" -> {
                repeat = findViewById(R.id.repeat) // 반복 여부 스위치 초기화하기
                repeat.setOnCheckedChangeListener(this)

                startDate = findViewById(R.id.startDate) // 수행 예정일 초기화하기
                startDate.setOnClickListener(BtnClickListener())
                startDate.text = formattedMonthDay
                startDateResult = formattedymd
                repeat.setOnCheckedChangeListener(this)
            }
            "TodoUpdateActivity" -> {
                repeat = findViewById(R.id.repeat) // 반복 여부 스위치 초기화하기
                repeat.setOnCheckedChangeListener(this)

                startDate = findViewById(R.id.startDate) // 수행 예정일 초기화하기
                startDate.setOnClickListener(BtnClickListener())
                startDate.text = formattedMonthDay
                startDateResult = formattedymd
                repeat.setOnCheckedChangeListener(this)
            }
        }

        // 메모 값 가져오기
        memo = findViewById(R.id.memo)
        memotxt = memo.text

        // 하단 버튼 초기화
        btmBtn = findViewById(btmBtnId)
        setFullBtmBtnText(btmBtn)
        btmBtn.setOnClickListener(BtnClickListener())
    }

    // 루틴 내 행동 만들기 및 수정 액티비티 내의 공통 요소들 초기화하기
    fun initActionActivity(btmBtnId : Int) {
        // 루틴 내 행동 이름 입력값 가져오기
        mainTitleInput = findViewById(R.id.mainTitleInput)
        mainTitle = mainTitleInput.text!!

        // 예상 소요 시간 값 가져오기
        timecost = findViewById(R.id.timecost)
        timecostResult = timecost.text

        // 메모 값 가져오기
        memo = findViewById(R.id.memo)
        memotxt = memo.text

        // 하단 버튼 초기화
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
            "ActionListActivity" -> {
                fabtn = findViewById(R.id.fabtn)
                fabMain = findViewById(R.id.fabMain)
                fabMain.setOnClickListener {
                    startActivity<ActionMakeActivity>("id" to rtId) // '행동 추가' 액티비티로 이동
                }
            }
        }
    }

    // fabMain 클릭 시 작동할 기능 초기화
    private fun onFABMainClick() {
        setAnimation(fabClosed)
        setVisibility(fabClosed)
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
//            fabTodo.visibility = View.VISIBLE
//            fabRt.visibility = View.VISIBLE
            fabTodo.show()
            fabRt.show()
        }else{
//            fabTodo.visibility = View.INVISIBLE
//            fabRt.visibility = View.INVISIBLE
            fabTodo.hide()
            fabRt.hide()
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
                    if(inputCheck()) makeRt(service, type, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, getMDate(mainDaysResult.joinToString(separator = " "), "", 0), startTimeResult, rtOnFeedResult,
                        memotxt.toString(), getUserId(applicationContext))
                }
                R.id.updateRt -> {
                    mainTitle = mainTitleInput.text!!
                    memotxt = memo.text
                    if(inputCheck()) updateRt(service, rtId, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, getMDate(mainDaysResult.joinToString(separator = " "), "", 0), startTimeResult, rtOnFeedResult, memotxt.toString())
                }
                R.id.makeTodo -> {
                    val type = getString(R.string.todo) // 루틴 만들기 액티비티이므로 루틴("rt")로 설정함
                    if(inputCheck()) makeRt(service, type, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, startDateResult, startTimeResult, repeatResult,
                        memotxt.toString(), getUserId(applicationContext))
                }
                R.id.updateTodo -> {
                    mainTitle = mainTitleInput.text!!
                    memotxt = memo.text
                    if(inputCheck()) updateRt(service, rtId, mainTitle.toString(), mainDaysResult,
                        activateAlarmResult, startDateResult, startTimeResult, repeatResult, memotxt.toString())
                }
                R.id.makeAction -> {
                    if(inputCheck()) makeAction(service, mainTitle.toString(), timecostResult.toString(), memotxt.toString(), rtId)
                }
                R.id.updateAction -> {
                    mainTitle = mainTitleInput.text!!
                    timecostResult = timecost.text
                    memotxt = memo.text
                    if(inputCheck()) updateAction(service, actionId, mainTitle.toString(), timecostResult.toString(), memotxt.toString())
                }

                // 루틴, 할 일 만들기 or 수정에서 "수행 예정 시각" 또는 "수행 예정일" 받아오기
                R.id.startTime -> showTimePicker()
                R.id.startDate -> setStartDate()
            }
        }
    }

    // 루틴 만들거나 수정할 경우 사용자가 선택한 수행 요일에 맞춰서 가장 빠른 날짜를 구하기 위한 메소드
    fun getMDate(mDays: String, mDateInput : String, daysToAdd : Long): String {
        // 루틴 또는 할 일을 수행한 날
        val doneDay : LocalDate = if(mDateInput != "") {
            LocalDate.parse(mDateInput)
        } else {
            LocalDate.now()
        }
        var mDate = doneDay.plusDays(daysToAdd) // DB에 저장될 날짜(내일 날짜부터 시작해서 수행 요일에 맞는 날짜 찾은 후에 이 변수에 넣음)
        var dayOfWeek = getDayOfWeek(mDate) // 요일 이름을 넣어둘 변수

        while (!mDays.contains(dayOfWeek)) { // 루틴 또는 반복하는 할 일인 경우 다음 수행 요일에 맞는 날짜값 찾기
            mDate = mDate.plusDays(1)
            dayOfWeek = getDayOfWeek(mDate)

//            d(TAG, "day : $mDate")
//            d(TAG, "dayOfWeek : $dayOfWeek")
        }
        return mDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // getMDate 메소드에서 사용자가 선택한 요일의 문자열값을 얻기 위한 메소드
    fun getDayOfWeek(day : LocalDate): String {
        var result = ""
        when (day.dayOfWeek.toString()) {
            "SUNDAY" -> result = "일"
            "MONDAY" -> result = "월"
            "TUESDAY" -> result = "화"
            "WEDNESDAY" -> result = "수"
            "THURSDAY" -> result = "목"
            "FRIDAY" -> result = "금"
            "SATURDAY" -> result = "토"
        }
        return result
    }

    // 툴바 우측 버튼 눌렀을 때의 동작 구현
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        d(TAG, "onOptionsItemSelected Main")
        when(item.itemId){
            R.id.toolbarTrash -> showAlert("삭제하기", "정말 삭제하시겠어요?", "확인", "취소")
            R.id.toolbarUpdate -> {
                when(TAG) {
                    "ActionListActivity" -> startActivity<RtUpdateActivity>("id" to rtId) // '루틴 수정하기' 액티비티로 이동
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 루틴 또는 할 일 삭제 시 재확인 받는 다이얼로그 띄우기
    override fun showAlert(title: String, msg: String, pos: String, neg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { _: DialogInterface, _: Int ->
                if(TAG == "ActionUpdateActivity") {
                    deleteAction(service, actionId, rtId, this)
                    finish()
                } else {
                    deleteRt(service, rtId, this)
                    finish()
                }
            }
            .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
            .show()
    }

    // 루틴, 할 일, 루틴 내 행동의 제목을 입력하지 않은 경우 입력하도록 안내하기
    // 반복 요일 설정해야 하는데 안 헀을 경우 최소한 1개 요일 입력하도록 안내하기
    fun inputCheck(): Boolean {
        d(TAG, "mainDaysResult.size : "+mainDaysResult.size)
        if(mainTitle.isBlank()) {
            // 루틴, 할 일, 루틴 내 행동의 제목을 입력하지 않은 경우 입력하도록 안내하기
            toast(getString(R.string.titleEmpty))
            return false
        }
        else if(((TAG == "RtMakeActivity" || TAG == "RtUpdateActivity") || ((TAG == "TodoMakeActivity" || TAG == "TodoUpdateActivity") && repeatResult))
            && mainDaysResult.size == 0) {
            // 반복 요일 설정해야 하는데 안 헀을 경우 최소한 1개 요일 입력하도록 안내하기
            toast(getString(R.string.daysEmpty))
            return false
        }
        else if((TAG == "ActionMakeActivity" || TAG == "ActionUpdateActivity") && (timecostResult.isBlank() || timecostResult.equals(0))) {
            // 루틴 내 행동의 예상 소요 시간 입력하지 않았거나 0인 경우
            toast(getString(R.string.timecostEmpty))
            return false
        }
        return true
    }

    // 루틴, 할 일, 루틴 내 행동 만들기 및 수정하기 액티비티에서 선택된 반복 요일 값 가져오기
    override fun onCheckedStateChanged(group: MultiSelectToggleGroup?, checkedId: Int, isChecked: Boolean) {
        val checkedIds = group?.checkedIds // 선택된 요일들의 id값 가져오기
        val array = checkedIds?.toIntArray() // 가져온 id값을 Array로 변환
        d(TAG, "checkedIds : $checkedIds")
        d(TAG, "array : "+array.toString())
        d(TAG, "checkedId : "+ resources.getResourceEntryName(checkedId))

        val days: MutableList<String> = mutableListOf() // 선택된 요일들을 문자열로 바꿔서 담을 리스트
        for (i in 0 until array!!.size) {
            var checkedDay = resources.getResourceEntryName(array[i])
            when(checkedDay) {
                "sun" -> checkedDay = getString(R.string.sunday)
                "mon" -> checkedDay = getString(R.string.monday)
                "tue" -> checkedDay = getString(R.string.tuesday)
                "wed" -> checkedDay = getString(R.string.wednesday)
                "thu" -> checkedDay = getString(R.string.thursday)
                "fri" -> checkedDay = getString(R.string.friday)
                "sat" -> checkedDay = getString(R.string.saturday)
            }
            days.add(checkedDay)
        }
        mainDaysResult = days // 일, 월, ... , 토 의 순서대로 정렬한 결과를 액티비티에서 사용할 변수에 담아줌
        d(TAG, "mainDaysResult : $mainDaysResult")
    }

    // 루틴, 할 일, 루틴 내 행동 만들기 및 수정하기 액티비티에서 시작 알람 활성화 여부 및 그룹 피드 공개 여부 값 가져오기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.activateAlarm -> {
                activateAlarmResult = isChecked
            }
            R.id.rtOnFeed -> {
                rtOnFeedResult = isChecked
            }
            R.id.repeat -> {
                repeatResult = isChecked
                if(!repeatResult) {
                    mainDays.isEnabled = repeatResult
                    mainDays.clearCheck()
                    mainDays.visibility = View.GONE
                } else {
                    mainDays.isEnabled = repeatResult
                    mainDays.visibility = View.VISIBLE
                }
            }
        }
    }

    // '루틴 만들기' 액티비티의 하단 버튼 눌렀을 때의 동작(루틴 만들기)
    fun makeRt(service : RetrofitService, mType : String, title : String, mDays : MutableList<String>, alarm : Boolean,
        date : String, time : String, onFeed : Boolean, memo : String, userId : Int) {
        d(TAG, "makeRt 변수들 : $mType, $title, $mDays, $alarm, $time, $onFeed, $memo, $userId")
        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.makeRt(mType, title, days, alarm, date, time, onFeed, memo, userId).enqueue(object : Callback<JsonObject> {
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
    fun updateRt(service : RetrofitService, id : Int, title : String, mDays : MutableList<String>, alarm : Boolean,
        date : String, time : String, onFeed : Boolean, memo : String) {
        d(TAG, "updateRt 변수들 : $id, $title, $mDays, $alarm, $time, $onFeed, $memo")
        val days = mDays.joinToString(separator = " ") // MutableList를 요일 이름만 남긴 하나의 문자열로 바꿔줌
        service.updateRt(id, title, days, alarm, date, time, onFeed, memo).enqueue(object : Callback<JsonObject> {
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

    // 메인 액티비티 '루틴' 탭에서 루틴 및 할 일 목록 불러오기
    fun getRts(service: RetrofitService, userId : Int) {
        d(TAG, "getRts 변수들 : $userId")
        service.getRts(userId).enqueue(object : Callback<MutableList<RtData>> {
            override fun onFailure(call: Call<MutableList<RtData>>, t: Throwable) {
                d(TAG, "루틴(할 일) 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<RtData>>, response: Response<MutableList<RtData>>) {
                d(TAG, "루틴(할 일) 목록 가져오기 요청 응답 수신 성공")
//                d(TAG, "getRts : "+response.body().toString())
                val rtdatas = response.body()
                try {
                    rtTodoViewModel.setList(rtdatas!!)
                } catch (e: IllegalStateException) {
                    d(TAG, "error : $e")
                }
            }
        })
    }

    // '루틴 수정하기' 또는 '할 일 수정하기' 액티비티에 루틴 또는 할 일의 데이터 세팅하기
    fun getRt(service: RetrofitService, rtId : Int) {
        d(TAG, "getRt 변수들 : $rtId")
        service.getRt(rtId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 데이터 가져오기 요청 응답 수신 성공")
                d(TAG, "getRt : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)

                mainTitleInput.setText(gson.get("rtTitle").asString)
                val days = gson.get("mDays").asString.replace(" ", "").toMutableList()
                d(TAG, "days : $days")
//                if(days.size > 0) {
                    for(i in 0 until days.size) {
                        var day = ""
                        d(TAG, "days[i] : "+days[i])
                        when(days[i].toString()) {
                            getString(R.string.sunday) -> day = "sun"
                            getString(R.string.monday) -> day = "mon"
                            getString(R.string.tuesday) -> day = "tue"
                            getString(R.string.wednesday) -> day = "wed"
                            getString(R.string.thursday) -> day = "thu"
                            getString(R.string.friday) -> day = "fri"
                            getString(R.string.saturday) -> day = "sat"
                        }
                        mainDays.check(resources.getIdentifier(day, "id", packageName))
                    }
//                }
                activateAlarm.isChecked = gson.get("alarm").asInt == 1
                startTime.text = gson.get("time").asString
                startTimeResult = gson.get("time").asString
                startTimeResultParsed = LocalTime.parse(startTimeResult)

                if(TAG == "RtMakeActivity" || TAG == "RtUpdateActivity") {
                    rtOnFeed.isChecked = gson.get("onFeed").asInt == 1
                } else if(TAG == "TodoMakeActivity" || TAG == "TodoUpdateActivity") {
                    repeat.isChecked = gson.get("onFeed").asInt == 1

                    val formattedForShow: String = LocalDate.parse(gson.get("date").asString).format(formatterMonthDay)
                    startDate.text = formattedForShow
                    startDateResult = gson.get("date").asString
                    startDateResultParsed = LocalDate.parse(startDateResult)
                }
                memo.setText(gson.get("memo").asString)
            }
        })
    }

    // 루틴(할 일) 삭제하기
    fun deleteRt(service: RetrofitService, rtId : Int, context : Context) {
        d(TAG, "deleteRt 변수들 : $rtId")
        service.deleteRt(rtId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 데이터 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 데이터 삭제 요청 응답 수신 성공")
                d(TAG, "getRt : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        getRts(service, getUserId(context))
                    }
                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 루틴(할 일) 완료 처리하기
    fun doneRt(context : Context, service: RetrofitService, rtId : Int, done : Int, mDays : String, mDateInput : String) {
        d(TAG, "doneRt 변수들 : $rtId, $done, $mDays, $mDateInput")
        var mDate = ""
        if(mDays.isNotBlank() && done == 1) {
            mDate = getMDate(mDays, mDateInput, 1) // DB에 저장될 날짜(내일 날짜부터 시작해서 수행 요일에 맞는 날짜 찾은 후에 이 변수에 넣음)
        } else if(done == 0) {
            mDate = LocalDate.now().toString()
        }

        service.doneRt(rtId, done, mDate).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴(할 일) 수행 처리 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴(할 일) 수행 처리 요청 응답 수신 성공")
                d(TAG, "doneRt : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                getRts(service, getUserId(context))
            }
        })
    }

    // 메인 액티비티 회고 작성 내용 저장하기
    fun setReview(service: RetrofitService, content: String, onPublic: Boolean, mDate: String) {
        d(TAG, "setReview 변수들 : $content, $onPublic, $mDate")
        service.setReview(content, onPublic, mDate, getUserId(applicationContext)).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "회고 저장 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "회고 저장 요청 응답 수신 성공")
                d(TAG, "setReview : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                    }
                    false -> Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 메인 액티비티 회고 작성 내용 불러오기
    fun getReview(service: RetrofitService, mDate: String, review: EditText, onPublicSwitch: SwitchMaterial) {
        d(TAG, "getReview 변수들 : $mDate")
        service.getReview(mDate, getUserId(applicationContext)).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "회고 불러오기 실패 : {$t}")
                review.setText("")
                onPublicSwitch.isChecked = true
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "회고 불러오기 요청 응답 수신 성공")
                d(TAG, "getReview : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val content = gson.get("content").asString // 회고 내용
                val onPublic = gson.get("on_public").asBoolean // 회고 공개 여부

                review.setText(content)
                onPublicSwitch.isChecked = onPublic
            }
        })
    }

    // '행동 추가하기' 액티비티의 하단 버튼 눌렀을 때의 동작(행동 추가하기)
    fun makeAction(service : RetrofitService, title : String, time : String, memo : String, rtId : Int) {
        d(TAG, "makeAction 변수들 : $title, $time, $memo, $rtId")
        service.makeAction(title, time, memo, rtId, getUserId(this)).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "행동 추가하기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "행동 추가하기 요청 응답 수신 성공")
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

    // '행동 수정하기' 액티비티의 하단 버튼 눌렀을 때의 동작(행동 수정하기)
    fun updateAction(service : RetrofitService, id : Int, title : String, time : String, memo : String) {
        d(TAG, "updateAction 변수들 : $title, $time, $memo")
        service.updateAction(id, title, time, memo).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "행동 수정하기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "행동 수정하기 요청 응답 수신 성공")
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

    // 메인에서 루틴 선택 시 해당 루틴 내 행동 목록 불러오기
    fun getActions(service: RetrofitService, rtId : Int, userId : Int) {
        d(TAG, "getActions 변수들 : $rtId, $userId")
        service.getActions(rtId, userId).enqueue(object : Callback<MutableList<ActionData>> {
            override fun onFailure(call: Call<MutableList<ActionData>>, t: Throwable) {
                d(TAG, "루틴(할 일) 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<ActionData>>, response: Response<MutableList<ActionData>>) {
                d(TAG, "루틴(할 일) 목록 가져오기 요청 응답 수신 성공")
//                d(TAG, "getRts : "+response.body().toString())
                val actionDatas = response.body()
                try {
                    actionViewModel.setList(actionDatas!!)
                } catch (e: IllegalStateException) {
                    d(TAG, "error : $e")
                }
            }
        })
    }

    // 루틴 내 행동의 데이터 불러오기
    fun getAction(service: RetrofitService, actionId : Int) {
        d(TAG, "getAction 변수들 : $actionId")
        service.getAction(actionId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴 내 행동 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴 내 행동 데이터 가져오기 요청 응답 수신 성공")
                d(TAG, "getAction : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)

                mainTitleInput.setText(gson.get("title").asString)
                timecost.setText(gson.get("time").asString)
                memo.setText(gson.get("memo").asString)
            }
        })
    }

    // 루틴(할 일) 삭제하기
    fun deleteAction(service: RetrofitService, actionId : Int, rtId : Int, context : Context) {
        d(TAG, "deleteAction 변수들 : $actionId, $rtId")
        service.deleteAction(actionId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴 내 행동 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴 내 행동 삭제 요청 응답 수신 성공")
                d(TAG, "getRt : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        getActions(service, rtId, getUserId(context))
                    }
                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}