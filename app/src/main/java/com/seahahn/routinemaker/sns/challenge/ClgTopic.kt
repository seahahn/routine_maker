package com.seahahn.routinemaker.sns.challenge

enum class ClgTopic(val topic: Int) {
    HEALTH(0), // 건강
    EMOTION(1), // 정서
    LIFE(2), // 생활
    COMPETENCE(3), // 역량
    ASSET(4), // 자산
    HOBBY(5), // 취미
    ETC(6); // 그 외

    fun topic() = topic
}