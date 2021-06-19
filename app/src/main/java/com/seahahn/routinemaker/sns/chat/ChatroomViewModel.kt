package com.seahahn.routinemaker.sns.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.ChatroomData

class ChatroomViewModel : ViewModel(){

    private val mutableChatroomData = MutableLiveData<ChatroomData>()
    val gottenChatroomData: LiveData<ChatroomData> get() = mutableChatroomData

    fun setData(data: ChatroomData) {
        mutableChatroomData.value = data
    }
}