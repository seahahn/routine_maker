package com.seahahn.routinemaker.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.GroupData

class NoticeListViewModel : ViewModel() {

    private val mutableNoticeData = MutableLiveData<MutableList<NoticeData>>()
    val gottenNoticeData: LiveData<MutableList<NoticeData>> get() = mutableNoticeData

    fun setList(data: MutableList<NoticeData>) {
        mutableNoticeData.value = data
    }
}