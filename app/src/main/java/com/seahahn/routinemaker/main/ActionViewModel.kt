package com.seahahn.routinemaker.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject

class ActionViewModel : ViewModel(){

    private val mutableRtData = MutableLiveData<MutableList<ActionData>>()
    val gottenActionData: LiveData<MutableList<ActionData>> get() = mutableRtData

    fun setList(data: MutableList<ActionData>) {
        mutableRtData.value = data
    }

}