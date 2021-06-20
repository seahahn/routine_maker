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
import com.seahahn.routinemaker.sns.ChatroomData
import com.seahahn.routinemaker.sns.chat.ChatContentsViewModel
import com.seahahn.routinemaker.sns.chat.ChatDataBase
import com.seahahn.routinemaker.sns.chat.ChatMsg
import com.seahahn.routinemaker.sns.chat.ChatroomViewModel
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
        connectSocket(SERVER_ADDRESS, PORT, chatroomData.id.toString(), getUserNick(this))
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
        showProgress(false)
        sleep(500)
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
                showProgress(false)
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

                chatDB!!.chatDao().getChatMsgs(chatroomData.id).observe(lifecycleOwner) { chatMsgs ->
                    chatContentsViewModel.setList(chatMsgs)
                } // 채팅방에 해당하는 채팅 내용 가져오기
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

    @Throws(IOException::class)
    fun connectSocket(host: String, port: Int, chatRoomId : String, nick : String) {
        socket = Socket(host, port) // Socket 생성 및 접속
        connectInputStream()
        connectOutputStream()

        val userData = JsonObject()
        userData.addProperty("chatRoomId", chatRoomId)
        userData.addProperty("nick", nick)
        val gson = Gson().toJson(userData)

        sendUserData(gson) // 채팅 서버에 채팅방 고유 번호 보내기
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