package com.seahahn.routinemaker.sns.newsfeed

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager2.widget.ViewPager2
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import com.seahahn.routinemaker.util.UserInfo.getUserNick
import com.seahahn.routinemaker.util.UserInfo.getUserPhoto
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

/*
* 피드 작성
*/
class GroupFeedMakeActivity : Sns() {

    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_feed_make)

        // 레트로핏 통신 연결
        service = initRetrofit()

        groupId = intent.getIntExtra("groupId", 0) // DB 내 그룹의 고유 번호 받기
        challengeId = intent.getIntExtra("challengeId", 0) // DB 내 그룹 챌린지의 고유 번호 받기

        title = findViewById(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.makeFeedTitle) // 툴바 제목에 들어갈 텍스트. 루틴 제목을 가져옴
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        initFeedActivity(R.id.makeFeed) // 액티비티 구성 요소 초기화하기
        addImg.tag = 5 // 최대 업로드 가능한 사진 개수
        addImg.setOnClickListener(ImgClickListener()) // 이미지 추가 아이콘 클릭 시 동작할 내용

        // 추가된 이미지 보여줄 뷰페이저 초기화
        feedImgAdapter = FeedImgAdapter() // 어댑터 초기화
        mViewPager.adapter = feedImgAdapter // 어댑터 연결
        feedImgAdapter.isChangableActivity(true)
//        mViewPager.registerOnPageChangeCallback(MyOnPageChangeCallback())

        // 사용자의 프로필 사진과 닉네임 표시하기
        setUserNickAndPhoto(getUserNick(this), getUserPhoto(this))
    }

    inner class MyOnPageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            feedImgAdapter.notifyDataSetChanged()
        }
    }
}