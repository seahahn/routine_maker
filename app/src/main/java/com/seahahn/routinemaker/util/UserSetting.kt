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

//    // 알림 설정에서 전체 알림 설정값을 저장
//    fun setNotiSetAll(context: Context, input: Boolean) {
//        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
//        val editor : SharedPreferences.Editor = prefs.edit()
//        editor.putBoolean("NOTI_ALL", input)
//        editor.apply()
//    }
//
//    fun getNotiSetAll(context: Context): Boolean {
//        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
//        return prefs.getBoolean("NOTI_ALL", true)
//    }

    // 알림 설정에서 루틴 시작 알림 설정값을 저장
    fun setNotiSetRtStart(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("NOTI_RT_START", input)
        editor.apply()
    }

    fun getNotiSetRtStart(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTI_RT_START", true)
    }

    // 알림 설정에서 루틴 미수행 알림 설정값을 저장
    fun setNotiSetRtDoneYet(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("NOTI_RT_DONE_YET", input)
        editor.apply()
    }

    fun getNotiSetRtDoneYet(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTI_RT_DONE_YET", true)
    }

    // 알림 설정에서 SNS 피드 댓글 알림 설정값을 저장
    fun setNotiSetCmt(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("NOTI_CMT", input)
        editor.apply()
    }

    fun getNotiSetCmt(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTI_CMT", true)
    }

    // 알림 설정에서 SNS 피드 좋아요 알림 설정값을 저장
    fun setNotiSetLike(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("NOTI_LIKE", input)
        editor.apply()
    }

    fun getNotiSetLike(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTI_LIKE", true)
    }

    // 알림 설정에서 채팅 알림 설정값을 저장
    fun setNotiSetChat(context: Context, input: Boolean) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("NOTI_CHAT", input)
        editor.apply()
    }

    fun getNotiSetChat(context: Context): Boolean {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getBoolean("NOTI_CHAT", true)
    }
}