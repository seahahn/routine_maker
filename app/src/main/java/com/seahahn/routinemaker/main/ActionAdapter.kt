package com.seahahn.routinemaker.main

import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.AppVar.getSelectedDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ActionAdapter : RecyclerView.Adapter<ActionViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    //데이터들을 저장하는 변수
    private var data = mutableListOf<ActionData>()
    private lateinit var date : String
    private lateinit var dayOfWeek : String

    private var mDays = "" // 행동이 속한 루틴의 반복 요일
    private var mDate = "" // 행동이 속한 루틴의 수행 예정일
    // 사용자가 선택한 날짜가 오늘 날짜이면서 루틴을 수행할 요일인지 아닌지 여부를 저장할 변수
    // 이를 viewholder에 전달하여 아이템의 체크박스를 활성화 혹은 비활성화 시킴
    private var isActionEnabled = false

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return ActionViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
//        d(TAG, "onBindViewHolder")
        holder.getService(service) // 뷰홀더에 레트로핏 서비스 객체 전달
        holder.getRtInfo(mDays, mDate, isActionEnabled)
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<ActionData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<ActionData> {
        return data
    }

    fun replaceDate(dateInput : String) {
        date = dateInput
    }

    // 어댑터에 레트로핏 서비스 객체를 가져옴
    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }

    // 사용자가 선택한 날짜에 따라 아이템의 체크박스를 활성화 혹은 비활성화 시키기 위한 값을 가져오는 메소드
    // 사용자가 선택한 루틴의 체크 여부를 확인하기 위한 메소드
    fun getRtInfo(days : String, date : String, actionEnabled : Boolean) {
        mDays = days
        mDate = date
        isActionEnabled = actionEnabled
    }

    //data는 어댑터에서 사용하고 있는 List임. 순서를 변경하는 함수
    fun onItemDragMove(beforePosition : Int, afterPosition : Int){
        if(beforePosition < afterPosition){
            for (i in beforePosition until afterPosition) {
                Collections.swap(data, i, i + 1)
                d(TAG, "데이터 i ID값 : "+ data[i].id)
                d(TAG, "데이터 i+1 ID값 : "+ data[i+1].id)
                d(TAG, "포지션 상승 이동 감지 : $i -> "+ (i+1))
                d(TAG, "데이터 i ID값 : "+ data[i].id)
                d(TAG, "날짜 : $mDate")

                chageActionPos(service, mDate, data[i+1].id, data[i].id, i+1, i)
            }
        } else {
            for (i in beforePosition downTo afterPosition + 1) {
                Collections.swap(data, i, i - 1)
                d(TAG, "데이터 i ID값 : "+ data[i].id)
                d(TAG, "데이터 i-1 ID값 : "+ data[i-1].id)
                d(TAG, "포지션 하락 이동 감지 : $i -> "+ (i-1))
                d(TAG, "데이터 i ID값 : "+ data[i].id)
                d(TAG, "날짜 : $mDate")

                chageActionPos(service, mDate, data[i-1].id, data[i].id, i-1, i)
            }
        }
        notifyItemMoved(beforePosition, afterPosition)
    }

    // 루틴 내 행동 순서 변경하기
    fun chageActionPos(service: RetrofitService, mDate : String, actionMoved : Int, actionPushed: Int,
                       posMoved: Int, posPushed: Int) {
        d(TAG, "chageActionPos 변수들 : $actionMoved, $actionPushed, $posMoved, $posPushed")
        service.chageActionPos(mDate, actionMoved, actionPushed, posMoved, posPushed).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "루틴 내 행동 순서 변경 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "루틴 내 행동 순서 변경 요청 응답 수신 성공")
                d(TAG, "chageActionPos : "+response.body().toString())
//                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
//                val msg = gson.get("msg").asString
//                val result = gson.get("result").asBoolean
//                when(result) {
//                    true -> {
//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                    }
//                    false -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                }
            }
        })
    }

    //드래그앤 드롭 터치를 마무리 하였을 경우 들어오는 함수
    fun changeMoveEvent(){
        d(TAG, "아이템 이동하였음")
    }
}