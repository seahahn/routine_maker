package com.seahahn.routinemaker.notice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main

class NoticeActivity : Main() {

    private val TAG = this::class.java.simpleName
    private var webView : WebView? = null //웹뷰
    private var webSetting : WebSettings? = null //웹뷰세팅

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        val title = findViewById<TextView>(R.id.toolbarTitle) // 상단 툴바 제목
        val titleText = getString(R.string.notice) // 툴바 제목에 들어갈 텍스트
        initToolbar(title, titleText, 1) // 툴바 세팅하기

        val url = intent.getStringExtra("url")
        Log.d(TAG, "url : $url")
        webView = findViewById<WebView>(R.id.webview)
        webView?.setWebViewClient(WebViewClient()) // 클릭시 새창 안뜨게 막아준다
        webSetting = webView?.getSettings()
        webSetting?.setJavaScriptEnabled(true) // 자바스크립트 사용을 허용한다
        webSetting?.setDomStorageEnabled(true) // 로컬저장소에 허용할지 여부를 판별하는 메소드
        webView?.loadUrl(url!!)
    }


}