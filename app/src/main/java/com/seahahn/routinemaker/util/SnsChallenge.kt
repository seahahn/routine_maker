package com.seahahn.routinemaker.util

import android.content.Context
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChallengeData
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.sns.challenge.ChallengeMakeActivity
import com.seahahn.routinemaker.sns.challenge.ClgListViewModel
import com.seahahn.routinemaker.sns.challenge.ClgTopic
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedMakeActivity
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedViewModel
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


open class SnsChallenge : Sns() {

    private val TAG = this::class.java.simpleName

    val clgListViewModel by viewModels<ClgListViewModel>() // 챌린지 목록 데이터

    lateinit var clgTitleInput: TextInputEditText // 챌린지명 입력란
    lateinit var clgTitle: Editable // 챌린지명 입력 결과
    lateinit var clgTitleTxt: TextView // 챌린지명 표시(챌린지 정보 액티비티에서 사용)

    lateinit var clgTopicTv: TextView // 챌린지 주제 표시
    var clgTopic: Int = 0 // 챌린지 주제

    lateinit var clgStartDate: TextView // 챌린지 시작일
    lateinit var clgPeriod: TextView // 챌린지 기간(단위 = 주)
    lateinit var certFrequency: TextView // 챌린지 인증 빈도(주 1회, 주 5회 등)
    lateinit var certDays: MultiSelectToggleGroup // 챌린지 수행 가능 요일
    lateinit var certNum: TextView // 챌린지 하루 인증 횟수
    lateinit var certTimeStart: TextView // 챌린지 인증 가능 시작 시각
    lateinit var certTimeEnd: TextView // 챌린지 인증 가능 종료 시각

    lateinit var certImageGood: ImageView // 챌린지 올바른 인증샷
    lateinit var certImageBad: ImageView // 챌린지 잘못된 인증샷
    lateinit var certImageGoodURL: String // 챌린지 올바른 인증샷 URL
    lateinit var certImageBadURL: String // 챌린지 잘못된 인증샷 URL

    lateinit var clgMemo: EditText // 챌린지 추가 설명 입력란
    lateinit var clgMemoTxt: Editable // 챌린지 추가 설명 입력 결과
    var clgHostId: Int = 0 // 챌린지 생성자 고유 번호
    var clgId: Int = 0 // 챌린지 고유 번호

    // 챌린지 만들기, 수정하기 액티비티 내의 공통 요소들 초기화하기
    fun initClgActivity(btmBtnId : Int) {

    }

    // FAB 버튼 초기화하기
    fun initFABinSNSClg() {
        fabtn = findViewById(R.id.fabtn)
        fabMain = findViewById(R.id.fabMain)
        fabMain.setOnClickListener {
            startActivity<ChallengeMakeActivity>("groupId" to groupId) // '챌린지 만들기' 액티비티로 이동
        }
    }

    // 챌린지 생성하기
    fun makeClg(service : RetrofitService, clgTitle: String, clgTopic: Int, clgStartDate: String, clgPeriod: Int, certFrequency: Int, certDays: String, certNum: String, certTimeStart: String, certTimeEnd: String, certImageGood: String, certImageBad: String, clgMemo: String, groupId: Int) {
        Logger.d(TAG, "makeClg 변수들 : $clgTitle, $clgTopic, $clgStartDate, $clgPeriod, $certFrequency, $certDays, $certNum, $certTimeStart, $certTimeEnd, $certImageGood, $certImageBad, $clgMemo, $groupId")
        val clgHostId = UserInfo.getUserId(applicationContext)
        service.makeClg(clgTitle, clgTopic, clgStartDate, clgPeriod, certFrequency, certDays, certNum, certTimeStart, certTimeEnd, certImageGood, certImageBad, clgMemo, groupId, clgHostId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "챌린지 생성 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "챌린지 생성 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        toast(msg)
                        finish()
                    }
                    false -> toast(msg)
                }
            }
        })
    }

    // 챌린지 수정하기
    fun updateClg(service : RetrofitService, clgId: Int, clgTitle: String, clgTopic: Int, clgStartDate: String, clgPeriod: Int, certFrequency: Int, certDays: String, certNum: String, certTimeStart: String, certTimeEnd: String, certImageGood: String, certImageBad: String, clgMemo: String) {
        Logger.d(TAG, "updateClg 변수들 : $clgId, $clgTitle, $clgTopic, $clgStartDate, $clgPeriod, $certFrequency, $certDays, $certNum, $certTimeStart, $certTimeEnd, $certImageGood, $certImageBad, $clgMemo")
        service.updateClg(clgId, clgTitle, clgTopic, clgStartDate, clgPeriod, certFrequency, certDays, certNum, certTimeStart, certTimeEnd, certImageGood, certImageBad, clgMemo).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "챌린지 정보 수정 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "챌린지 정보 수정 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                Log.d(TAG, response.body().toString())
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        toast(msg)
                        finish()
                    }
                    false -> toast(msg)
                }
            }
        })
    }

    // 챌린지 삭제하기
    fun deleteClg(service: RetrofitService, clgId: Int, groupId: Int, context : Context) {
        Log.d(TAG, "deleteClg 변수들 : $clgId")
        service.deleteClg(clgId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "챌린지 삭제 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "챌린지 삭제 요청 응답 수신 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                when(result) {
                    true -> {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        getClgs(service, groupId)
                    }
                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // 챌린지 데이터 가져오기
    fun getClg(service: RetrofitService, clgId : Int) {
        Log.d(TAG, "getClg 변수들 : $clgId")
        val userId = UserInfo.getUserId(applicationContext)
        service.getClg(clgId, userId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "챌린지 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "챌린지 데이터 가져오기 요청 응답 수신 성공")
                Log.d(TAG, "getClg : " + response.body().toString())
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                content.setText(gson.get("content").asString)
                imagesURL = gson.get("images").asString

                // 챌린지 제목 표시하기
                clgTitleInput.setText(gson.get("title").asString)
                clgTitle = clgTitleInput.text!!
                clgTitleTxt.text = gson.get("title").asString

                // 챌린지 주제 표시
                clgTopic = gson.get("topic").asInt
                clgTopicTv.text = showClgTopic(clgTopic)

                // 챌린지 시작일, 기간 및 인증 빈도 표시
                clgStartDate.text = gson.get("startDate").asString
                clgPeriod.text = gson.get("period").asString
                certFrequency.text = gson.get("frequency").asString

                // 챌린지 수행 요일 표시
                val days = gson.get("certDays").asString.replace(" ", "").toMutableList()
                for(i in 0 until days.size) {
                    var day = ""
                    when(days[i].toString()) {
                        getString(R.string.sunday) -> day = "sun"
                        getString(R.string.monday) -> day = "mon"
                        getString(R.string.tuesday) -> day = "tue"
                        getString(R.string.wednesday) -> day = "wed"
                        getString(R.string.thursday) -> day = "thu"
                        getString(R.string.friday) -> day = "fri"
                        getString(R.string.saturday) -> day = "sat"
                    }
                    certDays.check(resources.getIdentifier(day, "id", packageName))
                }

                // 챌린지 하루 수행 횟수 및 인증 가능 시작 시각과 종료 시각 표시
                certNum.text = gson.get("certNum").asString
                certTimeStart.text = gson.get("certTimeStart").asString
                certTimeEnd.text = gson.get("certTimeEnd").asString

                // 이미지 URL 이용하여 이미지뷰에 이미지 넣기
                certImageGoodURL = gson.get("imageGood").asString
                certImageBadURL = gson.get("imageBad").asString
                setImgByGlide(applicationContext, certImageGoodURL, certImageGood)
                setImgByGlide(applicationContext, certImageBadURL, certImageBad)

                // 챌린지 추가 설명 표시
                clgMemo.setText(gson.get("memo").asString)
                clgMemoTxt = clgMemo.text

                // 챌린지가 속한 그룹 및 생성자 고유 번호 불러오기
                groupId = gson.get("groupId").asInt
                clgHostId = gson.get("hostId").asInt
            }
        })
    }

    // 챌린지 주제 번호에 해당하는 텍스트를 반환함(예시 : 0 -> 건강)
    fun showClgTopic(topic : Int) : String {
        return when (topic) {
            ClgTopic.HEALTH.topic() -> getString(R.string.topicHealth)
            ClgTopic.EMOTION.topic() -> getString(R.string.topicEmotion)
            ClgTopic.LIFE.topic() -> getString(R.string.topicLife)
            ClgTopic.COMPETENCE.topic() -> getString(R.string.topicCompetence)
            ClgTopic.ASSET.topic() -> getString(R.string.topicAsset)
            ClgTopic.HOBBY.topic() -> getString(R.string.topicHobby)
            ClgTopic.ETC.topic() -> getString(R.string.topicEtc)
            else -> getString(R.string.topicError)
        }
    }

    // 챌린지 목록 불러오기
    fun getClgs(service: RetrofitService, groupId : Int) {
        Logger.d(TAG, "getClgs 변수들 : $groupId")
        service.getClgs(groupId).enqueue(object : Callback<MutableList<ChallengeData>> {
            override fun onFailure(call: Call<MutableList<ChallengeData>>, t: Throwable) {
                Log.d(TAG, "챌린지 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<ChallengeData>>, response: Response<MutableList<ChallengeData>>) {
                Log.d(TAG, "챌린지 목록 가져오기 요청 응답 수신 성공")
                val clgData = response.body()
                try {
                    clgListViewModel.setList(clgData!!)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "error : $e")
                }
            }
        })
    }

}