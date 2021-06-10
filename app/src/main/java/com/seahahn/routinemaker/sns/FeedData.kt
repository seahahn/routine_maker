package com.seahahn.routinemaker.sns

import java.io.Serializable

/*
* 뉴스피드 데이터 클래스
* id는 DB에서의 피드 고유번호(Primary Key)
* userId는 피드를 작성한 사용자의 DB 내 고유 번호
* content는 피드에 작성된 텍스트 내용
* images는 피드에 포함된 이미지들의 S3내 URL 경로 모음(List를 toString하여 저장)
* groupId는 피드가 속한 그룹의 고유 번호
* challengeId는 피드가 속한 챌린지의 고유 번호(챌린지 피드인 경우)
* createdAt은 피드 생성 시점
* */
data class FeedData(
    val id : Int,
    val writerId : Int,
    var content : String,
    var images : String,
    val groupId : Int,
    val challengeId : Int,
    val createdAt : String,
    val likeCount : Int,
    val commentCount : Int,
    val liked : Boolean,
    val cmt : Boolean
) : Serializable