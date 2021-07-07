package com.seahahn.routinemaker.util

import android.content.Context
import android.content.SharedPreferences

object UserSetting {

    private const val MY_ACCOUNT : String = "account"

    // 메인 액티비티의 루틴 프래그먼트의 루틴(할 일) 목록 리사이클러뷰의 아이템 전체 보기 여부를 저장
    fun setRtListMode(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("RT_MODE", input)
        editor.apply()
    }

    fun getRtListMode(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("RT_MODE", false)
    }
}