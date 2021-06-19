package com.seahahn.routinemaker.sns.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ChatContentsAdapter : RecyclerView.Adapter<ChatContentsAdapter.ChatContentViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService
    lateinit var context : Context

    //데이터들을 저장하는 변수
    private var data = mutableListOf<ChatMsg>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatContentViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        lateinit var view : View
        when(viewType) {
            0 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_mine, parent, false) // 사용자 본인이 보냄
            1 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_other, parent, false) // 타인이 보냄
            2 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_mine_img, parent, false) // 사용자 본인이 보낸 이미지
            3 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_other_img, parent, false) // 타인이 보낸 이미지
        }
        return ChatContentViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return when(data[position].contentType) {
            0 -> if(data[position].writerId == getUserId(context)) { 0 } else { 1 }
            1 -> if(data[position].writerId == getUserId(context)) { 2 } else { 3 }
            else -> 1
        }
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: ChatContentViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<ChatMsg>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<ChatMsg> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }

    fun getContext(contextInput : Context) {
        context = contextInput
    }


    inner class ChatContentViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = this::class.java.simpleName
        val context: Context = itemView.context
//        lateinit var serviceInViewHolder : RetrofitService

        private val formatterHM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        private val item : ConstraintLayout = itemView.findViewById(R.id.item)
        private val profilePic : ImageView = itemView.findViewById(R.id.profile_pic)
        private val nick : TextView = itemView.findViewById(R.id.nick)
        private val createdAt : TextView = itemView.findViewById(R.id.createdAt)
        private lateinit var content : TextView
        private lateinit var contentImg : ImageView

        fun onBind(chatMsg : ChatMsg){
            // 채팅 메시지 타입 구분하기
            if(chatMsg.contentType == 0) {
                content = itemView.findViewById(R.id.content) // 텍스트
                content.text = chatMsg.content
            } else {
                contentImg = itemView.findViewById(R.id.content) // 이미지
                Glide.with(context).load(chatMsg.content)
                    .placeholder(R.drawable.warning)
                    .error(R.drawable.warning)
                    .into(contentImg)
            }

            // 댓글 작성자 프로필 사진, 닉네임 및 작성일자 표시하기
            getUserData(chatMsg.writerId)
            val dateTime = LocalDateTime.parse(chatMsg.createdAt.replace(" ", "T"))
            createdAt.text = dateTime.format(formatterHM)
        }

        // 피드 작성자 프로필 사진, 닉네임 표시하기
        fun getUserData(writerId : Int) {
            Logger.d(TAG, "getUserData 변수들 : $writerId")
            service.getUserData(writerId).enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
                    Logger.d(TAG, "사용자 데이터 : ${response.body().toString()}")
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    val nickname = gson.get("nick").asString
                    val photo = gson.get("photo").asString

                    Glide.with(context).load(photo)
                        .placeholder(R.drawable.warning)
                        .error(R.drawable.warning)
                        .into(profilePic)
                    nick.text = nickname
                }
            })
        }
    }

}