package com.seahahn.routinemaker.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateViewModel : ViewModel() {

    private val mutableDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = mutableDate

    fun selectDate(date: String) {
        mutableDate.value = date
    }

}