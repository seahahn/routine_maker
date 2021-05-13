package com.seahahn.routinemaker.main

data class ActionData(
    val id : Int,
    val actionTitle : String,
    val time : String,
    val memo : String,
    val mDate : String,
    val rtId : Int,
    val userId : Int,
    val done : Int,
    val pos : Int,
    val createdAt : String
)
