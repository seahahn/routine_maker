package com.seahahn.routinemaker.main
/*
* 루틴과 할 일 데이터 클래스
* id는 DB에서의 루틴 또는 할 일의 고유번호(Primary Key)
* type에서 rt와 todo로 루틴과 할 일을 구분함
* rtTitle은 루틴 또는 할 일의 제목
* days는 반복할 요일들
* alarm은 알람 활성화 여부
* time은 수행 예정 시각
* onFeed는 사용자가 속한 그룹의 뉴스피드에 올릴지 말지 여부
* memo는 루틴 또는 할 일에 포함된 메모의 내용
* userId는 루틴 또는 할 일을 생성한 사용자의 DB 내 고유 번호
* createdAt은 생성 시점. 이를 이용하여 생성 시점 이전에는 루틴 또는 할 일이 생성되지 않게 함
* */
data class RtData(
    val id : Int,
    val mType : String,
    val rtTitle : String,
    val mDays : String,
    val alarm : Boolean,
    val date : String,
    val time : String,
    val onFeed : Boolean,
    val memo : String,
    val userId : Int,
    val createdAt : String,
)
