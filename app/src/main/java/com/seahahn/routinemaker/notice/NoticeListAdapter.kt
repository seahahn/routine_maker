package com.seahahn.routinemaker.notice

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
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
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedDetailActivity
import com.seahahn.routinemaker.util.FCMNotiType
import com.seahahn.routinemaker.util.Sns
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeListAdapter(mContext : Context) : RecyclerView.Adapter<NoticeListAdapter.NoticeListViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService
    val context : Context = mContext

    //데이터들을 저장하는 변수
    private var data = mutableListOf<NoticeData>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeListAdapter.NoticeListViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeListViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: NoticeListAdapter.NoticeListViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<NoticeData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<NoticeData> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }


    inner class NoticeListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = this::class.java.simpleName

        private val context = itemView.context

        private val item : ConstraintLayout = itemView.findViewById(R.id.item)
        private val img : ImageView = itemView.findViewById(R.id.img)
        private val content : TextView = itemView.findViewById(R.id.content)

        init {
            item.setOnClickListener(ItemClickListener())
            img.setOnClickListener(Sns.ProfileClickListener())
        }

        fun onBind(noticeData : NoticeData) {
            getUserData(noticeData.senderId, noticeData.type, noticeData.title, noticeData.body)

            item.tag = hashMapOf("feedId" to noticeData.target)
        }

        // 피드 작성자 프로필 사진, 닉네임 표시하기
        fun getUserData(senderId : Int, type: Int, title: String, body: String) {
            Logger.d(TAG, "getUserData 변수들 : $senderId")
            service.getUserData(senderId).enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
                    Logger.d(TAG, "사용자 데이터 : ${response.body().toString()}")
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    val id = gson.get("id").asString.toInt()
                    val nickname = gson.get("nick").asString
                    val photo = gson.get("photo").asString

                    // 프로필 사진에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
                    img.tag = hashMapOf("id" to id, "nick" to nickname)

                    Glide.with(context).load(photo)
                        .placeholder(R.drawable.warning)
                        .error(R.drawable.warning)
                        .into(img)

//                    val body = Gson().fromJson(body, JsonObject::class.java)
//                    val feedContent = body.get("content").asString
                    // 알림 타입에 따라 보여줄 텍스트를 다르게 설정
                    val text : String
                    when(type) {
                        FCMNotiType.CHAT.type() -> {}
                        FCMNotiType.CMT.type() -> {
                            val body = Gson().fromJson(body, JsonObject::class.java)
                            val notiContent = body.get("content").asString
                            text = nickname.plus(context.getString(R.string.notiCmt) +" "+ notiContent)
                            content.text = setNicknameTextBold(text, nickname)
                        }
                        FCMNotiType.SUB_CMT.type() -> {
                            val body = Gson().fromJson(body, JsonObject::class.java)
                            val notiContent = body.get("content").asString
                            text = nickname.plus(context.getString(R.string.notiSubCmt) +" "+ notiContent)
                            content.text = setNicknameTextBold(text, nickname)
                        }
                        FCMNotiType.LIKE.type() -> {
                            val body = Gson().fromJson(body, JsonObject::class.java)
                            val notiContent = body.get("content").asString
                            text = nickname.plus(context.getString(R.string.notiLike) +" "+ notiContent)
                            content.text = setNicknameTextBold(text, nickname)
                        }
                        FCMNotiType.RT.type() -> {
                            text = title.plus(" "+context.getString(R.string.notiRtTodo))
                            content.text = setNicknameTextBold(text, nickname)
                        }
                    }
                }
            })
        }

        // 알림을 발생시킨 사용자의 닉네임 글자만 굵게 만들어줌
        fun setNicknameTextBold(text : String, nickname : String) : SpannableString {
            val spannableString = SpannableString(text)
            val start: Int = text.indexOf(nickname)
            val end: Int = start + nickname.length
            spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableString
        }

        // 아이템 클릭 시 동작할 내용
        inner class ItemClickListener() : View.OnClickListener {
            override fun onClick(v: View?) {
                val feedId = ((v!!.tag as HashMap<*, *>)["feedId"]).toString().toInt()
                val it = Intent(context, GroupFeedDetailActivity::class.java)
                it.putExtra("feedId", feedId)
                context.startActivity(it)
            }
        }
    }
}