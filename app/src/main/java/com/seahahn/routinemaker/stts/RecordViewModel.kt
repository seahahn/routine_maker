package com.seahahn.routinemaker.stts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.main.RtData

class RecordViewModel : ViewModel(){

    private val mutableRtData = MutableLiveData<MutableList<RtData>>()
    val recordRtData: LiveData<MutableList<RtData>> get() = mutableRtData

    private val mutableActionData = MutableLiveData<MutableList<ActionData>>()
    val recordActionPast: LiveData<MutableList<ActionData>> get() = mutableActionData

    fun setRtRecordList(data: MutableList<RtData>) {
        mutableRtData.value = data
    }

    fun setActionRecordList(data: MutableList<ActionData>) {
        mutableActionData.value = data
    }

}