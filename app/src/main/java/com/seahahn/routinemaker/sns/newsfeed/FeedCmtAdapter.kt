package com.seahahn.routinemaker.sns.newsfeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.sns.FeedData

class FeedCmtAdapter : RecyclerView.Adapter<FeedCmtViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var data = mutableListOf<CmtData>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedCmtViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        lateinit var view : View
        when(viewType) {
            0 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_main_cmt, parent, false)
            1 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_sub_cmt, parent, false)
        }
        return FeedCmtViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if(data[position].isSub) {
            1
        } else {
            0
        }
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: FeedCmtViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.getService(service)
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<CmtData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<CmtData> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }
}