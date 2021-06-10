package com.seahahn.routinemaker.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.main.RtData
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData
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

    @FormUrlEncoded
    @POST("/api/main/make_rt.php") // 루틴 또는 할 일 만들기 액티비티에서 데이터 보내 DB에 저장하기
    fun makeRt(
        @Field("m_type") mType: String,
        @Field("title") title: String,
        @Field("m_days") mDays: String,
        @Field("alarm") alarm: Boolean,
        @Field("m_date") date: String,
        @Field("m_time") time: String,
        @Field("on_feed") onFeed: Boolean,
        @Field("memo") memo: String,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/update_rt.php") // 루틴 또는 할 일 만들기 액티비티에서 데이터 보내 DB에 저장하기
    fun updateRt(
        @Field("id") id: Int,
        @Field("title") title: String,
        @Field("m_days") mDays: String,
        @Field("alarm") alarm: Boolean,
        @Field("m_date") date: String,
        @Field("m_time") time: String,
        @Field("on_feed") onFeed: Boolean,
        @Field("memo") memo: String
//        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @GET("/api/main/get_rts.php") // 사용자 고유 번호를 이용하여 루틴 및 할 일 목록 가져오기
    fun getRts(
        @Query("user_id") userId: Int,
        @Query("past") past: Boolean
    ) : Call<MutableList<RtData>>

    @GET("/api/main/get_rt.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 또는 할 일의 정보 가져오기
    fun getRt(
        @Query("id") rtId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/delete_rt.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 또는 할 일 삭제하기
    fun deleteRt(
        @Field("id") rtId: Int
    ) : Call<JsonObject>

    @GET("/api/main/done_rt.php") // 루틴 또는 할 일의 고유 번호를 이용하여
    fun doneRt(
        @Query("id") rtId: Int,
        @Query("done") done: Int, // 완료 후 설정할 상태에 대한 값
        @Query("m_date") mDate: String,
        @Query("done_date") doneDate: String // 루틴을 완료한 날짜(할 일에는 안 쓰임)
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/set_review.php") // 루틴 또는 할 일 만들기 액티비티에서 데이터 보내 DB에 저장하기
    fun setReview(
        @Field("content") content: String,
        @Field("on_public") onPublic: Boolean,
        @Field("m_date") mDate: String,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @GET("/api/main/get_review.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 또는 할 일 삭제하기
    fun getReview(
        @Query("m_date") mDate: String,
        @Query("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/make_action.php") // 루틴 내 행동 추가하기 액티비티에서 데이터 보내 DB에 저장하기
    fun makeAction(
        @Field("title") title: String,
        @Field("m_time") time: String,
        @Field("memo") memo: String,
        @Field("rt_id") rtId: Int,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/update_action.php") // 루틴 내 행동 추가하기 액티비티에서 데이터 보내 DB에 저장하기
    fun updateAction(
        @Field("id") id: Int,
        @Field("title") title: String,
        @Field("m_time") time: String,
        @Field("memo") memo: String
    ) : Call<JsonObject>

    @GET("/api/main/get_actions.php") // 사용자 고유 번호를 이용하여 루틴 내 행동 목록 가져오기
    fun getActions(
        @Query("rt_id") rtId: Int,
        @Query("user_id") userId: Int,
        @Query("past") past: Boolean,
        @Query("done_day") doneDay: String
    ) : Call<MutableList<ActionData>>

    @GET("/api/main/get_action.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동의 정보 가져오기
    fun getAction(
        @Query("id") actionId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/delete_action.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun deleteAction(
        @Field("id") actionId: Int
    ) : Call<JsonObject>

    @GET("/api/main/done_action.php") // 루틴 내 행동의 고유 번호를 이용하여 완료 처리하기
    fun doneAction(
        @Query("id") actionId: Int,
        @Query("done") done: Int, // 완료 후 설정할 상태에 대한 값
        @Query("m_date") mDate: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/main/change_action_pos.php") // 루틴 내 행동 추가하기 액티비티에서 데이터 보내 DB에 저장하기
    fun chageActionPos(
        @Field("m_date") mDate: String,
        @Field("id_moved") actionMoved: Int,
        @Field("id_pushed") actionPushed: Int,
        @Field("pos_moved") posMoved: Int,
        @Field("pos_pushed") posPushed: Int
    ) : Call<JsonObject>

    @GET("/api/stts/get_rt_records.php") // 사용자 고유 번호를 이용하여 루틴 과거 수행 내역 가져오기
    fun getRtRecords(
        @Query("user_id") userId: Int
    ) : Call<MutableList<RtData>>

    @GET("/api/stts/get_action_records.php") // 사용자 고유 번호를 이용하여 루틴 내 행동 목록 가져오기
    fun getActionRecords(
        @Query("user_id") userId: Int
    ) : Call<MutableList<ActionData>>

    @FormUrlEncoded
    @POST("/api/sns/make_group.php") // 그룹 만들기 액티비티에서 데이터 보내 DB에 저장하기
    fun makeGroup(
        @Field("title") title: String,
        @Field("tags") tags: String,
        @Field("head_limit") headLimit: Int,
        @Field("on_public") onPublic: Boolean,
        @Field("memo") memo: String,
        @Field("leader_id") leaderId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/update_group.php") // 루틴 또는 할 일 만들기 액티비티에서 데이터 보내 DB에 저장하기
    fun updateGroup(
        @Field("id") id: Int,
        @Field("title") title: String,
        @Field("tags") tags: String,
        @Field("head_limit") headLimit: Int,
        @Field("on_public") onPublic: Boolean,
        @Field("memo") memo: String
    ) : Call<JsonObject>

    @GET("/api/sns/get_groups.php") // 전체 그룹 목록 가져오기
    fun getGroups(
        @Query("user_id") userId: Int
    ) : Call<MutableList<GroupData>>

    @GET("/api/sns/get_group_members.php") // 전체 그룹 목록 가져오기
    fun getGroupMembers(
        @Query("group_id") groupId: Int,
        @Query("joined") joined: Boolean
    ) : Call<MutableList<GroupMemberData>>

    @FormUrlEncoded
    @POST("/api/sns/set_group_leader.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun setGroupLeader(
        @Field("group_id") groupId: Int,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @GET("/api/sns/get_group.php") // 그룹의 고유 번호를 이용하여 해당 그룹의 정보 가져오기
    fun getGroup(
        @Query("id") groupId: Int,
        @Query("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/delete_group.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun deleteGroup(
        @Field("id") groupId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/join_group.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun joinGroup(
        @Field("group_id") groupId: Int,
        @Field("user_id") userId: Int,
        @Field("joined") joined: Boolean
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/accept_join_group.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun acceptJoinGroup(
        @Field("group_id") groupId: Int,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/quit_group.php") // 루틴 또는 할 일의 고유 번호를 이용하여 해당 루틴 내 행동 삭제하기
    fun quitGroup(
        @Field("group_id") groupId: Int,
        @Field("user_id") userId: Int
    ) : Call<JsonObject>

    @GET("/api/sns/get_user_data.php") // 사용자의 닉네임, 프로필 사진 URL 등의 정보를 가져옴
    fun getUserData(
        @Query("user_id") id: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/make_feed.php") // 그룹 피드 작성 액티비티에서 데이터 보내 DB에 저장하기
    fun makeFeed(
        @Field("writer_id") writerId: Int,
        @Field("content") content: String,
        @Field("images") images: String,
        @Field("group_id") groupId: Int,
        @Field("challenge_id") challengeId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/update_feed.php") // 그룹 피드 수정 액티비티에서 데이터 보내 DB에 저장하기
    fun updateFeed(
        @Field("id") feedId: Int,
        @Field("content") content: String,
        @Field("images") images: String
    ) : Call<JsonObject>

    @GET("/api/sns/get_feeds.php") // 선택한 그룹의 피드 목록 가져오기
    fun getFeeds(
        @Query("group_id") groupId: Int,
        @Query("user_id") userId: Int
    ) : Call<MutableList<FeedData>>

    @GET("/api/sns/get_feed.php") // 그룹 피드의 고유 번호를 이용하여 해당 그룹 피드의 정보 가져오기
    fun getFeed(
        @Query("id") feedId: Int,
        @Query("user_id") userId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/delete_feed.php") // 그룹 피드의 고유 번호를 이용하여 해당 피드 삭제하기
    fun deleteFeed(
        @Field("id") feedId: Int
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/set_feed_like.php") // 그룹 피드의 고유 번호를 이용하여 해당 피드 삭제하기
    fun setFeedLike(
        @Field("feed_id") feedId: Int,
        @Field("writer_id") writerId: Int,
        @Field("is_liked") isLiked: Boolean
    ) : Call<JsonObject>

    @GET("/api/sns/get_cmts.php") // 선택한 그룹의 피드 목록 가져오기
    fun getCmts(
        @Query("feed_id") feedId: Int
    ) : Call<MutableList<CmtData>>

    @FormUrlEncoded
    @POST("/api/sns/make_cmt.php") // 그룹 피드 디테일 액티비티에서 댓글 작성 시 데이터 보내 DB에 저장하기
    fun makeCmt(
        @Field("writer_id") writerId: Int,
        @Field("feed_id") feedId: Int,
        @Field("content") content: String,
        @Field("image") image: String,
        @Field("is_sub") isSub: Boolean,
        @Field("main_cmt") mainCmt: Int?
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/update_cmt.php") // 그룹 피드 디테일 액티비티에서 댓글 수정하기
    fun updateCmt(
        @Field("id") cmtId: Int,
        @Field("content") content: String
    ) : Call<JsonObject>

    @FormUrlEncoded
    @POST("/api/sns/delete_cmt.php") // 그룹 피드 댓글의 고유 번호를 이용하여 해당 댓글 삭제하기
    fun deleteCmt(
        @Field("id") cmtId: Int
    ) : Call<JsonObject>

}
