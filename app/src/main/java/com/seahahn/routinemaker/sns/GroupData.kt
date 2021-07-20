package com.seahahn.routinemaker.sns
/*
* 그룹 데이터 클래스
* id는 DB에서의 그룹 고유번호(Primary Key)
* title은 그룹명
* tags는 그룹에 설정된 태그들
* headLimit는 인원 제한
* members는 그룹에 가입된 인원들의 사용자 고유 번호 모음(List를 toString하여 저장)
* isLocked는 그룹 공개 여부
* userId는 그룹을 생성한 사용자의 DB 내 고유 번호
* createdAt은 그룹 생성 시점
* */
data class GroupData(
    val id : Int,
    val title : String,
    val tags : String,
    val headLimit : Int,
    val onPublic : Boolean,
    val memo : String,
    val leaderId : Int,
    val joined : Boolean,
    val memberCount : Int,
    val createdAt : String
)
