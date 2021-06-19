package com.seahahn.routinemaker.sns

data class ChatroomData(
    val id : Int,
    val isGroupchat: Boolean,
    val userId: Int,
    val audienceId: Int,
    var memberList : String,
    val createdAt : String
)
