package com.seahahn.routinemaker.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActionViewModel : ViewModel(){

    private val mutableRtData = MutableLiveData<MutableList<ActionData>>()
    val gottenActionData: LiveData<MutableList<ActionData>> get() = mutableRtData

//    private val mutableRtDataPast = MutableLiveData<MutableList<ActionData>>()
//    val gottenActionDataPast: LiveData<MutableList<ActionData>> get() = mutableRtDataPast

    fun setList(data: MutableList<ActionData>) {
        mutableRtData.value = data
    }

//    fun setListPast(data: MutableList<ActionData>) {
//        mutableRtDataPast.value = data
//    }

}