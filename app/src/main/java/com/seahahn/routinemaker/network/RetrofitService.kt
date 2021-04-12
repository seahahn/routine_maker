package com.seahahn.routinemaker.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET

interface RetrofitService {

    @FormUrlEncoded
    @POST("/api/users/signup_ok.php") // 사용자 회원가입 시 정보 보내고 등록 여부 응답 받기
    fun signup(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("nick") nick: String,
        @Field("inway") inway: String,
        @Field("active") active: Int
    ) : Call<JsonObject>

    @GET("/api/users/check_email.php") // 사용자 회원가입 시 이메일 중복 체크
    fun checkEmail(
        @Query("email") email: String
    ) : Call<JsonObject>

    @GET("/api/users/check_nick.php") // 사용자 닉네임 중복 체크(회원가입 시 or 마이페이지 닉네임 변경 시)
    fun checkNick(
        @Query("nick") nick: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/login_ok.php") // 사용자 로그인 시 정보 보내고 성공 여부 응답 받기
    fun login(
        @Field("email") email: String,
        @Field("pw") pw: String
    ) : Call<JsonObject>

    @GET("/api/users/resetpw.php") // 사용자 비밀번호 분실로 인한 재설정 시 이메일 인증하기 위해서 이메일 주소 보내기
    fun resetPw(
        @Query("email") email: String
    ) : Call<JsonObject>

    @GET("/api/users/resetpw_check.php") // 이메일로 보낸 인증번호에 맞게 입력했는지 확인하기 위해 이메일과 해쉬값 보내기
    fun resetPwCheck(
        @Query("email") email: String,
        @Query("hash") hash: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/resetpw_ok.php") // 비밀번호 재설정 위해서 이메일과 비번, 비번 확인 입력 정보 보내기
    fun resetPwOk(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("pwc") pwc: String
    ) : Call<JsonObject>

    // 기존에 이메일과 비밀번호로 가입했는데 구글 또는 네이버로 로그인하려는 경우
    // 이때 사용자가 동의하면 기존 계정과 선택한 구글 또는 네이버 계정을 연동시킴
    @FormUrlEncoded
    @POST("/api/users/sns_con.php")
    fun snsCon(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("inway") inway: String
    ) : Call<JsonObject>

    @GET("/api/users/mypage_info.php") // SharedPreferences에 저장해둔 DB 내 사용자의 고유 번호를 통해 사용자의 정보 불러오기
    fun mypageInfo(
        @Query("id") id: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/change_info.php") // 사용자 로그인 시 정보 보내고 성공 여부 응답 받기
    fun changeInfo(
        @Field("id") id: Int,
        @Field("subject") subject: String,
        @Field("content") content: String
    ) : Call<JsonObject>

}
