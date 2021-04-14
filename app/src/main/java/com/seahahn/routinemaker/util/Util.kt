package com.seahahn.routinemaker.util

import android.content.DialogInterface
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import retrofit2.Retrofit

open class Util  : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    private lateinit var service : RetrofitService

    open lateinit var drawerLayout : DrawerLayout // 좌측 내비게이션 메뉴가 포함된 액티비티의 경우 DrawerLayout을 포함하고 있음
    var homeBtn : Int = 0

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit(): RetrofitService {
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)

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

    // 툴바 버튼 클릭 시 작동할 기능
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

}