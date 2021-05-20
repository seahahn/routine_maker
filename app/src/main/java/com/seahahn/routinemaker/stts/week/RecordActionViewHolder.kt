package com.seahahn.routinemaker.stts.week

import android.content.Context
import android.util.Log.d
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.network.RetrofitService
import org.jetbrains.anko.imageResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class RecordActionViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val recordTitle : TextView = itemView.findViewById(R.id.action_title)
    private val resultImg : ImageView = itemView.findViewById(R.id.action_result)

    // 주간 루틴 내 행동 수행 내역을 표시하기 위한 날짜 및 수행 여부 표시 이미지들
    var sun : TextView = itemView.findViewById(R.id.sun)
    var mon : TextView = itemView.findViewById(R.id.mon)
    var tue : TextView = itemView.findViewById(R.id.tue)
    var wed : TextView = itemView.findViewById(R.id.wed)
    var thu : TextView = itemView.findViewById(R.id.thu)
    var fri : TextView = itemView.findViewById(R.id.fri)
    var sat : TextView = itemView.findViewById(R.id.sat)
    var dayOfWeekList = mutableListOf(sun, mon, tue, wed, thu, fri, sat)

    var sunDone : ImageView = itemView.findViewById(R.id.sunDone)
    var monDone : ImageView = itemView.findViewById(R.id.monDone)
    var tueDone : ImageView = itemView.findViewById(R.id.tueDone)
    var wedDone : ImageView = itemView.findViewById(R.id.wedDone)
    var thuDone : ImageView = itemView.findViewById(R.id.thuDone)
    var friDone : ImageView = itemView.findViewById(R.id.friDone)
    var satDone : ImageView = itemView.findViewById(R.id.satDone)
    var dayOfWeekDoneList = mutableListOf(sunDone, monDone, tueDone, wedDone, thuDone, friDone, satDone)

    var mDatas = mutableListOf<ActionData>()
    lateinit var it_mDatas : Iterator<ActionData>

    lateinit var parsedDate : LocalDate
    lateinit var firstDayOfWeek : LocalDate
    val formatterWeek : DateTimeFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())

    init {

    }

    fun onBind(actionData : ActionData, dateInput : String){
        recordTitle.tag = hashMapOf("id" to actionData.id, "rtId" to actionData.rtId, "title" to actionData.actionTitle)
        recordTitle.text = actionData.actionTitle

        setTable(dateInput, actionData.id)
    }

    // 각 루틴 내 행동 별 수행 기록 테이블을 초기화하는 메소드
    fun setTable(dateInput : String, actionId : Int) {
        parsedDate = LocalDate.parse(dateInput) // 사용자가 선택한 날짜 값을 가져옴
        firstDayOfWeek = parsedDate.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1)
        var totalCount = 0
        var doneCount = 0
        for(i in 0 until dayOfWeekList.size) {
            // 첫 행에 날짜 넣기
            dayOfWeekList[i].text = firstDayOfWeek.format(formatterWeek)

            // 두 번째 행에 루틴 내 행동 수행 여부 표시하는 이미지 넣기
//            d(TAG, "mDatas : $mDatas")
            it_mDatas = mDatas.iterator()
            while (it_mDatas.hasNext()) {
                val it_mData = it_mDatas.next()
                // 사용자가 선택한 날짜에 해당하는 데이터만 출력 목록에 포함시킴
                if (firstDayOfWeek.isEqual(LocalDate.parse(it_mData.mDate))
                    && it_mData.id == actionId) {
                    totalCount++
                    when(it_mData.done) {
                        0 -> {
                            dayOfWeekDoneList[i].imageResource = R.drawable.stts_red
                        }
                        1 -> {
                            dayOfWeekDoneList[i].imageResource = R.drawable.stts_green
                            doneCount++
                        }
                    }
                }
            }
            firstDayOfWeek = firstDayOfWeek.plusDays(1)
        }
        // 루틴 내 행동의 주간 수행 결과를 보여주는 이미지 넣기
//        d(TAG, "result : $totalCount / $doneCount")
        if(totalCount == doneCount && doneCount > 0) {
            resultImg.setImageResource(R.drawable.stts_green)
        } else if(totalCount != doneCount && doneCount > 0) {
//            d(TAG, "yellow")
            resultImg.setImageResource(R.drawable.stts_yellow)
        } else if(totalCount != 0 && doneCount == 0) {
//            d(TAG, "red")
            resultImg.setImageResource(R.drawable.stts_red)
        }
    }

    // 사용자의 과거 루틴 내 행동 수행 내역을 가져오기 위한 메소드
    fun getActionDatas(newList: MutableList<ActionData>) {
        mDatas = newList
    }
}
