package com.seahahn.routinemaker.stts

import android.content.Context
import android.util.Log.d
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.network.RetrofitService
import java.time.LocalDate

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

    private val viewEmptyList : LinearLayout = itemView.findViewById(R.id.view_empty_list)

    private lateinit var recordActionAdapter : RecordActionAdapter
    var mDatas = mutableListOf<ActionData>()
    var showDatas = mutableListOf<ActionData>()
    lateinit var it_mDatas : Iterator<ActionData>

    lateinit var parsedDate : LocalDate

    private var isOpened = false

    init {
        item.setOnClickListener(ItemClickListener()) // 아이템을 누르면 루틴 내 행동 목록을 열거나 닫음
    }

    fun onBind(rtData : RtData, dateInput : String){
        recordTitle.text = rtData.rtTitle // 루틴 제목 넣기

        arrow.setImageResource(R.drawable.record_arrow_down) // 루틴 제목 우측의 화살표. 루틴 내 행동 목록이 열려있으면 상향, 닫혀있으면 하향
        divider.isVisible = isOpened // 루틴 제목이 있는 영역과 루틴 내 행동 목록 영역을 구분해주는 가로선. 행동 목록이 닫혀있을 때는 안 보임
        recordActionList.visibility = View.GONE // 루틴 내 행동 목록은 처음에는 안 보이고, 사용자가 루틴 제목을 누르면 보임

        recordActionAdapter = RecordActionAdapter() // 어댑터 초기화
        recordActionList.adapter = recordActionAdapter // 어댑터 연결

        setShowDatas(dateInput, rtData.id) // 해당 루틴 제목을 클릭할 시 그에 맞는 루틴 내 행동 데이터를 보여주기 위한 데이터 세팅

        // 루틴 수행 결과에 따라 아이템 우측에 표시할 아이콘 모양을 다르게 출력함
        when(rtData.done) {
            // 루틴 미완료 시 2가지 경우로 나뉨
            0 -> {
                // 루틴 내 행동 아무것도 수행하지 않은 경우
                resultImg.setImageResource(R.drawable.stts_red)
                // 루틴 내 행동 1개 이상 수행했으나 전부 다 수행하진 않은 경우
                if(recordActionAdapter.isAllDone() && recordActionAdapter.itemCount != 0) {
                    resultImg.setImageResource(R.drawable.stts_yellow)
                } else if(recordActionAdapter.itemCount == 0) {
                    resultImg.setImageResource(R.drawable.horizontal_line)
                }
            }
            // 루틴 완료했으면 녹색 원으로 표시
            1 -> resultImg.setImageResource(R.drawable.stts_green)
        }
    }

    // 날짜에 맞는 데이터만 골라서 보여주기 위한 메소드
    fun setShowDatas(dateInput : String, rtId : Int) {
        parsedDate = LocalDate.parse(dateInput) // 사용자가 선택한 날짜 값을 가져옴
        showDatas.clear() // 기존 목록을 비움
        it_mDatas = mDatas.iterator()
        while (it_mDatas.hasNext()) {
            val it_mData = it_mDatas.next()
            if (parsedDate.isEqual(LocalDate.parse(it_mData.mDate))
                && it_mData.rtId == rtId) { // 날짜 및 선택된 루틴에 해당하는 루틴 내 행동 데이터만 출력 목록에 포함시킴
                showDatas.add(it_mData)
            }
        }
        recordActionAdapter.replaceList(showDatas) // 추려낸 목록을 어댑터에 보냄
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