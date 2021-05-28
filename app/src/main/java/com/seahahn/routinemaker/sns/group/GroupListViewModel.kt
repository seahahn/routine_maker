package com.seahahn.routinemaker.sns.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.seahahn.routinemaker.sns.GroupData

class GroupListViewModel : ViewModel(){

    private val mutableGroupData = MutableLiveData<MutableList<GroupData>>()
    val gottenGroupData: LiveData<MutableList<GroupData>> get() = mutableGroupData

    fun setList(data: MutableList<GroupData>) {
        mutableGroupData.value = data
    }
}