package com.seahahn.routinemaker.sns.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.ChatData
import com.seahahn.routinemaker.sns.GroupMemberData

class ChatMembersViewModel : ViewModel(){

    private val mutableChatMembersData = MutableLiveData<MutableList<GroupMemberData>>()
    val gottenChatMembers: LiveData<MutableList<GroupMemberData>> get() = mutableChatMembersData

    fun setList(data: MutableList<GroupMemberData>) {
        mutableChatMembersData.value = data
    }
}