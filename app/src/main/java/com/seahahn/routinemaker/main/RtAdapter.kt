package com.seahahn.routinemaker.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService

class RtAdapter : RecyclerView.Adapter<RtViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var data = mutableListOf<RtData>()
    private lateinit var date : String
    private lateinit var dayOfWeek : String

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RtViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RtViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: RtViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.getService(service)
        holder.onBind(data[position], date, dayOfWeek)
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<RtData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<RtData> {
        return data
    }

    fun replaceDate(dateInput : String) {
        date = dateInput
    }

    fun setDayOfWeek(dayOfWeekInput: String) {
        dayOfWeek = dayOfWeekInput
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }
}