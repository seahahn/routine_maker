package com.seahahn.routinemaker.util

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.DateViewModel
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.sns.ChatroomData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

open class Util  : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    lateinit var service : RetrofitService
    private val rfServiceViewModel by viewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델

    open lateinit var drawerLayout : DrawerLayout // 좌측 내비게이션 메뉴가 포함된 액티비티의 경우 DrawerLayout을 포함하고 있음
    var homeBtn : Int = 0

    // 카메라 원본이미지 Uri를 저장할 변수
    var photoURI: Uri? = null

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit(): RetrofitService {
        d(TAG, "initRetrofit")
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)

        rfServiceViewModel.setService(service) // 뷰모델에 레트로핏 서비스 객체 저장하기

        return service
    }

    open fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int -> finish() }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
            .show()
    }

    // 상단 툴바 초기화
    fun initToolbar(title: TextView, titleText: String, leftIcon: Int) {

        setSupportActionBar(findViewById(R.id.toolbar)) // 커스텀 툴바 설정

        supportActionBar!!.setDisplayShowTitleEnabled(false) //기본 제목을 없애줍니다
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // 좌측 화살표 누르면 이전 액티비티로 가도록 함

        // 툴바 좌측 아이콘 설정. 0이면 햄버거 메뉴 아이콘, 1이면 좌향 화살표 아이콘.
        if(leftIcon == 0) {
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.hbgmenu) // 왼쪽 버튼 이미지 설정 - 좌측 햄버거 메뉴
            homeBtn = R.drawable.hbgmenu
        } else if(leftIcon == 1){
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.backward_arrow) // 왼쪽 버튼 이미지 설정 - 좌향 화살표
            homeBtn = R.drawable.backward_arrow
        }
        title.text = titleText // 제목 설정
    }

    // 툴바 좌측 버튼 클릭 시 작동할 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        d(TAG, "onOptionsItemSelected")
        when(item.itemId){
            android.R.id.home->{ // 툴바 좌측 버튼
                if(homeBtn == R.drawable.hbgmenu) { // 햄버거 메뉴 버튼일 경우
                    drawerLayout.openDrawer(GravityCompat.START) // 네비게이션 드로어 열기
                } else if(homeBtn == R.drawable.backward_arrow) { // 좌향 화살표일 경우
                    d(TAG, "뒤로 가기")
                    finish() // 액티비티 종료하기
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 액티비티 하단에 꽉 차는 버튼 있는 경우에 해당 버튼의 텍스트 설정하기
    open fun setFullBtmBtnText(btn: Button) {
        when(btn.id) {
            R.id.resetpwBtn -> btn.text = getString(R.string.resetpwtext1)
            R.id.makeRt -> btn.text = getString(R.string.makeRt)
            R.id.updateRt -> btn.text = getString(R.string.updateRt)
            R.id.makeTodo -> btn.text = getString(R.string.makeTodo)
            R.id.updateTodo -> btn.text = getString(R.string.updateTodo)
            R.id.makeAction -> btn.text = getString(R.string.makeAction)
            R.id.updateAction -> btn.text = getString(R.string.updateAction)
        }
    }

    // EditText에 있던 포커스를 다른 곳 클릭하면 해제해주는 메소드
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    if(TAG != "GroupFeedDetailActivity") {
                        val imm: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // 사진 촬영 후 저장된 사진 비트맵을 임시 디렉토리에 이미지 파일로 저장하는 메소드
    fun saveBitmapToJpg(bitmap: Bitmap, name: String, quality: Int): String {
        /**
         * 캐시 디렉토리에 비트맵을 이미지파일로 저장하는 코드입니다.
         *
         * @version target API 28 ★ API29이상은 테스트 하지않았습니다.★
         * @param Bitmap bitmap - 저장하고자 하는 이미지의 비트맵
         * @param String fileName - 저장하고자 하는 이미지의 비트맵
         *
         * File storage = 저장이 될 저장소 위치
         *
         * return = 저장된 이미지의 경로
         *
         * 비트맵에 사용될 스토리지와 이름을 지정하고 이미지파일을 생성합니다.
         * FileOutputStream으로 이미지파일에 비트맵을 추가해줍니다.
         */
        val storage: File = cacheDir //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
        val fileName = "$name.jpg"
        val imgFile = File(storage, fileName)
        try {
            imgFile.createNewFile()
            val out = FileOutputStream(imgFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out) //썸네일로 사용하므로 퀄리티를 낮게설정
            out.close()
        } catch (e: FileNotFoundException) {
            Log.e("saveBitmapToJpg", "FileNotFoundException : " + e)
        } catch (e: IOException) {
            Log.e("saveBitmapToJpg", "IOException : " + e)
        }
        d("imgPath", "$cacheDir/$fileName")
        return "$cacheDir/$fileName"
    }

    // 사진 원본 가져오기 위한 메소드들
    fun createImageUri(filename: String, mimeType: String) : Uri? { // 이미지 URI 생성
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // 이미지 불러오기
    fun loadBitmapFromMediaStoreBy(photoUri: Uri): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) { // Api 버전별 이미지 처리
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    // 채팅방 정보 불러오기
    fun setFirebaseToken(service: RetrofitService, id : Int, token : String) {
        Logger.d(TAG, "setFirebaseToken 변수들 : $id, $token")
        service.setFirebaseToken(id, token).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "토큰값 보내기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "토큰값 보내기 요청 응답 수신 성공")
                Log.d(TAG, "body : ${response.body().toString()}")
            }
        })
    }








    // 파일 업로드 예시
//        val exampleFile = File(applicationContext.filesDir, "ExampleKey")
//
//        exampleFile.writeText("Example file contents")
//
//        Amplify.Storage.uploadFile(
//            "test/test.txt", // S3 버킷 내 저장 경로. 맨 뒤가 파일명임. 확장자도 붙어야 함
//            exampleFile, // 실제 저장될 파일
//            { result -> Log.d("MyAmplifyApp", "Successfully uploaded: " + result) },
//            { error -> Log.d("MyAmplifyApp", "Upload failed", error) }
//        )

}