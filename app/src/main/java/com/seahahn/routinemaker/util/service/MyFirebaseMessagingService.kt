package com.seahahn.routinemaker.util.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.util.Log.d
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChatroomData
import com.seahahn.routinemaker.sns.chat.*
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.setUserFCMToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    private lateinit var retrofit : Retrofit
    lateinit var service : RetrofitService

    lateinit var msg : String
    lateinit var chatMsg : ChatMsg

    lateinit var chatroomData : ChatroomData // 채팅방 데이터
    val chatDB by lazy { ChatDataBase.getInstance(this) } // 채팅 내용 저장해둔 Room DB 객체 가져오기

    lateinit var title : String // 채팅방 제목

    lateinit var intent : Intent
//    val intent by lazy { Intent(this, ChatActivity::class.java) } // 채팅 내용 저장해둔 Room DB 객체 가져오기

    // FirebaseInstanceIdService는 이제 사라짐. 이제 이걸 사용함
    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")

        // 토큰 값을 따로 저장해둔다.
        setUserFCMToken(this, token)

        Log.i("로그: ", "성공적으로 토큰을 저장함")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage!!.from)

        // Notification 메시지를 수신할 경우는
        // remoteMessage.notification?.body!! 여기에 내용이 저장되어있다.
        // Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)

        if(remoteMessage.data.isNotEmpty()){
            Log.i("타이틀: ", remoteMessage.data["title"].toString())
            Log.i("바디: ", remoteMessage.data["body"].toString())

            retrofit = RetrofitClient.getInstance()
            service = retrofit.create(RetrofitService::class.java)

            msg = remoteMessage.data["body"].toString()
            chatMsg = Gson().fromJson(msg, ChatMsg::class.java)
            chatDB!!.chatDao().insertChatMsg(chatMsg) // 채팅방에 메시지 추가

            if(chatMsg.contentType != 4 && chatMsg.contentType != 5) { // 사용자의 입장 또는 퇴장을 제외한 일반적인 메시지인 경우에만 알림 띄우기
                getChatRoomData(chatMsg.roomId, remoteMessage) // 이동해야 할 채팅방 데이터 가져오기

                val badgeBefore = chatDB!!.chatDao().getChatroom(chatMsg.roomId).msgBadge // 이전 뱃지 숫자
                val badgeUpdate = ChatRoomBadgeUpdate(chatMsg.roomId, badgeBefore+1) // 이전 숫자에 1 추가
                chatDB!!.chatDao().updateBadge(badgeUpdate) // 채팅방에 안 읽은 메시지 갯수 수정(채팅방 목록 뱃지에 표시)
            }

//            sendNotification(remoteMessage)
        }

        else {
            Log.i("수신에러: ", "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Log.i("data값: ", remoteMessage.data.toString())
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시되도록 함
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent
        // PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임한다.
        d(TAG, "chatroomData : $chatroomData")
        intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("isGroupchat", chatroomData.isGroupchat) // 그룹채팅인지 1:1채팅인지 여부
        intent.putExtra("hostId", chatroomData.hostId) // 개설자 ID
        intent.putExtra("audienceId", chatroomData.audienceId) // 채팅방이 속한 그룹 고유 번호 또는 1:1채팅 상대방 고유 번호
        intent.putExtra("title", title)
//        if(chatroomData.isGroupchat) { // 그룹 채팅인 경우 그룹명을 채팅방 제목으로 설정
//            getGroup(chatroomData.audienceId)
//        } else { // 1:1 채팅인 경우 대화 상대방 닉네임을 채팅방 제목으로 설정
//            if(chatroomData.hostId != getUserId(this)) {
//                getUserData(chatroomData.hostId)
//            } else {
//                getUserData(chatroomData.audienceId)
//            }
//        }
//        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack 을 경로만 남긴다. A-B-C-D-B => A-B
        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)

        // 알림 채널 이름
        val channelId = getString(R.string.default_notification_channel_id)

        // 알림 소리
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림에 대한 UI 정보와 작업을 지정한다.
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 설정
            .setContentTitle(remoteMessage.data["title"].toString()) // 제목
            .setContentText(if(chatMsg.contentType == 0) { chatMsg.content } else { "사진" }) // 메시지 내용(0은 텍스트, 그 외(1)는 사진)
            .setAutoCancel(true)
            .setSound(soundUri) // 알림 소리
            .setContentIntent(pendingIntent) // 알림 실행 시 Intent

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요하다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())
    }

    // 알림 누르면 연결될 채팅방에 대한 정보를 가져옴
    fun getChatRoomData(roomId: Int, remoteMessage: RemoteMessage) {
        Logger.d(TAG, "getChatRoomData Mini 변수 : $roomId")
        service.getChatRoomData(roomId).enqueue(object : Callback<ChatroomData> {
            override fun onFailure(call: Call<ChatroomData>, t: Throwable) {
                Log.d(TAG, "채팅방 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<ChatroomData>, response: Response<ChatroomData>) {
                Log.d(TAG, "채팅방 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "body : ${response.body().toString()}")
                chatroomData = response.body()!!
                if(chatroomData.isGroupchat) { // 그룹 채팅인 경우 그룹명을 채팅방 제목으로 설정
                    getGroup(chatroomData.audienceId, remoteMessage)
                } else { // 1:1 채팅인 경우 대화 상대방 닉네임을 채팅방 제목으로 설정
                    if(chatroomData.hostId != getUserId(applicationContext)) {
                        getUserData(roomId, chatroomData.hostId, remoteMessage)
                    } else {
                        getUserData(roomId, chatroomData.audienceId, remoteMessage)
                    }
                }
            }
        })
    }

    // 그룹 채팅일 경우 채팅방 제목을 그룹명으로 쓰기 위해 데이터를 가져옴
    fun getGroup(roomId: Int, remoteMessage: RemoteMessage) {
        Logger.d(TAG, "getChatRoomData Mini 변수 : $roomId")
        service.getGroup(roomId, getUserId(this)).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "채팅방 그룹 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "채팅방 그룹 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "body : ${response.body().toString()}")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                title = gson.get("title").asString
                d(TAG, "title : $title")

                object : Thread() {
                    override fun run() {
                        // 채팅방 검색을 위해서 채팅방 제목(그룹명 또는 상대방 닉네임) 저장해두기
                        val titleUpdate = ChatRoomTitleUpdate(roomId, title)
                        chatDB!!.chatDao().updateTitle(titleUpdate)
                    }
                }.start()

                sendNotification(remoteMessage)
            }
        })
    }

    // 채팅 상대방의 닉네임을 채팅방 제목으로 쓰기 위해 데이터를 가져옴
    fun getUserData(roomId: Int, userId : Int, remoteMessage: RemoteMessage) {
        Logger.d(TAG, "getUserData 변수 : $userId")
        service.getUserData(userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                title = gson.get("nick").asString
                d(TAG, "title : $title")

                object : Thread() {
                    override fun run() {
                        // 채팅방 검색을 위해서 채팅방 제목(그룹명 또는 상대방 닉네임) 저장해두기
                        val titleUpdate = ChatRoomTitleUpdate(roomId, title)
                        chatDB!!.chatDao().updateTitle(titleUpdate)
                    }
                }.start()

                sendNotification(remoteMessage)
            }
        })
    }
}