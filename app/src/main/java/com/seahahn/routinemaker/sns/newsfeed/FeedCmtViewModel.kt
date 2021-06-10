package com.seahahn.routinemaker.sns.newsfeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.sns.GroupData

class FeedCmtViewModel : ViewModel(){

    private val mutableCmtData = MutableLiveData<MutableList<CmtData>>()
    val gottenCmtData: LiveData<MutableList<CmtData>> get() = mutableCmtData

    fun setList(data: MutableList<CmtData>) {
        mutableCmtData.value = data
    }
}