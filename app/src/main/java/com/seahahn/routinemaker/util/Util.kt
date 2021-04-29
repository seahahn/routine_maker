package com.seahahn.routinemaker.util

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
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
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.DateViewModel
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import retrofit2.Retrofit

open class Util  : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    lateinit var service : RetrofitService
    private val rfServiceViewModel by viewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델

    open lateinit var drawerLayout : DrawerLayout // 좌측 내비게이션 메뉴가 포함된 액티비티의 경우 DrawerLayout을 포함하고 있음
    var homeBtn : Int = 0

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit(): RetrofitService {
        d(TAG, "initRetrofit")
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)

        rfServiceViewModel.setService(service) // 뷰모델에 레트로핏 서비스 객체 저장하기

        return service
    }

    open fun showAlert(title: String, msg: String, pos: String, neg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { _: DialogInterface, _: Int -> finish() }
            .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
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
        Log.d(TAG, "onOptionsItemSelected")
        when(item.itemId){
            android.R.id.home->{ // 툴바 좌측 버튼
                if(homeBtn == R.drawable.hbgmenu) { // 햄버거 메뉴 버튼일 경우
                    drawerLayout.openDrawer(GravityCompat.START) // 네비게이션 드로어 열기
                } else if(homeBtn == R.drawable.backward_arrow) { // 좌향 화살표일 경우
                    Log.d(TAG, "뒤로 가기")
                    finish() // 액티비티 종료하기
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 액티비티 하단에 꽉 차는 버튼 있는 경우에 해당 버튼의 텍스트 설정하기
    fun setFullBtmBtnText(btn: Button) {
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
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
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