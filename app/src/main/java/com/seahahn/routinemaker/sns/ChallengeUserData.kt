package com.seahahn.routinemaker.sns
/*
* 챌린지 참여자 데이터 클래스
*/
data class ChallengeUserData(
    val id : Int,
    val challengeId : Int,
    val userId : Int,
    val createdAt : String
)
