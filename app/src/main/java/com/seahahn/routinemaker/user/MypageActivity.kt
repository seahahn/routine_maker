package com.seahahn.routinemaker.user

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.naverlogin.OAuthLogin
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.ToggleEditButton
import com.seahahn.routinemaker.util.ToggleEditTextView
import com.seahahn.routinemaker.util.User
import com.seahahn.routinemaker.util.UserInfo
import com.seahahn.routinemaker.util.UserInfo.getUserEmail
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserInway
import com.seahahn.routinemaker.util.UserInfo.setUserPhoto
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MypageActivity : User(), PopupMenu.OnMenuItemClickListener {

    private val TAG = this::class.java.simpleName

    lateinit var nick : ToggleEditTextView
    lateinit var nickupdate : ToggleEditButton
    lateinit var intro : ToggleEditTextView
    lateinit var introupdate : ToggleEditButton
    lateinit var photo : ImageView
    var mbsNum : Int = 0
    lateinit var photoUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // 레트로핏 통신 연결
        service = initRetrofit()

        val tbtitle = findViewById<TextView>(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.mypage) // 툴바 제목에 들어갈 텍스트
        initToolbar(tbtitle, titleText, 1) // 툴바 세팅하기

        // 사용자 정보 불러오기
        val id = getUserId(applicationContext) // DB 내 사용자의 고유 번호
        nick = findViewById(R.id.nick) // 사용자 닉네임
        val level = findViewById<TextView>(R.id.level) // 사용자 레벨
        val title = findViewById<TextView>(R.id.title) // 사용자의 칭호(타이틀)
        intro = findViewById(R.id.review) // 사용자 자기소개글
        val membership = findViewById<TextView>(R.id.membership) // 사용자 멤버십
        photo = findViewById(R.id.photo) // 사용자 프로필 사진
        mypageInfo(service, id, nick, level, title, intro, membership, photo) // 사용자 정보 불러오기

        // 사용자 정보 수정하기
        nickupdate = findViewById(R.id.nickupdate) // 사용자 닉네임
        nickupdate.setOnClickListener(BtnOnClickListener())

        val titleupdate = findViewById<TextView>(R.id.titleupdate) // 사용자의 칭호(타이틀)

        introupdate = findViewById(R.id.introupdate) // 사용자 자기소개글
        introupdate.setOnClickListener(BtnOnClickListener())

        val mbsApply = findViewById<TextView>(R.id.mbsApply) // 사용자 멤버십 신청하기 or 결제 내역
        val photoUpdate = findViewById<ImageView>(R.id.photoUpdate) // 사용자 프로필 사진
        photoUpdate.setOnClickListener(BtnOnClickListener())

        val pwchange = findViewById<TextView>(R.id.pwchange) // 비밀번호 변경하기 텍스트 버튼
        if(getUserInway(applicationContext) != "etc") pwchange.visibility = GONE
        val logout = findViewById<TextView>(R.id.logout) // 로그아웃 텍스트 버튼
        val exit = findViewById<TextView>(R.id.exit) // 회원 탈퇴 텍스트 버튼
        pwchange.setOnClickListener(BtnOnClickListener())
        logout.setOnClickListener(BtnOnClickListener())
        exit.setOnClickListener(BtnOnClickListener())
    }
//    nick : ToggleEditTextView,
    private fun mypageInfo(service : RetrofitService, id : Int, nick : ToggleEditTextView, lv : TextView, title : TextView, intro : ToggleEditTextView, mbs : TextView, photo : ImageView) {
        service.mypageInfo(id).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "마이페이지 정보 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "마이페이지 정보 가져오기 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                d(TAG, response.body().toString())

                nick.setText(gson.get("nick").asString)
                lv.text = (gson.get("lv").asInt).toString()
                title.text = gson.get("title").asString
//                        val cc = gson.get("cc").asInt
                intro.setText(gson.get("intro").asString)

                mbsNum = gson.get("mbs").asInt
                if(mbsNum == 0) {
                    mbs.text = getString(R.string.mbsBasic)
                } else if(mbsNum == 1) {
                    mbs.text = getString(R.string.mbsPremium)
                }

                photoUrl = gson.get("photo").asString
                Glide.with(applicationContext).load(photoUrl)
                    .placeholder(R.drawable.warning)
                    .error(R.drawable.warning)
                    .into(photo)

//                d(TAG, "nick.getText() : "+nick.getText())
//                d(TAG, "intro.getText() : "+intro.getText())
                nickupdate.bind(nick.getText(), nick)
                introupdate.bind(intro.getText(), intro)
            }
        })
    }

    // 수정하기, 비밀번호 변경하기, 로그아웃, 회원탈퇴 텍스트 버튼 눌렀을 때의 동작 모음
    inner class BtnOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.nickupdate -> {
                    nickupdate.bind(nick.getText(), nick)
                }

                R.id.titleupdate -> {}

                R.id.introupdate -> {
                    introupdate.bind(intro.getText(), intro)
                }

                R.id.mbsApply -> {}

                R.id.photoUpdate -> {
                    photo(v)
                }
                R.id.pwchange -> {
                    startActivity<ResetpwActivity>(
                        "from" to "Mypage",
                        "email" to getUserEmail(applicationContext)
                    )
                }
                R.id.logout -> {
                    Firebase.auth.signOut() // 구글 로그아웃
                    mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                    mOAuthLoginInstance.logout(this@MypageActivity) // 네이버 로그인 해제. 로그아웃임.

                    UserInfo.clearUser(this@MypageActivity) // 기기(SharedPref)에 저장되어 있던 사용자 정보 삭제
                    d(TAG, "로그아웃")
                    toast("로그아웃 되었습니다.")
                    startActivity<LoginActivity>()
                }
                R.id.exit -> {
                    // 구글 연동 해제
                    if(Firebase.auth.currentUser != null) {
                        val googleUser = Firebase.auth.currentUser
                        googleUser!!.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                d(TAG, "User account deleted.")
                            }
                        }
                    }

                    // 네이버 연동 해제. 네트워크 사용으로 인해 다른 스레드로 작동시킴
                    mOAuthLoginInstance = OAuthLogin.getInstance() // 네이버 로그인 인증 객체 가져오기
                    doAsync {
                        val isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(this@MypageActivity)
                        d(TAG, "네이버 로그아웃 : $isSuccessDeleteToken")
                    }

                    UserInfo.clearUser(this@MypageActivity)
                    d(TAG, "연동 해제")
                    toast("회원 탈퇴 되었습니다.")
                    startActivity<LoginActivity>()
                }
            }
        }
    }

    // 프로필 사진 변경 아이콘 눌렀을 때 팝업 메뉴를 띄워줌
    fun photo(v: View) {
        PopupMenu(this, v).apply {
            // MainActivity implements OnMenuItemClickListener
            setOnMenuItemClickListener(this@MypageActivity)
            inflate(R.menu.menu_profile_photo)
            show()
        }
    }

    // 프로필 사진 변경 아이콘의 팝업 메뉴 항목별 동작할 내용
    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.takePhoto -> {
                capturePhoto()
                true
            }
            R.id.bringPhoto -> {
                bringPhoto()
                true
            }
            R.id.deletePhoto -> {
                deletePhoto()
                true
            }
            else -> false
        }
    }

    fun deletePhoto() {
        val defaultPhotoUrl = "https://rtmaker.s3.ap-northeast-2.amazonaws.com/img_admin/profile_default.png"
        changeInfo(service, "photo", defaultPhotoUrl) // DB 내 사용자의 프로필 사진 경로 정보 변경하기
    }

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_BRING = 2
    lateinit var locationForPhotos: Uri

    // 프로필 사진 촬영하기
    fun capturePhoto() {
        d(TAG, "capturePhoto")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun bringPhoto() {
        d(TAG, "bringPhoto")
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_IMAGE_BRING)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) { // 사진 촬영한 경우의 결과 받아오기
            val thumbnail: Bitmap? = data?.getParcelableExtra("data") // 찍은 사진 이미지 썸네일 비트맵 가져오기

            val img = saveBitmapToJpg(thumbnail!!, System.currentTimeMillis().toString(), 50) // 사진 비트맵이 저장된 파일 경로 가져오기
            val imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
            val imgPath = "photo/"+System.currentTimeMillis().toString()+".jpg" // S3에 저장될 경로 설정

            // s3에 저장하기
            Amplify.Storage.uploadFile(
                imgPath, // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
                imgFile, // 실제 저장될 파일
                { result ->
                    d(TAG, "Successfully uploaded : $result")
                    val s3url = getString(R.string.s3_bucket_route) // s3 루트 경로(위 imgPath의 앞부분)
                    changeInfo(service, "photo", "$s3url$imgPath") // DB 내 사용자의 프로필 사진 경로 정보 변경하기
                },
                { error -> d(TAG, "Upload failed", error) }
            )
        } else if(requestCode == REQUEST_IMAGE_BRING && resultCode == Activity.RESULT_OK) { // 갤러리에서 사진 가져온 경우의 결과 받아오기
            val imgUri : Uri? = data?.data

            val thumbnail = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)

//            val thumbnail: Bitmap? = data?.getParcelableExtra("data") // 찍은 사진 이미지 썸네일 비트맵 가져오기

            val img = saveBitmapToJpg(thumbnail!!, System.currentTimeMillis().toString(), 50) // 사진 비트맵이 저장된 파일 경로 가져오기
            val imgFile = File(img) // 가져온 경로를 바탕으로 파일로 만들기
            val imgPath = "photo/"+System.currentTimeMillis().toString()+".jpg" // S3에 저장될 경로 설정

            // s3에 저장하기
            Amplify.Storage.uploadFile(
                imgPath, // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
                imgFile, // 실제 저장될 파일
                { result ->
                    d(TAG, "Successfully uploaded : $result")
                    val s3url = getString(R.string.s3_bucket_route) // s3 루트 경로(위 imgPath의 앞부분)
                    changeInfo(service, "photo", "$s3url$imgPath") // DB 내 사용자의 프로필 사진 경로 정보 변경하기
                },
                { error -> d(TAG, "Upload failed", error) }
            )
        }
    }

    // 사용자 정보 변경 요청하기
    private fun changeInfo(service : RetrofitService, subject : String, content: String) {
        val id = getUserId(applicationContext)
        service.changeInfo(id, subject, content).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "사용자 정보 수정 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "사용자 정보 수정 요청 성공")
                d(TAG, "response.body().toString() : "+response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                d(TAG, msg)
                if(result) {
                    if(subject == "photo") {
                        photoUrl = gson.get("photo").asString
                        setUserPhoto(applicationContext, photoUrl)
                        Glide.with(applicationContext).load(photoUrl)
                            .placeholder(R.drawable.warning)
                            .error(R.drawable.warning)
                            .into(photo)
                    }
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 사진 촬영 후 저장된 사진 비트맵을 임시 디렉토리에 이미지 파일로 저장하는 메소드
//    fun saveBitmapToJpg(bitmap: Bitmap, name: String): String {
//        /**
//         * 캐시 디렉토리에 비트맵을 이미지파일로 저장하는 코드입니다.
//         *
//         * @version target API 28 ★ API29이상은 테스트 하지않았습니다.★
//         * @param Bitmap bitmap - 저장하고자 하는 이미지의 비트맵
//         * @param String fileName - 저장하고자 하는 이미지의 비트맵
//         *
//         * File storage = 저장이 될 저장소 위치
//         *
//         * return = 저장된 이미지의 경로
//         *
//         * 비트맵에 사용될 스토리지와 이름을 지정하고 이미지파일을 생성합니다.
//         * FileOutputStream으로 이미지파일에 비트맵을 추가해줍니다.
//         */
//        val storage: File = cacheDir //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
//        val fileName = "$name.jpg"
//        val imgFile = File(storage, fileName)
//        try {
//            imgFile.createNewFile()
//            val out = FileOutputStream(imgFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out) //썸네일로 사용하므로 퀄리티를 낮게설정
//            out.close()
//        } catch (e: FileNotFoundException) {
//            Log.e("saveBitmapToJpg", "FileNotFoundException : " + e)
//        } catch (e: IOException) {
//            Log.e("saveBitmapToJpg", "IOException : " + e)
//        }
//        d("imgPath", "$cacheDir/$fileName")
//        return "$cacheDir/$fileName"
//    }

}