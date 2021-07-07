package com.seahahn.routinemaker.sns.group

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.Sns

class GroupMemberListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : LinearLayout = itemView.findViewById(R.id.item)
    private val img : ImageView = itemView.findViewById(R.id.img)
    private val nick : TextView = itemView.findViewById(R.id.nick)

    init {
        item.setOnClickListener(Sns.ProfileClickListener())
    }

    fun onBind(groupMemberData : GroupMemberData){
        // 그룹 멤버 프로필 사진 표시하기
        Glide.with(context).load(groupMemberData.photo)
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(img)

        // 그룹 멤버 닉네임 표시하기
        nick.text = groupMemberData.nick

        // 프로필 사진 및 닉네임에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
        item.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }
}
