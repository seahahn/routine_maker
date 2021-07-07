package com.seahahn.routinemaker.sns.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatContentsViewModel : ViewModel(){

    private val mutableChatContentsData = MutableLiveData<MutableList<ChatMsg>>()
    val gottenChatMsg: LiveData<MutableList<ChatMsg>> get() = mutableChatContentsData

    fun setList(data: MutableList<ChatMsg>) {
        mutableChatContentsData.value = data
    }
}