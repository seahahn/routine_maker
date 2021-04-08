package com.seahahn.routinemaker.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET

interface RetrofitService {
//    @FormUrlEncoded
//    @POST("auth/login_mobile")
//    fun login(
//        @Field("email") email: Editable?,
//        @Field("pw") pw: Editable?
//    ) : Call<JsonObject>

//    @FormUrlEncoded
//    @POST("auth/signup_mobile")
//    fun signup(
//        @Field("email") email: Editable?,
//        @Field("pw") pw: Editable?,
//        @Field("nickname") nickname: Editable?
//    ) : Call<Void>

    @FormUrlEncoded
    @POST("/api/users/signup_ok.php")
    fun signup(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("nick") nick: String,
        @Field("inway") inway: String,
        @Field("active") active: Int
    ) : Call<JsonObject>

    @GET("/api/users/check_email.php")
    fun checkEmail(
        @Query("email") email: String
    ) : Call<JsonObject>

    @GET("/api/users/check_nick.php")
    fun checkNick(
        @Query("nick") nick: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/login_ok.php")
    fun login(
        @Field("email") email: String,
        @Field("pw") pw: String
    ) : Call<JsonObject>

    @GET("/api/users/resetpw.php")
    fun resetPw(
        @Query("email") email: String
    ) : Call<JsonObject>

    @GET("/api/users/resetpw_check.php")
    fun resetPwCheck(
        @Query("hash") hash: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/resetpw_ok.php")
    fun resetPwOk(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("pwc") pwc: String
    ) : Call<JsonObject>

    @GET("/api/users/resetpw_check.php")
    fun naverlogin(
        @Query("hash") hash: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/users/sns_con.php")
    fun sns_con(
        @Field("email") email: String,
        @Field("pw") pw: String,
        @Field("inway") inway: String
    ) : Call<JsonObject>

}
