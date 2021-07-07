package com.seahahn.routinemaker.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RetrofitServiceViewModel : ViewModel() {

    private val mutableRFService = MutableLiveData<RetrofitService>()
    val rfService: LiveData<RetrofitService> get() = mutableRFService

    fun setService(service: RetrofitService) {
        mutableRFService.value = service
    }
}