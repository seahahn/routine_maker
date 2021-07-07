package com.seahahn.routinemaker.util

data class NotificationData(
    val senderId: Int,
    val type: Int,
    val title: String,
    val body: String,
    val target: Int
)
