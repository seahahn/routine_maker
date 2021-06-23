package com.seahahn.routinemaker.sns

data class ChatroomData(
    val id : Int,
    val isGroupchat: Boolean,
    val hostId: Int,
    val audienceId: Int,
    val createdAt : String
)
