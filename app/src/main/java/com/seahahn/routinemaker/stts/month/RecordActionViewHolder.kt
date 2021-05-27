package com.seahahn.routinemaker.stts.month

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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class RecordActionViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val recordTitle : TextView = itemView.findViewById(R.id.action_title)
    private val resultImg : ImageView = itemView.findViewById(R.id.action_result)
    private val tvTimecost : TextView = itemView.findViewById(R.id.timecost)
    private val tvDoneCount : TextView = itemView.findViewById(R.id.doneCount)
    private val tvTotalCount : TextView = itemView.findViewById(R.id.totalCount)

    var mDatas = mutableListOf<ActionData>()
    lateinit var it_mDatas : Iterator<ActionData>

    private lateinit var currentMonth: YearMonth
    private val formatterYM : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun onBind(actionData : ActionData, dateInput : String){
        recordTitle.tag = hashMapOf("id" to actionData.id, "rtId" to actionData.rtId, "title" to actionData.actionTitle)
        recordTitle.text = actionData.actionTitle

        setTable(dateInput, actionData)
    }

    // 각 루틴 내 행동 별 수행 기록 테이블을 초기화하는 메소드
    fun setTable(dateInput : String, actionData : ActionData) {
        currentMonth = YearMonth.parse(LocalDate.parse(dateInput).format(formatterYM).toString())

        var totalCount = 0 // 한 달 동안의 루틴 내 행동 수행 가능했던 횟수(완료/미완료 전체)
        var doneCount = 0 // 수행 완료한 횟수
        var timecost = 0 // 총 소요 시간
        // 각 루틴 내 행동에 대하여 사용자가 선택한 기간(월) 동안의 루틴 수행 기록을 가져옴
        it_mDatas = mDatas.iterator()
        while (it_mDatas.hasNext()) {
            val it_mData = it_mDatas.next()
            // 사용자가 선택한 월에 해당하는 데이터만 출력 목록에 포함시킴
            if (currentMonth == YearMonth.parse(LocalDate.parse(it_mData.mDate).format(formatterYM).toString())
                && it_mData.id == actionData.id) {
                totalCount++
                if(it_mData.done == 1) {
                    doneCount++
                    timecost += it_mData.time.toInt() // 수행 완료했던 당시의 루틴 내 행동 소요 시간을 총 소요 시간 값에 더해줌
                }
            }
        }
        tvTotalCount.text = totalCount.toString()
        tvDoneCount.text = doneCount.toString()
        tvTimecost.text = timecost.toString()

        // 루틴 내 행동의 월간 수행 결과를 보여주는 이미지 넣기
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
