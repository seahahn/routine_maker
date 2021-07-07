package com.seahahn.routinemaker.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RtDoneViewModel : ViewModel(){

    private val mutableRtDataPast = MutableLiveData<MutableList<RtData>>()
    val gottenRtDataPast: LiveData<MutableList<RtData>> get() = mutableRtDataPast

    fun setPastList(data: MutableList<RtData>) {
        mutableRtDataPast.value = data
    }

}