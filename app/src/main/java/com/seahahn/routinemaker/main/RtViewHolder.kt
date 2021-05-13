package com.seahahn.routinemaker.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.Main
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class RtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val rtTitle : CheckBox = itemView.findViewById(R.id.rt_title)
    private val typeText : TextView = itemView.findViewById(R.id.typeText)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val time : TextView = itemView.findViewById(R.id.rt_time)
    private val days : TextView = itemView.findViewById(R.id.rt_days)

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEE", Locale.getDefault()) // 문자열 형식(월 일)

    // 사용자가 선택한 날짜가 오늘 날짜이면서 루틴을 수행할 요일인지 아닌지 여부를 저장할 변수
    // 이를 ActionViewHolder에 전달하여 아이템의 체크박스를 활성화 혹은 비활성화 시킴
    private var isActionEnabled = false

    // 사용자가 루틴을 클릭할 경우, 해당 루틴의 체크(완료) 여부를 루틴 내 행동 쪽에 전달하기 위한 변수
    private var isRtChecked = false

    init {
//        d(TAG, "RtViewHolder init")
        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        rtTitle.setOnClickListener(RtTodoClickListener()) // 체크박스 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(rtData : RtData, dateInput : String, dayOfWeekInput : String){

        // 아이템 태그에 루틴(할 일)의 고유 번호, 루틴인지 할 일인지 구분과 함께 반복 요일, 수행 예정일, 반복 여부(할 일인 경우)를 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType, "title" to rtData.rtTitle, "mDays" to rtData.mDays, "mDate" to rtData.mDate)

        // 루틴 제목 표시하기
        rtTitle.text = rtData.rtTitle

        // 사용자가 선택한 날짜가 오늘 날짜인 경우 체크박스 활성화, 다른 날짜인 경우 비활성화하기
        val date = LocalDate.parse(dateInput) // 사용자가 선택한 날짜
        // "루틴" 중에서 오늘 날짜와 요일에 해당하면 체크박스 활성화, 아니면 비활성화
        // "할 일"은 날짜와 요일 상관 없이 체크박스 활성화. 사용자가 원하면 언제든 완료 처리할 수 있도록 하기 위함.
        rtTitle.isEnabled = (rtData.mType == "rt"
                && dayOfWeekInput in rtData.mDays // 사용자가 선택한 날짜의 요일이 해당 루틴의 수행 요일에 포함되지 않으면 비활성화
                && date == LocalDate.now() // 사용자가 선택한 날짜가 오늘 날짜와 동일하지 않으면 비활성화
                ) // 사용자가 선택한 날짜가 루틴의 수행 예정일이 아니면 비활성화 && date == LocalDate.parse(rtData.date)
                || rtData.mType == "todo"
        isActionEnabled = rtTitle.isEnabled

        if(!rtTitle.isEnabled) {
            rtTitle.alpha = 0.4f // 비활성화인 경우 흐리게 만들기
        } else {
            rtTitle.alpha = 1.0f
        }

        // 완료 여부에 따라 체크 여부 설정하기
        if(rtData.done == 0 || date.isAfter(LocalDate.now())) { // || !rtTitle.isEnabled
            rtTitle.isChecked = false
            rtTitle.paintFlags = 0
        } else if(rtData.done == 1) {
            rtTitle.isChecked = true
            rtTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        // 체크박스 태그에 루틴(할 일)의 고유 번호, 루틴인지 할 일인지 구분과 함께 반복 요일, 수행 예정일, 반복 여부(할 일인 경우)를 담아둠(메소드에서 활용)
        rtTitle.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType, "mDays" to rtData.mDays, "mDate" to rtData.mDate, "repeat" to rtData.onFeed)

        // 루틴 수정 or 삭제 메뉴 팝업 나오는 버튼.
        // 수정 또는 삭제 시 루틴의 고유 번호(id)와 구분(type)값을 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        moreBtn.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType)

        // 루틴(할 일) 수행 예정일 및 예정 시각 표시하기
        if(rtData.mType == "rt") {
            time.text = rtData.mTime // hh:mm
        } else {
            (LocalDate.parse(rtData.mDate).format(formatter).toString() + " " +rtData.mTime).also { time.text = it } // (~월 ~일 ~요일 hh:mm)
        }

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

    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            val id = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()
            val type = ((v.tag as HashMap<*, *>)["type"]).toString()
            val title = ((v.tag as HashMap<*, *>)["title"]).toString()
            val mDays = ((v.tag as HashMap<*, *>)["mDays"]).toString()
            val mDate = ((v.tag as HashMap<*, *>)["mDate"]).toString()

            when(type) {
                "rt" -> { // 루틴인 경우 루틴 내 행동 목록으로 이동
                    val it = Intent(context, ActionListActivity::class.java)
                    it.putExtra("id", id)
                    it.putExtra("title", title)
                    it.putExtra("mDays", mDays)
                    it.putExtra("mDate", mDate)
                    it.putExtra("isActionEnabled", isActionEnabled) // 루틴의 체크박스 활성화 여부에 따라 루틴에 속한 행동들의 체크박스 활성화 여부를 결정
                    isRtChecked = rtTitle.isChecked
                    it.putExtra("isRtChecked", isRtChecked)
                    context.startActivity(it)
                }
                "todo" -> { // 할 일인 경우 할 일 수정하기 액티비티로 이동
                    val it = Intent(context, TodoUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                }
            }
        }
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
                R.id.update -> { // 루틴 수정
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
                R.id.delete -> { // 루틴 삭제
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
