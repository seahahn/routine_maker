package com.seahahn.routinemaker.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class RtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    lateinit var serviceInViewHolder : RetrofitService



    private val rtTitle : CheckBox = itemView.findViewById(R.id.rt_title)
    private val typeText : TextView = itemView.findViewById(R.id.typeText)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val time : TextView = itemView.findViewById(R.id.rt_time)
    private val days : TextView = itemView.findViewById(R.id.rt_days)

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault()) // 문자열 형식(월 일)

    init {
//        d(TAG, "RtViewHolder init")
        itemView.setOnClickListener {
            Toast.makeText(itemView.context, "itemView click test", Toast.LENGTH_SHORT).show()
        }

        rtTitle.setOnClickListener(RtTodoClickListener()) // 체크박스 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(rtData : RtData, dateInput : String, dayOfWeekInput : String){
        // 루틴 제목 표시하기
        rtTitle.text = rtData.rtTitle

        // 사용자가 선택한 날짜가 오늘 날짜인 경우 체크박스 활성화, 다른 날짜인 경우 비활성화
        val date = LocalDate.parse(dateInput) // 사용자가 선택한 날짜
        // "루틴" 중에서 오늘 날짜와 요일에 해당하면 체크박스 활성화, 아니면 비활성화
        // "할 일"은 날짜와 요일 상관 없이 체크박스 활성화. 사용자가 원하면 언제든 완료 처리할 수 있도록 하기 위함.
        rtTitle.isEnabled = !(rtData.mType == "rt" && (date != LocalDate.now() || dayOfWeekInput !in rtData.mDays || date != LocalDate.parse(rtData.date)))

        if(!rtTitle.isEnabled) {
            rtTitle.alpha = 0.4f // 비활성화인 경우 흐리게 만들기
        } else {
            rtTitle.alpha = 1.0f
        }

        // 완료 여부에 따라 체크 여부 설정하기
        if(rtData.done == 0) {
            rtTitle.isChecked = false
            rtTitle.paintFlags = 0
        } else {
            rtTitle.isChecked = true
            rtTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        // 체크박스 태그에 루틴(할 일)의 고유 번호, 루틴인지 할 일인지 구분과 함께 반복 요일, 수행 예정일, 반복 여부(할 일인 경우)를 담아둠(메소드에서 활용)
        rtTitle.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType, "mDays" to rtData.mDays, "mDate" to rtData.date, "repeat" to rtData.onFeed)

        // 루틴 수정 or 삭제 메뉴 팝업 나오는 버튼.
        // 수정 또는 삭제 시 루틴의 고유 번호(id)와 구분(type)값을 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        moreBtn.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType)

        // 루틴(할 일) 수행 예정일 및 예정 시각 표시하기(~월 ~일 ~요일 hh:mm)
        (LocalDate.parse(rtData.date).format(formatter).toString() + " " +rtData.time).also { time.text = it }

        // 루틴 반복 요일 표시하기
        days.text = rtData.mDays

        // 아이템 우측 하단에 루틴인지 할 일인지 표시하기
        when(rtData.mType) {
            "rt" -> typeText.text = itemView.context.getString(R.string.item_rt)
            "todo" -> typeText.text = itemView.context.getString(R.string.item_todo)
        }
    }

    // 레트로핏 서비스 객체 가져오기(doneRt에서 사용)
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

    // 루틴 목록의 체크박스 체크 시 동작할 내용
    inner class RtTodoClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val context : MainActivity = v!!.context as MainActivity
//            d(TAG, "context : $context")

            val checkbox = v as CompoundButton
            val id = ((checkbox.tag as HashMap<*, *>)["id"]).toString().toInt()
            val type = ((checkbox.tag as HashMap<*, *>)["type"]).toString()
            val mDays = ((checkbox.tag as HashMap<*, *>)["mDays"]).toString()
            val mDate = ((checkbox.tag as HashMap<*, *>)["mDate"]).toString()
            val repeat = ((checkbox.tag as HashMap<*, *>)["repeat"]).toString()
            if(checkbox.isChecked) {
                checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                context.doneRt(context, serviceInViewHolder, id, 1, mDays, mDate)
//                context.getRts(serviceInViewHolder, UserInfo.getUserId(context))
            } else {
                checkbox.paintFlags = 0
                context.doneRt(context, serviceInViewHolder, id, 0, mDays, mDate)
//                context.getRts(serviceInViewHolder, UserInfo.getUserId(context))
            }
        }
    }

    // 루틴 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(RtPopupMenuListener(v, serviceInViewHolder))
                inflate(R.menu.menu_rt_more)
                show()
            }
        }
    }

    // 루틴 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class RtPopupMenuListener(v: View, serviceInput: RetrofitService) : Main(), PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val rtItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private val context = rtItem.context as MainActivity
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.rtUpdate -> { // 루틴 수정
//                    d(TAG, "rtUpdate : "+rtItem.tag)
                    val id = ((rtItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val type = ((rtItem.tag as HashMap<*, *>)["type"]).toString()
                    var it = Intent()
                    when(type) {
                        "rt" -> it = Intent(context, RtUpdateActivity::class.java)
                        "todo" -> it = Intent(context, TodoUpdateActivity::class.java)
                    }
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.rtDelete -> { // 루틴 삭제
//                    d(TAG, "rtDelete")
                    val id = ((rtItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    showAlert("삭제하기", "정말 삭제하시겠어요?", "확인", "취소", id, svc)
                    true
                }
                else -> false
            }
        }

        // 루틴 또는 할 일 삭제 시 재확인 받는 다이얼로그 띄우기
        // 삭제 후 메인액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, pos: String, neg: String, rtId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(pos) { _: DialogInterface, _: Int ->
                    context.deleteRt(service, rtId, context)
//                    val it = Intent(context, MainActivity::class.java)
//                    finish()
//                    context.startActivity(it)

//                    context.getRts(service, UserInfo.getUserId(context))
                }
                .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
