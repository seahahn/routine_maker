package com.seahahn.routinemaker.util

import android.content.Context
import android.content.SharedPreferences

object UserInfo {

    private val MY_ACCOUNT : String = "account"

    fun setUserId(context: Context, input: Int) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putInt("MY_ID", input)
        editor.commit()
    }

    fun getUserId(context: Context): Int {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getInt("MY_ID", 0)
    }

    fun setUserEmail(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("MY_EMAIL", input)
        editor.commit()
    }

    fun getUserEmail(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("MY_EMAIL", "").toString()
    }

    fun setUserNick(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("MY_NICK", input)
        editor.commit()
    }

    fun getUserNick(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("MY_NICK", "").toString()
    }

    fun setUserMbs(context: Context, input: Int) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putInt("MY_MBS", input)
        editor.commit()
    }

    fun getUserMbs(context: Context): Int {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getInt("MY_MBS", 0)
    }

    fun setUserPhoto(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("MY_PHOTO", input)
        editor.commit()
    }

    fun getUserPhoto(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("MY_PHOTO", "").toString()
    }

    fun setUserPass(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("MY_PASS", input)
        editor.commit()
    }

    fun getUserPass(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("MY_PASS", "").toString()
    }

    fun setUserInway(context: Context, input: String) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.putString("MY_INWAY", input)
        editor.commit()
    }

    fun getUserInway(context: Context): String {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString("MY_INWAY", "").toString()
    }

    fun clearUser(context: Context) {
        val prefs : SharedPreferences = context.getSharedPreferences(MY_ACCOUNT, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.clear()
        editor.commit()
    }

}