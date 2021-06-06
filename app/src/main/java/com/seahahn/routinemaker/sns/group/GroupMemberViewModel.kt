package com.seahahn.routinemaker.sns.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData

class GroupMemberViewModel : ViewModel(){

    private val mutableGroupMemberData = MutableLiveData<MutableList<GroupMemberData>>()
    val gottenGroupMemberData: LiveData<MutableList<GroupMemberData>> get() = mutableGroupMemberData

    private val newGroupLeader = MutableLiveData<Int>()
    val gottenNewGroupLeader: LiveData<Int> get() = newGroupLeader

    fun setList(data: MutableList<GroupMemberData>) {
        mutableGroupMemberData.value = data
    }

    fun setNewLeaderId(data: Int) {
        newGroupLeader.value = data
    }
}