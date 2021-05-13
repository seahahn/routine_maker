package com.seahahn.routinemaker.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject

class RtTodoViewModel : ViewModel(){

    private val mutableRtData = MutableLiveData<MutableList<RtData>>()
    val gottenRtData: LiveData<MutableList<RtData>> get() = mutableRtData

    private val mutableRtDataPast = MutableLiveData<MutableList<RtData>>()
    val gottenRtDataPast: LiveData<MutableList<RtData>> get() = mutableRtDataPast

    fun setList(data: MutableList<RtData>) {
        mutableRtData.value = data
    }

    fun setPastList(data: MutableList<RtData>) {
        mutableRtDataPast.value = data
    }

}