package com.seahahn.routinemaker.notice

data class NoticeData(
    val id : Int,
    val receiverId : Int,
    val senderId : Int,
    val type : Int,
    val title : String,
    val body : String,
    val target : Int,
    val createdAt : String
)
