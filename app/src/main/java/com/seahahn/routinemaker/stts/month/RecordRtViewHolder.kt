package com.seahahn.routinemaker.stts.month

import android.content.Context
import android.util.Log.d
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.Stts
import org.jetbrains.anko.runOnUiThread
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class RecordRtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val recordTitle : TextView = itemView.findViewById(R.id.record_title)
    private val arrow : ImageView = itemView.findViewById(R.id.arrow)
    private val resultImg : ImageView = itemView.findViewById(R.id.result_img)
    private val divider : View = itemView.findViewById(R.id.divider)
    private val recordActionList : RecyclerView = itemView.findViewById(R.id.record_action_list)

    private var totalDoneCount = 0
    private var noneCount = 0
    private var notEnough = false

    private val viewEmptyList : LinearLayout = itemView.findViewById(R.id.view_empty_list)

    private lateinit var recordActionAdapter : RecordActionAdapter
    var rtDatas = mutableListOf<RtData>()
    var mDatas = mutableListOf<ActionData>()
    var showDatas = mutableListOf<ActionData>()
    lateinit var it_rtDatas : Iterator<RtData>
    lateinit var it_mDatas : Iterator<ActionData>

    private lateinit var currentMonth: YearMonth
    lateinit var parsedDate : LocalDate
    private val formatterYM : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    private var isOpened = false

    init {
        item.setOnClickListener(ItemClickListener()) // 아이템을 누르면 루틴 내 행동 목록을 열거나 닫음
    }

    fun onBind(rtData : RtData, dateInput : String){
        isOpened = false
        viewEmptyList.visibility = View.GONE

        totalDoneCount = 0
        noneCount = 0
        notEnough = false

        recordTitle.text = rtData.rtTitle // 루틴 제목 넣기

        arrow.setImageResource(R.drawable.record_arrow_down) // 루틴 제목 우측의 화살표. 루틴 내 행동 목록이 열려있으면 상향, 닫혀있으면 하향
        divider.isVisible = isOpened // 루틴 제목이 있는 영역과 루틴 내 행동 목록 영역을 구분해주는 가로선. 행동 목록이 닫혀있을 때는 안 보임
        recordActionList.visibility = View.GONE // 루틴 내 행동 목록은 처음에는 안 보이고, 사용자가 루틴 제목을 누르면 보임

        recordActionAdapter = RecordActionAdapter() // 어댑터 초기화
        recordActionList.adapter = recordActionAdapter // 어댑터 연결

        setShowDatas(dateInput, rtData.id) // 해당 루틴 제목을 클릭할 시 그에 맞는 루틴 내 행동 데이터를 보여주기 위한 데이터 세팅
    }

    // 루틴 수행 결과에 따라 아이템 우측에 표시할 아이콘 모양을 다르게 출력함
    fun setDoneResultIcon() {
        when {
            noneCount == currentMonth.lengthOfMonth() -> {
                // 루틴 내 행동이 없는 경우
                resultImg.setImageResource(R.drawable.horizontal_line)
            }
            totalDoneCount == 0 -> {
                // 루틴 한 번도 제대로 수행하지 않은 경우
                resultImg.setImageResource(R.drawable.stts_red)
                // 일부 수행한 경우 있으면 노란색 표시
                if(notEnough) {
//                    d(TAG, "notEnough yellow?")
                    resultImg.setImageResource(R.drawable.stts_yellow)
                }
            }
            totalDoneCount in 1..currentMonth.lengthOfMonth() -> {
                // 1번 이상 수행했으나 전부 다 수행하진 않은 경우
//                d(TAG, "yellow?")
                resultImg.setImageResource(R.drawable.stts_yellow)
            }
            totalDoneCount == currentMonth.lengthOfMonth() -> {
                // 전부 다 수행한 경우
                resultImg.setImageResource(R.drawable.stts_green)
            }
        }

        // 수행 기록 결과를 보여주는 이미지가 출력되면 로딩된 데이터 수를 추가함
       (context as Stts).loadedDataCount++
        d(TAG, "데이터 로딩 체크 : ${context.needToBeLoadedDataCount}, ${context.loadedDataCount}")
        // 로딩되어야 할 데이터와 로딩된 데이터 수가 같으면 프로그레스바를 숨김
        if(context.needToBeLoadedDataCount == context.loadedDataCount) {
            context.showProgress(false)
        }
    }

    // 날짜에 맞는 데이터만 골라서 보여주기 위한 메소드
    fun setShowDatas(dateInput : String, rtId : Int) {
        parsedDate = LocalDate.parse(dateInput) // 사용자가 선택한 날짜 값을 가져옴
        recordActionAdapter.replaceDate(dateInput)
        currentMonth = YearMonth.parse(LocalDate.parse(dateInput).format(formatterYM).toString())
        showDatas.clear() // 기존 목록을 비움

        if(mDatas.isNotEmpty()) object : Thread() {
            override fun run() {
                setTable(rtId)
                setActionList(rtId)
            }
        }.start()
    }

    // 각 루틴 별 수행 기록 테이블을 초기화하는 메소드
    fun setTable(rtId : Int) {
        totalDoneCount = 0
        noneCount = 0

        var totalCount: Int
        var doneCount: Int

//        if(mDatas.isNotEmpty()) object : Thread() {
//            override fun run() {
                d(TAG, "setTable start")
                for(i in 0 until currentMonth.lengthOfMonth()) {
                    totalCount = 0
                    doneCount = 0

                    it_mDatas = mDatas.iterator()
                    while (it_mDatas.hasNext()) {
                        val it_mData = it_mDatas.next()
                        // 사용자가 선택한 날짜에 해당하는 데이터만 출력 목록에 포함시킴
                        if (currentMonth == YearMonth.parse(LocalDate.parse(it_mData.mDate).format(formatterYM).toString())
                            && it_mData.rtId == rtId) {
                            totalCount++ // 해당 날짜의 루틴 내 행동 수
                            if(it_mData.done == 1) doneCount++ // 해당 날짜의 수행 완료 루틴 내 행동 수
                        }
                    }
                    if(totalCount == doneCount && doneCount > 0) {
                        totalDoneCount++
                    } else if(totalCount != doneCount && doneCount > 0) {
        //                d(TAG, "notEnough?")
                        notEnough = true
                    } else if(totalCount != 0 && doneCount == 0) {
                    } else {
                        noneCount++
                    }
                }
//            }
//        }.start()
    }

    // 각 루틴 별 루틴 내 행동 수행 기록 테이블 목록을 초기화하는 메소드
    fun setActionList(rtId : Int) {
        // 루틴 내 행동 목록 표시하기 위해서 데이터 세팅하기
//        d(TAG, "firstDayOfWeek : $firstDayOfWeek")
//        d(TAG, "mDatas : $mDatas")
//        if(mDatas.isNotEmpty()) object : Thread() {
//            override fun run() {
                for(i in 0 until currentMonth.lengthOfMonth()) {
                    it_mDatas = mDatas.iterator()
                    while (it_mDatas.hasNext()) {
                        val it_mData = it_mDatas.next()
                        if (currentMonth == YearMonth.parse(LocalDate.parse(it_mData.mDate).format(formatterYM).toString())
                            && it_mData.rtId == rtId) { // 날짜 및 선택된 루틴에 해당하는 루틴 내 행동 데이터만 출력 목록에 포함시킴
                            var check: Boolean // 루틴 내 행동 수행 기록 목록에 들어갔는지 아닌지 여부 확인하기
                            var result = false
                            for(j in 0 until showDatas.size) {
                                check = showDatas[j].id == it_mData.id
                                if(check) result = check
                            }
                            if(!result || showDatas.size == 0) showDatas.add(it_mData) // 아이템이 없거나 루틴 내 행동 고유 번호 기준으로 중복되면 포함시키지 않음
                        }
                    }
                }

                recordActionAdapter.getAllDatas(mDatas) // 전체 루틴 내 행동 목록을 보냄
                context.runOnUiThread {
                    recordActionAdapter.replaceList(showDatas) // 추려낸 목록을 어댑터에 보냄
                    setDoneResultIcon()
                }
//            }
//        }.start()
    }

    // 사용자의 모든 과거 루틴 수행 내역을 가져오기 위한 메소드
    fun getRtDatas(newList: MutableList<RtData>) {
        rtDatas = newList
    }

    // 사용자의 과거 루틴 내 행동 수행 내역을 가져오기 위한 메소드
    fun getActionDatas(newList: MutableList<ActionData>) {
        mDatas = newList
    }

    // 아이템 클릭 시 동작할 내용
    // 누를 때마다 루틴 내 행동을 열거나 닫음
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            isOpened = !isOpened
            if(isOpened) {
                arrow.setImageResource(R.drawable.record_arrow_up)
                divider.isVisible = isOpened
                recordActionList.visibility = View.VISIBLE
                // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
                if(recordActionAdapter.itemCount == 0) {
                    viewEmptyList.visibility = View.VISIBLE
                }
            } else {
                arrow.setImageResource(R.drawable.record_arrow_down)
                divider.isVisible = isOpened
                recordActionList.visibility = View.GONE
                viewEmptyList.visibility = View.GONE
            }
        }
    }
}