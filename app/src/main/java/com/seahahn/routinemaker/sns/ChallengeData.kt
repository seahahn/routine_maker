package com.seahahn.routinemaker.sns
/*
* 챌린지 데이터 클래스
*/
data class ChallengeData(
    val id : Int,
    val title : String,
    val topic : Int,
    val startDate : String,
    val period : Int,
    val frequency : Int,
    val certDays : Int,
    val certNum : Int,
    val certTimeStart : String,
    val certTimeEnd : String,
    val imageGood : String,
    val imageBad : String,
    val memo : String,
    val groupId : Int,
    val hostId : Int,
    val createdAt : String
)
