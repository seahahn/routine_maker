package com.seahahn.routinemaker.util

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import retrofit2.Retrofit

open class Util  : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private lateinit var retrofit : Retrofit
    private lateinit var service : RetrofitService

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

}