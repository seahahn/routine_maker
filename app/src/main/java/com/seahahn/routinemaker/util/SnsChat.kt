package com.seahahn.routinemaker.util

import android.content.Context
import android.util.Log
import android.util.Log.d
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChatroomData
import com.seahahn.routinemaker.sns.chat.ChatContentsViewModel
import com.seahahn.routinemaker.sns.chat.ChatDataBase
import com.seahahn.routinemaker.sns.chat.ChatMsg
import com.seahahn.routinemaker.sns.chat.ChatroomViewModel
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketException

open class SnsChat : Sns() {

    private val TAG = this::class.java.simpleName

    val PORT = 33333
    //    private val SERVER_ADDRESS = "15.165.168.238"
    val SERVER_ADDRESS = "10.0.2.2"

    lateinit var socket: Socket
    lateinit var inputStream: DataInputStream
    lateinit var outputStream: DataOutputStream

    val chatroomViewModel by viewModels<ChatroomViewModel>() // 채팅방 데이터
    val chatContentsViewModel by viewModels<ChatContentsViewModel>() // 채팅방 내 채팅 내용 목록 데이터

    val chatDB by lazy { ChatDataBase.getInstance(this) } // 채팅 내용 저장해둔 Room DB 객체 가져오기

    lateinit var chatroomData : ChatroomData // 채팅방 데이터
    lateinit var chatContents : MutableList<ChatMsg>

    var isSocketConnected = false
    // 소켓 통신 스레드
    val socketThread = Thread {
        connectSocket(SERVER_ADDRESS, PORT, chatroomData.id.toString())
        receiveMsg()
    }
    // 채팅 내용 불러오는 스레드
    val chatContentsThread = Thread{
        chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(this) { chatMsgs ->
//            chatContents = chatMsgs
            chatContentsViewModel.setList(chatMsgs)
        } // 채팅방에 해당하는 채팅 내용 가져오기
    }


    // 채팅방 정보 불러오기
    fun getChatRoomData(lifecycleOwner: LifecycleOwner, service: RetrofitService, isGroupchat : Boolean, hostId : Int, audienceId : Int) {
        Logger.d(TAG, "getChatRoomData 변수들 : $isGroupchat, $hostId, $audienceId")
        service.getChatRoomData(isGroupchat, hostId, audienceId).enqueue(object : Callback<ChatroomData> {
            override fun onFailure(call: Call<ChatroomData>, t: Throwable) {
                Log.d(TAG, "채팅방 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<ChatroomData>, response: Response<ChatroomData>) {
                Log.d(TAG, "채팅방 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "body : ${response.body().toString()}")
                chatroomData = response.body()!!

                isSocketConnected = true
                socketThread.start()

                chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(lifecycleOwner) { chatMsgs ->
                    chatContentsViewModel.setList(chatMsgs)
                } // 채팅방에 해당하는 채팅 내용 가져오기
            }
        })
    }

    // 채팅 메시지 작성하기
    fun makeMsg(service: RetrofitService, content: String, contentType: Int, roomId: Int) {
        Logger.d(TAG, "makeMsg 변수들 : $content, $contentType, $roomId")
        val writerId = UserInfo.getUserId(applicationContext)
        service.makeMsg(writerId, content, contentType, roomId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "채팅 메시지 작성 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "채팅 메시지 작성 요청 응답 수신 성공")
                Log.d(TAG, response.body().toString())
            }
        })
    }

    @Throws(IOException::class)
    fun connectSocket(host: String, port: Int, chatroomId : String) {
        socket = Socket(host, port) // Socket 생성 및 접속
        connectInputStream()
        connectOutputStream()

        sendChatRoomId(chatroomId) // 채팅 서버에 채팅방 고유 번호 보내기
        d(TAG, "소켓 연결됨")
    }

    fun receiveMsg() {
        while(isSocketConnected) {
            d(TAG, "메시지 수신 중")
            try {
                val msg = receiveMessageFromServer()
                println(msg)

//                val chatMsg = ChatMsg(0,
//                    UserInfo.getUserId(applicationContext), chatInput.text.toString(), 0, chatroomData.id, System.currentTimeMillis().toString())
                val chatMsg = Gson().fromJson(msg, ChatMsg::class.java)
                chatDB!!.chatDao().insertChatMsg(chatMsg) // 채팅방에 해당하는 채팅 내용 가져오기
            } catch(e: SocketException) {
                d(TAG, "소켓 연결 해제")
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun connectInputStream() {
        inputStream = DataInputStream(socket.getInputStream()) // 생성된 socket을 통해 DataInputStream생성
    }

    @Throws(IOException::class)
    fun connectOutputStream() { // 생성된 socket을 통해 DataOutputStream생성
        outputStream = DataOutputStream(socket.getOutputStream())
    }

    @Throws(IOException::class)
    fun sendChatRoomId(id: String) {
        outputStream.writeUTF(id) // 생성된 출력 스트림을 통하여 데이터 송신
    }

    @Throws(IOException::class)
    fun sendMessageToServer(msg: String) {
        outputStream.writeUTF(msg) // 생성된 출력 스트림을 통하여 데이터 송신
        chatInput.text = null
    }

    @Throws(IOException::class)
    fun receiveMessageFromServer(): String {
        return inputStream.readUTF()
    }

    fun closeConnect() {
        isSocketConnected = false

        outputStream.close()
        inputStream.close()
        socket.close()
    }
}