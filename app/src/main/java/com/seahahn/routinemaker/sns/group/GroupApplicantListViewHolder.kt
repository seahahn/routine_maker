package com.seahahn.routinemaker.sns.group

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.AppVar.setAcceptedList
import com.seahahn.routinemaker.util.Sns

class GroupApplicantListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    val contextSns: Sns = Sns()
    lateinit var serviceInViewHolder : RetrofitService

//    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    val checkbox : MaterialCheckBox = itemView.findViewById(R.id.checkbox)
    private val img : ImageView = itemView.findViewById(R.id.img)
    private val nick : TextView = itemView.findViewById(R.id.nick)

    companion object {
        var acceptedList = mutableListOf<Int>()
    }

    init {
//        d(TAG, "RtViewHolder init")
        acceptedList.removeAll(acceptedList) // 액티비티 실행 시 기존에 들어가 있던 가입 승인 대상자 목록 초기화하기(안하면 이전 데이터가 남아 있는 경우 존재)
//        checkbox.setOnCheckedChangeListener(ItemCheckedChangeListener())
        img.setOnClickListener(Sns.ProfileClickListener())
        nick.setOnClickListener(Sns.ProfileClickListener())
    }

    fun onBind(groupMemberData : GroupMemberData){
        // 라디오버튼 태그에 그룹 멤버의 고유 번호를 담아둠
        checkbox.tag = hashMapOf("id" to groupMemberData.id)

        // 그룹 멤버 프로필 사진 표시하기
        Glide.with(context).load(groupMemberData.photo)
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(img)

        // 그룹 멤버 닉네임 표시하기
        nick.text = groupMemberData.nick

        // 프로필 사진 및 닉네임에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
        img.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
        nick.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

    // 체크박스 클릭 시 동작할 내용
    inner class ItemCheckedChangeListener : Sns(), CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val applicantId = ((buttonView!!.tag as HashMap<*, *>)["id"]).toString().toInt()
            if(isChecked) {
                acceptedList.add(applicantId)
            } else {
                acceptedList.remove(applicantId)
            }
            setAcceptedList(context, acceptedList)
            d(TAG, "acceptedList = $acceptedList")
        }
    }
}
