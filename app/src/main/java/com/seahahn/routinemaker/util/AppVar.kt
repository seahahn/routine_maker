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

    // 사용자가 선택한 다음 그룹 리더의 고유 번호를 임시 저장
    fun setNextLeaderId(context: Context, input: Int) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putInt("NEW_LEADER", input)
        editor.commit()
    }

    fun getNextLeaderId(context: Context): Int {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getInt("NEW_LEADER", 0)
    }

    // 사용자가 선택한 그룹 가입 신청자 목록을 임시 저장
    fun setAcceptedList(context: Context, input: MutableList<Int>) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("ACCEPTED_LIST", input.toString())
        editor.commit()
    }

    fun getAcceptedList(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("ACCEPTED_LIST", "").toString()
    }

    // 그룹 피드에서 사용자가 보고 있는 피드 이미지의 포지션 값 임시 저장
    fun setPagerPos(context: Context, input: Int) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putInt("PAGER_POS", input)
        editor.commit()
    }

    fun getPagerPos(context: Context): Int {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getInt("PAGER_POS", 0)
    }

    // 사용자가 선택한 다른 사용자의 프로필 방문 시 해당 사용자의 고유 번호 임시 저장
    fun setOtherUserId(context: Context, input: Int) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putInt("OTHER_USER_ID", input)
        editor.commit()
    }

    fun getOtherUserId(context: Context): Int {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getInt("OTHER_USER_ID", 0)
    }

    // 사용자가 선택한 다른 사용자의 프로필 방문 시 해당 사용자의 고유 번호 임시 저장
    fun setOtherUserNick(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("OTHER_USER_NICK", input)
        editor.commit()
    }

    fun getOtherUserNick(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("OTHER_USER_NICK", "").toString()
    }
}