package com.seahahn.routinemaker.sns.group

import android.os.Parcel
import android.os.Parcelable
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.AppVar
import com.seahahn.routinemaker.util.AppVar.setAcceptedList
import java.util.HashMap

class GroupApplicantListAdapter() : RecyclerView.Adapter<GroupApplicantListViewHolder>(),
    CompoundButton.OnCheckedChangeListener {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var data = mutableListOf<GroupMemberData>()

    // 아이템 전체 체크박스 체크 여부를 정하는 변수
    private var checkAll = false
    private lateinit var checkAllCheckbox: MaterialCheckBox
    private var checkboxList = mutableListOf<MaterialCheckBox>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupApplicantListViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_applicant, parent, false)
        return GroupApplicantListViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: GroupApplicantListViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.getService(service)
        holder.onBind(data[position])
        checkboxList.add(holder.checkbox)
        holder.checkbox.setOnCheckedChangeListener(this)
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

    // 전체 선택하기 체크박스 상태에 맞춰 아이템 전체 체크를 하거나 해제함
    fun checkAll(input : Boolean) {
        checkAll = input
        for(i in 0 until checkboxList.size) {
            checkboxList[i].isChecked = checkAll
        }
    }

    // 그룹 가입 신청자 목록 액티비티에 있는 전체 선택하기 체크박스 객체를 가져옴
    fun setCheckAllCheckbox(checkbox : MaterialCheckBox) {
        checkAllCheckbox = checkbox
    }

    // 모든 아이템이 체크되어 있는지 여부를 감지하여 이에 맞춰 전체 선택하기 체크박스의 체크 상태를 변경함
    private fun isAllItemChecked(input : Boolean) {
        checkAllCheckbox.isChecked = input
    }

    // 개별 아이템 체크박스의 체크 상태에 따라 동작할 내용
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val applicantId = ((buttonView!!.tag as HashMap<*, *>)["id"]).toString().toInt() // 가입 신청자의 고유 번호
        // 체크되어 있으면 승인 대상 목록에 추가하고 아니면 제외
        if(isChecked) {
            GroupApplicantListViewHolder.acceptedList.add(applicantId)
        } else {
            GroupApplicantListViewHolder.acceptedList.remove(applicantId)
        }
        setAcceptedList(buttonView.context, GroupApplicantListViewHolder.acceptedList) // 승인 대상자 목록을 SharedPreferences를 이용하여 임시 저장
        isAllItemChecked(checkboxList.size == GroupApplicantListViewHolder.acceptedList.size) // 각 아이템 체크 여부 감지할 때마다 전체 아이템이 선택되어 있는지 아닌지 확인 후 전체 선택하기 체크박스의 체크 여부를 결정
    }

}