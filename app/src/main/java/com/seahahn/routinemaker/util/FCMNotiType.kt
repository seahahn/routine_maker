package com.seahahn.routinemaker.util

enum class FCMNotiType(val type: Int) {
    CHAT(0), CMT(1), SUB_CMT(2), LIKE(3), RT(4);

    fun type() = type
}