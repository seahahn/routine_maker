package com.seahahn.routinemaker.network

import com.seahahn.routinemaker.util.NotificationContents
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmService {

    @Headers("Authorization: key=AAAAxBTtrNk:APA91bHulhtjaswBGKWngPPDh4OIs1hvZSNpRGjqL8_zPPRu7wm6qZPDAzIpbvTKoG7QXcyNP1htVsCBvf0wFBwGbfHnwdqR4isHAgoj8O-OvFQC2Bjhzc8J3_7RcVU4MlqtmfeXTJsC")
    @POST("fcm/send") // FCM 발송 요청 후 결과 받기
    fun sendFCMNotification(
        @Body notification: NotificationContents
    ) : Call<ResponseBody>
}