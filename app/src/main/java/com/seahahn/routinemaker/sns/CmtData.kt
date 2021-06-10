package com.seahahn.routinemaker.sns

import java.io.Serializable

/*
* 뉴스피드 댓글 데이터 클래스
* id는 DB에서의 댓글 고유번호(Primary Key)
* writerId는 댓글을 작성한 사용자의 DB 내 고유 번호
* feedId는 댓글이 작성된 피드의 DB 내 고유 번호
* content는 댓글에 작성된 텍스트 내용
* image는 댓글에 포함된 이미지의 S3내 URL 경로 모음
* isSub는 댓글이 그냥 댓글인지 대댓글인지의 여부
* mainCmt는 대댓글일 경우 이 대댓글이 달린 댓글의 고유 번호
* createdAt은 댓글 생성 시점
*/
data class CmtData(
    val id : Int,
    val writerId : Int,
    val feedId : Int,
    var content : String,
    var image : String,
    val isSub : Boolean,
    val mainCmt : Int,
    val createdAt : String
) : Serializable