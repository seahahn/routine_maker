package com.seahahn.routinemaker.util

import android.content.Context
import android.content.SharedPreferences

object AppVar {
    private val MY_ACCOUNT : String = "account"

    // 사용자가 선택한 날짜의 값을 문자열로 저장
    fun setSelectedDate(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("SELECETD_DATE", input)
        editor.commit()
    }

    fun getSelectedDate(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("SELECETD_DATE", "").toString()
    }

    // 사용자가 선택한 날짜의 값이 과거인지 아닌지를 저장
    fun setDatePast(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("DATE_PAST", input)
        editor.commit()
    }

    fun getDatePast(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("DATE_PAST", false)
    }
}