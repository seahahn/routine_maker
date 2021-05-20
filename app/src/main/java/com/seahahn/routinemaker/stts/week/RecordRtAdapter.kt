package com.seahahn.routinemaker.stts.week

import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.network.RetrofitService

class RecordRtAdapter : RecyclerView.Adapter<RecordRtViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var allData = mutableListOf<RtData>()
    private var data = mutableListOf<RtData>()
    private var doneCount = 0
    private var actionData = mutableListOf<ActionData>()
    private lateinit var date : String
    private lateinit var dayOfWeek : String

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordRtViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stts_week, parent, false)
        return RecordRtViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: RecordRtViewHolder, position: Int) {
//        d(TAG, "RRA onBindViewHolder")
//        holder.replaceDate(date)
        holder.getRtDatas(allData)
        holder.getActionDatas(actionData)
        holder.onBind(data[position], date)
    }

    override fun getItemCount(): Int = data.size

    fun getItemDoneCount(): Int {
        doneCount = 0
        for(i in 0 until data.size) {
            if(data[i].done == 1) doneCount++
        }
        return doneCount
    }

    fun replaceList(newList: MutableList<RtData>) {
//        d(TAG, "RRA replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun getAllDatas(newList: MutableList<RtData>) {
        allData = newList.toMutableList()
    }

    fun replaceActionList(newList: MutableList<ActionData>) {
//        d(TAG, "rt replaceList")
        actionData = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
    }

    fun returnList(): MutableList<RtData> {
        return data
    }

    fun replaceDate(dateInput : String) {
        date = dateInput
        doneCount = 0 // 날짜 바꾸면 완료한 루틴 수 0으로 초기화하기
    }

    fun setDayOfWeek(dayOfWeekInput: String) {
        dayOfWeek = dayOfWeekInput
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }
}