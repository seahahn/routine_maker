package com.seahahn.routinemaker.sns.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.ChatroomData

class ChatroomViewModel : ViewModel(){

    private val mutableChatroomData = MutableLiveData<MutableList<ChatRoom>>()
    val gottenChatroomData: LiveData<MutableList<ChatRoom>> get() = mutableChatroomData

    fun setData(data: MutableList<ChatRoom>) {
        mutableChatroomData.value = data
    }
}