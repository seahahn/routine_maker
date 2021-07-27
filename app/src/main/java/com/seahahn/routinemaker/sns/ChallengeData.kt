package com.seahahn.routinemaker.sns
/*
* 챌린지 데이터 클래스
* id는 DB에서의 챌린지 고유번호(Primary Key)
* title은 챌린지명
* topic은 챌린지 생성 시 선택된 챌린지 주제를 숫자로 표현한 것
* startDate는 챌린지 시작 일자
* endDate는 챌린지 종료 일자
* imageGood은 올바른 챌린지 인증 이미지 예시를 담은 S3 URL
* imageBad는 잘못된 챌린지 인증 이미지 예시를 담은 S3 URL
* memo는 챌린지에 관한 추가 설명
* members는 챌린지에 참여 중인 사용자들의 고유 번호 모음
* userId는 챌린지를 생성한 사용자의 고유 번호
* createdAt은 챌린지 생성 시점
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
    val certTimeStart : Int,
    val certTimeEnd : Int,
    val imageGood : String,
    val imageBad : String,
    val memo : String,
    val userId : Int,
    val createdAt : String
)
