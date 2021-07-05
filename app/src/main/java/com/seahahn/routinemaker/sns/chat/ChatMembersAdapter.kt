package com.seahahn.routinemaker.sns.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChatUserData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ChatMembersAdapter(mContext : Context) : RecyclerView.Adapter<ChatMembersAdapter.ChatMemberViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService
    val context : Context = mContext

    //데이터들을 저장하는 변수
    private var data = mutableListOf<GroupMemberData>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMembersAdapter.ChatMemberViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member, parent, false)
        return ChatMemberViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: ChatMembersAdapter.ChatMemberViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.onBind(data[position])
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


    inner class ChatMemberViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = this::class.java.simpleName

        private val item : LinearLayout = itemView.findViewById(R.id.item)
        private val img : ImageView = itemView.findViewById(R.id.img)
        private val nick : TextView = itemView.findViewById(R.id.nick)

        init {
            img.setOnClickListener(Sns.ProfileClickListener())
            nick.setOnClickListener(Sns.ProfileClickListener())
        }

        fun onBind(groupMemberData : GroupMemberData) {
            Glide.with(context).load(groupMemberData.photo)
                .placeholder(R.drawable.warning)
                .error(R.drawable.warning)
                .into(img)
            nick.text = groupMemberData.nick

            // 프로필 사진 및 닉네임에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
            img.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
            nick.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
        }
    }
}