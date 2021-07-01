package com.seahahn.routinemaker.sns.chat

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChatUserData
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.SnsChat
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserNick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Predicate


class ChatroomsAdapter(mContext : Context) : RecyclerView.Adapter<ChatroomsAdapter.ChatRoomViewHolder>(), Filterable {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService
    val context = mContext as Sns

    val formatterYMDHM: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // 문자열 형식(시 분)

    val chatDB by lazy { ChatDataBase.getInstance(context) } // 채팅 내용 저장해둔 Room DB 객체 가져오기

    //데이터들을 저장하는 변수
    private var originalData = mutableListOf<ChatRoom>()
    private var data = mutableListOf<ChatRoom>()
    var filteredData = mutableListOf<ChatRoom>()
    var resultData = mutableListOf<ChatRoom>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        lateinit var view : View
        when(viewType) {
            0 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list, parent, false) // 1:1 채팅인 경우
            1 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list_group, parent, false) // 그룹 채팅인 경우
        }
        return ChatRoomViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {

        return when(data[position].isGroupchat) {
            true -> 1
            false -> 0
        }
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<ChatRoom>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun saveOriginalList(list: MutableList<ChatRoom>) {
        originalData = list.toMutableList()
    }

    fun returnList(): MutableList<ChatRoom> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                filteredData.clear()
                if (constraint?.isEmpty() == true) {
                    filteredData.addAll(originalData)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (item in originalData) {
                        if (item.title.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filteredData.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                resultData.clear()
                resultData.addAll(results!!.values as Collection<ChatRoom>)
                replaceList(resultData)

                // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
                Logger.d(TAG, "groupListAdapter.itemCount : $itemCount")
                if (itemCount == 0) {
                    context.viewEmptyList.visibility = View.VISIBLE
                } else {
                    context.viewEmptyList.visibility = View.GONE
                }
            }
        }
    }

    inner class ChatRoomViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = this::class.java.simpleName
//        val context: Context = itemView.context
//        lateinit var serviceInViewHolder : RetrofitService

        private val formatterHM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        private val item : ConstraintLayout = itemView.findViewById(R.id.item)
        private val backframe : FrameLayout = itemView.findViewById(R.id.backframe)
        private val chatOut : TextView = itemView.findViewById(R.id.chatOut)
        private lateinit var profilePic : ImageView
        private lateinit var profilePicGroup : ConstraintLayout
        private val chatTitle : TextView = itemView.findViewById(R.id.chatTitle)
        private val lastMsg : TextView = itemView.findViewById(R.id.lastMsg)
        private val lastMsgTime : TextView = itemView.findViewById(R.id.lastMsgTime)
        private val msgBadge : TextView = itemView.findViewById(R.id.msgBadge)

        private lateinit var profilePicLeader : ImageView
        private lateinit var profilePicSecond : ImageView
        private lateinit var profilePicThird : ImageView
        private lateinit var profilePicFourth : ImageView
        private lateinit var ivList : MutableList<ImageView>

        private lateinit var itemTag : HashMap<String, Any>
        private lateinit var title : String
        private var mRoomId : Int = 0

        init {
            item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        }

        fun onBind(chatRoom : ChatRoom) {
            itemTag = hashMapOf()
            mRoomId = chatRoom.id
            // 채팅방 타입 구분하기
            if(!chatRoom.isGroupchat) {
                // 1:1 채팅인 경우 상대방 프로필 사진 넣기
                // 채팅 제목은 상대방 닉네임
                profilePic = itemView.findViewById(R.id.profile_pic)
                if(chatRoom.hostId == getUserId(context)) {
                    getUserData(chatRoom.audienceId, chatTitle, profilePic)
                } else {
                    getUserData(chatRoom.hostId, chatTitle, profilePic)
                }
            } else {
                // 그룹 채팅인 경우 첫번째에는 그룹 리더, 그 다음으로는 들어온지 오래 된 사람 순으로 프로필 사진 넣기
                // 최대 4명의 사진 넣음
                // 1명일 경우(그룹 리더만 있는 경우)에는 사진 자리에 꽉 차게 넣음
                // 채팅 제목은 그룹명
                profilePicGroup = itemView.findViewById(R.id.profile_pic)
                profilePicLeader = itemView.findViewById(R.id.profile_pic_leader)
                profilePicSecond = itemView.findViewById(R.id.profile_pic_second)
                profilePicThird = itemView.findViewById(R.id.profile_pic_third)
                profilePicFourth = itemView.findViewById(R.id.profile_pic_fourth)
                ivList = mutableListOf(profilePicLeader, profilePicSecond, profilePicThird, profilePicFourth)
                getChatUsers(chatRoom.id, chatRoom.hostId)
                getGroup(chatRoom.audienceId, chatTitle)
            }

            chatDB!!.chatDao().getLastChatMsg(chatRoom.id).observe(context as LifecycleOwner) { chatMsgs ->
                if(chatMsgs != null) {
                    if(chatMsgs.contentType == 0) {
                        lastMsg.text = chatMsgs.content // 텍스트인 경우
                    } else if(chatMsgs.contentType == 1){
                        lastMsg.text = context.getString(R.string.pic) // 이미지인 경우
                    }
                    val lastmsgAt = LocalDateTime.parse(chatMsgs.createdAt.replace(" ", "T"))
                    lastMsgTime.text = lastmsgAt.format(formatterHM)
                } else {
                    lastMsg.text = "" // 받은 메시지 없음
                    lastMsgTime.text = ""
                }

                // 안 읽은 메시지 갯수 가져오기
                d(TAG, "뱃지 수 : ${chatRoom.msgBadge}")
                if(chatRoom.msgBadge != 0) {
                    msgBadge.visibility = View.VISIBLE
                } else {
                    msgBadge.visibility = View.GONE
                }
                msgBadge.text = chatRoom.msgBadge.toString()
            } // 채팅방에 해당하는 채팅 내용 가져오기

            chatOut.tag = chatRoom.id
            chatOut.setOnClickListener(ChatOutClickListener())

            // 아이템 태그에 채팅방의 정보를 담아둠
            itemTag["isGroupchat"] = chatRoom.isGroupchat
            itemTag["hostId"] = chatRoom.hostId
            itemTag["audienceId"] = chatRoom.audienceId
            item.tag = itemTag
        }

        // 1:1 채팅 상대방 프로필 사진 가져오기
        fun getUserData(id : Int, chatTitle : TextView?, imageView : ImageView) {
//            Logger.d(TAG, "getUserData 변수들 : $writerId")
            service.getUserData(id).enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                    Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
//                    Logger.d(TAG, "사용자 데이터 : ${response.body().toString()}")
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    val nick = gson.get("nick").asString
                    val photo = gson.get("photo").asString

                    if(chatTitle != null) {
                        chatTitle.text = nick
                        title = nick
                        itemTag["title"] = nick
                    }
                    Glide.with(context).load(photo)
                        .placeholder(R.drawable.warning)
                        .error(R.drawable.warning)
                        .into(imageView)
                }
            })
        }

        // 사용자가 들어간 채팅방에 참여하고 있는 다른 사용자들의 목록 가져오기
        fun getChatUsers(roomId: Int, hostId: Int) {
            Logger.d(TAG, "getChatUsers 변수들 : $roomId")
            service.getChatUsers(roomId).enqueue(object : Callback<MutableList<ChatUserData>> {
                override fun onFailure(call: Call<MutableList<ChatUserData>>, t: Throwable) {
                    Log.d(TAG, "채팅방 참여자 목록 가져오기 실패 : {$t}")
                }

                override fun onResponse(call: Call<MutableList<ChatUserData>>, response: Response<MutableList<ChatUserData>>) {
                    Log.d(TAG, "채팅방 참여자 목록 가져오기 요청 응답 수신 성공")
                    Log.d(TAG, response.body().toString())
                    val chatUserData = response.body()!!
                    getUserData(hostId, null, profilePicLeader) // 그룹 리더 사진 가져오기

                    // 채팅 참여자 중 그룹 리더 제외한 나머지 참여자들의 이미지 가져오기
                    chatUserData.removeIf(Predicate { data -> data.userId == hostId })
                    var count = 0
                    count = if(chatUserData.size < 3) {
                        chatUserData.size
                    } else {
                        3
                    }
                    for(i in 0 until count) {
                        if(chatUserData[i].userId != hostId) getUserData(chatUserData[i].userId, null, ivList[i+1]) // 가장 오래된 3명 사진 가져오기
                    }
                }
            })
        }

        // 그룹 채팅방의 그룹 데이터 가져오기
        fun getGroup(groupId : Int, chatTitle : TextView) {
            val userId = getUserId(context)
            Log.d(TAG, "getGroup 변수들 : $groupId, $userId")
            service.getGroup(groupId, userId).enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d(TAG, "그룹 데이터 가져오기 실패 : {$t}")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d(TAG, "그룹 데이터 가져오기 요청 응답 수신 성공")
                    Log.d(TAG, "getGroup : " + response.body().toString())
                    val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                    val mTitle = gson.get("title").asString
                    chatTitle.text = mTitle
                    title = mTitle
                    itemTag["title"] = title
                }
            })
        }

        // 사용자의 채팅방 참여 여부 데이터 생성 또는 수정하기
        fun deleteChatUser(roomId: Int) {
            val userId = getUserId(context)
            Logger.d(TAG, "deleteChatUser 변수들 : $userId, $roomId")
            service.deleteChatUser(userId, roomId).enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d(TAG, "사용자 채팅방 입장 여부 삭제 실패 : {$t}")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d(TAG, "사용자 채팅방 입장 여부 삭제 요청 응답 수신 성공")
                    Log.d(TAG, response.body().toString())
                }
            })
        }

        // 아이템 클릭 시 동작할 내용
        inner class ItemClickListener() : View.OnClickListener {
            override fun onClick(v: View?) {
                val title = ((v!!.tag as HashMap<*, *>)["title"]).toString()
                val isGroupchat = ((v.tag as HashMap<*, *>)["isGroupchat"]).toString().toBoolean()
                val hostId = ((v.tag as HashMap<*, *>)["hostId"]).toString().toInt()
                val audienceId = ((v.tag as HashMap<*, *>)["audienceId"]).toString().toInt()

                val it = Intent(context, ChatActivity::class.java)
                it.putExtra("title", title)
                it.putExtra("isGroupchat", isGroupchat)
                it.putExtra("hostId", hostId)
                it.putExtra("audienceId", audienceId)
                context.startActivity(it)
            }
        }

        inner class ChatOutClickListener() : View.OnClickListener, SnsChat() {
            override fun onClick(v: View?) {
                val chatroomId = v!!.tag as Int
                d(TAG, "나가기 누름")
                object : Thread() {
                    override fun run() {
                        d(TAG, "나가기 작동")
                        val chatRoomDelete = ChatRoomDelete(chatroomId)
                        chatDB!!.chatDao().deleteChatroom(chatRoomDelete) // 채팅방 삭제
                        val chatMsgsDelete = ChatMsgsDelete(chatroomId)
                        chatDB!!.chatDao().deleteChatMsgs(chatMsgsDelete) // 메시지 삭제
                        deleteChatUser(chatroomId) // 서버 상에 있는 사용자의 채팅방 참여 여부 삭제

                        d(TAG, "소켓 연결 중")
                        socket = Socket(SERVER_ADDRESS, PORT) // Socket 생성 및 접속
                        connectInputStream()
                        connectOutputStream()

                        val userData = JsonObject()
                        userData.addProperty("chatRoomId", chatroomId.toString())
                        userData.addProperty("userId", getUserId(context))
                        userData.addProperty("nick", getUserNick(context))
                        val gson = Gson().toJson(userData)

                        sendUserData(gson) // 채팅 서버에 채팅방 고유 번호 보내기
                        d(TAG, "소켓 연결됨")

                        // 사용자 퇴장 메시지를 띄움
                        val now = LocalDateTime.now()
                        val chatMsg = ChatMsg(0,
                            getUserId(context),
                            getUserNick(context),
                            5,
                            chatroomId,
                            now.format(formatterYMDHM).toString())
                        val gson2 = Gson().toJson(chatMsg)

                        outputStream.writeUTF(gson2) // 생성된 출력 스트림을 통하여 데이터 송신
                        closeConnect()
                    }
                }.start()
            }

        }
    }

    inner class ChatUserIOViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var content : TextView

        fun onBind(chatMsg : ChatMsg) {
            // 채팅 메시지 타입 구분하기
            content.text = chatMsg.content
        }
    }
}