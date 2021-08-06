package com.seahahn.routinemaker.sns.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupMemberData

class GroupNextLeaderAdapter : RecyclerView.Adapter<GroupNextLeaderViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var data = mutableListOf<GroupMemberData>()

    private var radioBtnGroup = mutableListOf<RadioButton>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupNextLeaderViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member_radio, parent, false)
        return GroupNextLeaderViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: GroupNextLeaderViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.getService(service)
        holder.onBind(data[position])

        // 라디오버튼 눌렀을 때 이전에 선택했던 버튼은 선택 해제시키기 위해서 라디오버튼 그룹으로 묶어줌
        holder.radioBtn.tag = position
        radioBtnGroup.add(holder.radioBtn)
        holder.radioBtn.setOnClickListener(RadioBtnClickListener())
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<GroupMemberData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<GroupMemberData> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }

    // 라디오버튼 하나를 누르면 나머지는 선택 해제되는 방식
    inner class RadioBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val radioBtn = v as RadioButton
            for(i in 0 until radioBtnGroup.size) {
                radioBtnGroup[i].isChecked = radioBtn.tag == radioBtnGroup[i].tag
            }
        }

    }

}