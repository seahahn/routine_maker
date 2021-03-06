package com.seahahn.routinemaker.sns.newsfeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seahahn.routinemaker.sns.FeedData

class GroupFeedViewModel : ViewModel(){

    private val mutableFeedData = MutableLiveData<MutableList<FeedData>>()
    val gottenFeedData: LiveData<MutableList<FeedData>> get() = mutableFeedData

    fun setList(data: MutableList<FeedData>) {
        mutableFeedData.value = data
    }
}