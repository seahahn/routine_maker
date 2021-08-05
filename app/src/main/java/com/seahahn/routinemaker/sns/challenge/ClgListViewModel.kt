package com.seahahn.routinemaker.sns.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.ChallengeData

class ClgListViewModel : ViewModel() {

    private val mutableClgData = MutableLiveData<MutableList<ChallengeData>>()
    val gottenClgData: LiveData<MutableList<ChallengeData>> get() = mutableClgData

    fun setList(data: MutableList<ChallengeData>) {
        mutableClgData.value = data
    }
}