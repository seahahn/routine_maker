package com.seahahn.routinemaker.sns

data class ChatData(
    val id : Int,
    val writerId : Int,
    var content : String,
    var contentType : Int,
    val roomId : Int,
    val createdAt : String
)
