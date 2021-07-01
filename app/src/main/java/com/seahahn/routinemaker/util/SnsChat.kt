package com.seahahn.routinemaker.util

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChatUserData
import com.seahahn.routinemaker.sns.ChatroomData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.sns.chat.*
import com.seahahn.routinemaker.util.UserInfo.getUserFCMToken
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserNick
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.lang.Thread.sleep
import java.net.Socket
import java.net.SocketException
import java.time.LocalDateTime

open class SnsChat : Sns() {

    private val TAG = this::class.java.simpleName

    val PORT = 33333
    val SERVER_ADDRESS = "15.165.168.238"
//    val SERVER_ADDRESS = "10.0.2.2"

    lateinit var socket: Socket
    lateinit var inputStream: DataInputStream
    lateinit var outputStream: DataOutputStream

    val chatroomViewModel by viewModels<ChatroomViewModel>() // 채팅방 데이터
    val chatContentsViewModel by viewModels<ChatContentsViewModel>() // 채팅방 내 채팅 내용 목록 데이터
    val chatMembersViewModel by viewModels<ChatMembersViewModel>() // 채팅방 내 채팅 내용 목록 데이터

//    val chatDB by lazy { ChatDataBase.getInstance(this) } // 채팅 내용 저장해둔 Room DB 객체 가져오기

    lateinit var chatroomData : ChatroomData // 채팅방 데이터
    lateinit var chatUserData : MutableList<ChatUserData> // 채팅방 참여자 목록 데이터
    lateinit var chatUserDataMap : HashMap<Int, String> // 채팅방 참여자 목록을 id, token 쌍의 맵으로 저장한 것
    lateinit var chatContents : MutableList<ChatMsg>
    lateinit var chatUsersNickAndPhoto : MutableList<GroupMemberData>

    var isSocketConnected = false
    // 소켓 통신 스레드
    val socketThread = Thread {
        println(getUserId(this))
        connectSocket(SERVER_ADDRESS, PORT, chatroomData.id.toString(), getUserId(this), getUserNick(this))
        receiveMsg()
    }
    // 채팅 내용 불러오는 스레드
    val chatContentsThread = Thread{
        chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(this) { chatMsgs ->
//            chatContents = chatMsgs
            chatContentsViewModel.setList(chatMsgs)
        } // 채팅방에 해당하는 채팅 내용 가져오기
    }

    // 이미지 업로드하는 스레드
    var imgUploadCount = 0
    val imgUploadThread = Thread {
        saveImgsURL(imgDatasChat, imagesList)
//        makeMsg(service, imagesURL, 1, chatroomData.id)
//        val now = LocalDateTime.now()
//        val chatMsg = ChatMsg(0,
//            UserInfo.getUserId(applicationContext),
//            imagesURL,
//            1,
//            chatroomData.id,
//            now.format(formatterYMDHM).toString())
//
//        val gson = Gson().toJson(chatMsg)
//        sendMessageToServer(gson)
    }

    // 사진 촬영하기
    override fun capturePhoto() {
        Log.d(TAG, "capturePhoto")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(System.currentTimeMillis().toString(), "image/jpeg")?.let { uri ->
            photoURI = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            getContent.launch(intent)
        }
    }

    // 사진 가져오기(피드 최대 5장, 댓글 최대 1장)
    override fun bringPhoto() {
        Log.d(TAG, "bringPhoto")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        val chooser = Intent.createChooser(intent, getString(R.string.maxFivePics))
        getContent.launch(chooser)
        toast(getString(R.string.maxThirtyPics))
    }

    // 사진 찍기 혹은 가져오기의 결과물 가져오기
    override val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(TAG, "result : $result")
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) { // 사진 여러개 선택한 경우
                    val count = data.clipData!!.itemCount
                    if (count > 30) {
                        toast(getString(R.string.maxThirtyPicsWarning))
                    } else {
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            imgDatasChat.add(imageUri)
                        }
                    }
                } else if(photoURI != null) { // 카메라로 사진 촬영한 경우
                    Logger.d(TAG, "photoURI : $photoURI")
                    Logger.d(TAG, "data : $data")
                    val imageUri = photoURI
                    imgDatasChat.add(imageUri as Any)
                } else {
                    Logger.d(TAG, "photo error")
                }

                // 이미지 데이터가 있으면 이미지 메시지 전송하기
                if(imgDatasChat.isNotEmpty()) {
                    saveImgsURL(imgDatasChat, imagesList)
                    makeMsg(service, imagesURL, 1, chatroomData.id)
                    object : Thread() {
                        override fun run() {
                            val now = LocalDateTime.now()
                            val chatMsg = ChatMsg(0,
                                UserInfo.getUserId(applicationContext),
                                imagesURL,
                                1,
                                chatroomData.id,
                                now.format(formatterYMDHM).toString())

                            val gson = Gson().toJson(chatMsg)
                            sendMessageToServer(gson)
                        }
                    }.start()
//                        hideSoftKeyboard()
                }
            }
        }

    // 사진 경로 저장하기
    override fun saveImgsURL(imgDatas : MutableList<Any>, imagesList : MutableList<String>) {
        Logger.d(TAG, "imgDatas : $imgDatas")
        Logger.d(TAG, "imagesList : $imagesList")
        var imgUri : Any?
        var thumbnail : Bitmap

        var img : String
        var imgFile : File
        var imgPath : String
        val s3url = getString(R.string.s3_bucket_route) // s3 루트 경로(위 imgPath의 앞부분)

        showProgress(true)
        for(i in 0 until imgDatas.size) {
            when {
                imgDatas[i] is Uri -> { // 갤러리에서 사진 가져온 경우
                    d(TAG, "갤러리")
                    imgUri = imgDatas[i]
                    thumbnail = MediaStore.Images.Media.getBitmap(contentResolver, imgUri as Uri)

                    img = saveBitmapToJpg(thumbnail, System.currentTimeMillis().toString(), 100) // 사진 비트맵이 저장된 파일 경로 가져오기
                    imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
                    imgPath = "photo/" + System.currentTimeMillis().toString() + ".jpg" // S3에 저장될 경로 설정

                    imagesList.add("$s3url$imgPath") // 이미지 경로 추가

                    // s3에 저장하기
//                    runOnUiThread {
//                        showProgress(true)
//                    }
                        uploadFileToAWS(imgPath, imgFile)
                }
                imgDatas[i] is Bitmap -> { // 카메라로 사진 찍은 경우
                    d(TAG, "카메라")
                    img = saveBitmapToJpg(imgDatas[i] as Bitmap, System.currentTimeMillis().toString(), 100) // 사진 비트맵이 저장된 파일 경로 가져오기
                    imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
                    imgPath = "photo/" + System.currentTimeMillis().toString() + ".jpg" // S3에 저장될 경로 설정

                    imagesList.add("$s3url$imgPath") // 이미지 경로 추가

                    // s3에 저장하기
//                    runOnUiThread {
//                        showProgress(true)
//                    }
                        uploadFileToAWS(imgPath, imgFile)
                }
                else -> { // 원래 있던 것 그대로 둔 경우
                    imagesList.add(imgDatas[i].toString())
                }
            }
        }
        imagesURL = imagesList.toString() // 이미지 경로를 모아둔 리스트를 문자열로 바꾸어 저장
        imgDatas.clear()
        imagesList.clear()
        sleep(1000)
        showProgress(false)
//        val acceptedList = Arrays.stream(acceptedListString.substring(1, acceptedListString.length - 1).split(",").toTypedArray())
//            .map { obj: String -> obj.trim { it <= ' ' } }.mapToInt(Integer::parseInt).toArray()
    }

    override fun uploadFileToAWS(imgPath : String, imgFile : File) {
        // s3에 저장하기
        Amplify.Storage.uploadFile(
            imgPath, // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
            imgFile, // 실제 저장될 파일
            { result ->
                Log.d(TAG, "Successfully uploaded : $result")
//                showProgress(false)
            },
            { error -> Log.d(TAG, "Upload failed", error) }
        )
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

                setChatUser(service, chatroomData.id, true, getUserFCMToken(applicationContext)) // 사용자의 채팅방 입장 여부 데이터 보내기
                chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(lifecycleOwner) { chatMsgs ->
                    chatContentsViewModel.setList(chatMsgs)
                } // 채팅방에 해당하는 채팅 내용 가져오기
//                getChatUsers(service, chatroomData.id)
            }
        })
    }

    // 채팅 메시지 작성하기
    fun makeMsg(service: RetrofitService, content: String, contentType: Int, roomId: Int) {
        val writerId = UserInfo.getUserId(applicationContext)
        Logger.d(TAG, "makeMsg 변수들 : $writerId, $content, $contentType, $roomId")
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

    // 사용자의 채팅방 참여 여부 데이터 생성 또는 수정하기
    fun setChatUser(service: RetrofitService, roomId: Int, isIn: Boolean, token: String) {
        val userId = UserInfo.getUserId(applicationContext)
        Logger.d(TAG, "setChatUser 변수들 : $roomId, $userId, $isIn, $token")
        service.setChatUser(roomId, userId, isIn, token).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "사용자 채팅방 입장 여부 데이터 전송 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "사용자 채팅방 입장 여부 데이터 전송 요청 응답 수신 성공")
                Log.d(TAG, response.body().toString())
            }
        })
    }

    // 사용자가 들어간 채팅방에 참여하고 있는 다른 사용자들의 목록 가져오기
    fun getChatUsers(service: RetrofitService, roomId: Int) {
        Logger.d(TAG, "getChatUsers 변수들 : $roomId")
        service.getChatUsers(roomId).enqueue(object : Callback<MutableList<ChatUserData>> {
            override fun onFailure(call: Call<MutableList<ChatUserData>>, t: Throwable) {
                Log.d(TAG, "채팅방 참여자 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<ChatUserData>>, response: Response<MutableList<ChatUserData>>) {
                Log.d(TAG, "채팅방 참여자 목록 가져오기 요청 응답 수신 성공")
                Log.d(TAG, response.body().toString())
                chatUserData = response.body()!!
            }
        })
    }

    // 사용자가 들어간 채팅방에 참여하고 있는 다른 사용자들의 목록 가져오기
    fun getChatUsersNickAndPhoto(roomId: Int) {
        Logger.d(TAG, "getChatUsersNickAndPhoto 변수들 : $roomId")
        service.getChatUsersNickAndPhoto(roomId).enqueue(object : Callback<MutableList<GroupMemberData>> {
            override fun onFailure(call: Call<MutableList<GroupMemberData>>, t: Throwable) {
                Log.d(TAG, "채팅방 참여자 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<GroupMemberData>>, response: Response<MutableList<GroupMemberData>>) {
                Log.d(TAG, "채팅방 참여자 목록 가져오기 요청 응답 수신 성공")
                Log.d(TAG, response.body().toString())
                chatUsersNickAndPhoto = response.body()!!
                chatMembersViewModel.setList(chatUsersNickAndPhoto)
            }
        })
    }

    // 사용자의 채팅방 참여 여부 데이터 생성 또는 수정하기(채팅 목록 어댑터에서 별도로 사용중)
    fun deleteChatUser(service: RetrofitService, roomId: Int) {
        val userId = getUserId(applicationContext)
        Logger.d(TAG, "setChatUser 변수들 : $userId, $roomId")
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

    @Throws(IOException::class)
    fun connectSocket(host: String, port: Int, chatRoomId : String, userId : Int, nick : String) {
        d(TAG, "소켓 연결 중")
        socket = Socket(host, port) // Socket 생성 및 접속
        connectInputStream()
        connectOutputStream()

        val userData = JsonObject()
        userData.addProperty("chatRoomId", chatRoomId)
        userData.addProperty("userId", userId)
        userData.addProperty("nick", nick)
        val gson = Gson().toJson(userData)

        sendUserData(gson) // 채팅 서버에 채팅방 고유 번호 보내기
        d(TAG, "소켓 연결됨")

        // 채팅방 정보 불러오기
        val chatRoomLocal = chatDB!!.chatDao().getChatroom(chatroomData.id)
        d(TAG, "chatRoomLocal : $chatRoomLocal")

        if(chatRoomLocal == null) {
            val chatRoom = ChatRoom(
                chatroomData.id,
                "",
                chatroomData.isGroupchat,
                chatroomData.hostId,
                chatroomData.audienceId,
                chatroomData.createdAt,
                "",
                chatroomData.createdAt,
                0
            )
            chatDB!!.chatDao().insertChatRoom(chatRoom) // 채팅방 데이터 저장하기

            // 채팅방 검색을 위해서 채팅방 제목(그룹명 또는 상대방 닉네임) 저장해두기
            val titleUpdate = ChatRoomTitleUpdate(chatroomData.id, groupTitle)
            chatDB!!.chatDao().updateTitle(titleUpdate)

            // 사용자 입장 메시지를 띄움
            val now = LocalDateTime.now()
            val chatMsg = ChatMsg(0,
                getUserId(applicationContext),
                getUserNick(applicationContext),
                4,
                chatroomData.id,
                now.format(formatterYMDHM).toString())

            val gson = Gson().toJson(chatMsg)
            sendMessageToServer(gson)
        } else {
            d(TAG, "뱃지 0으로 만들기")
            // 읽은 메시지 갯수 0으로 유지(방에 들어와 있기 때문)
            val badgeUpdate = ChatRoomBadgeUpdate(chatroomData.id, 0) // 다 읽은 것으로 표시
            chatDB!!.chatDao().updateBadge(badgeUpdate) // 채팅방에 안 읽은 메시지 갯수 수정(채팅방 목록 뱃지에 표시)

            val now = LocalDateTime.now()
            val lastMsgAtUpdate = ChatRoomLastMsgAtUpdate(chatroomData.id, now.format(formatterYMDHM).toString()) // 다 읽은 것으로 표시
            chatDB!!.chatDao().updateLastMsgAt(lastMsgAtUpdate) // 채팅방에 안 읽은 메시지 갯수 수정(채팅방 목록 뱃지에 표시)
        }

        getChatUsersNickAndPhoto(chatroomData.id) // 채팅방 참여자 목록 가져오기
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

                // 누군가가 입장 또는 퇴장한 경우 채팅 참여자 목록을 새로 불러옴
                if(chatMsg.contentType == 4 || chatMsg.contentType == 5)
                    Thread() {
                        getChatUsersNickAndPhoto(chatroomData.id) // 채팅방 참여자 목록 가져오기
                    }.start()
            } catch(e: SocketException) {
                d(TAG, "소켓 연결 해제")
//                e.printStackTrace()
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
    fun sendUserData(data: String) {
        outputStream.writeUTF(data) // 생성된 출력 스트림을 통하여 데이터 송신
    }

    @Throws(IOException::class)
    fun sendMessageToServer(msg: String) {
        outputStream.writeUTF(msg) // 생성된 출력 스트림을 통하여 데이터 송신
        chatInput.text = null // 메시지 보낸 후 입력란 비우기
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