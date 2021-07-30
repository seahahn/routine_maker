package com.seahahn.routinemaker.sns

data class ChatData(
    val id : Int,
    val writerId : Int,
    val content : String,
    val contentType : Int,
    val roomId : Int,
    val createdAt : String
)
