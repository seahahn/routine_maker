package com.seahahn.routinemaker.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.AppVar.getSelectedDate
import com.seahahn.routinemaker.util.Main
import java.time.LocalDate
import java.util.*

class ActionViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val actionTitle : CheckBox = itemView.findViewById(R.id.rt_title)
    private val typeText : TextView = itemView.findViewById(R.id.typeText)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val time : TextView = itemView.findViewById(R.id.rt_time)
    private val days : TextView = itemView.findViewById(R.id.rt_days)

    private var mDays = ""
    private var mDate = ""
    private var isActionEnabled = false

    init {
//        d(TAG, "RtViewHolder init")
        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        actionTitle.setOnClickListener(ActionClickListener()) // 체크박스 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(actionData : ActionData) {

        // 아이템 태그에 루틴(할 일)의 고유 번호를 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to actionData.id, "rtId" to actionData.rtId, "pos" to actionData.pos)

        // 행동 제목 표시하기
        actionTitle.text = actionData.actionTitle

        // 선택한 루틴이 활성화되어 있으면 활성화, 아니면 비활성화
        actionTitle.isEnabled = isActionEnabled

        if(!actionTitle.isEnabled) {
            actionTitle.alpha = 0.4f // 비활성화인 경우 흐리게 만들기
        } else {
            actionTitle.alpha = 1.0f
        }

        // 완료 여부에 따라 체크 여부 설정하기
        val date = LocalDate.parse(getSelectedDate(context)) // 사용자가 선택한 날짜가 오늘 날짜인 경우 체크박스 활성화, 오늘 이후 날짜(미래)인 경우 비활성화하기
        if(actionData.done == 0 || date.isAfter(LocalDate.now())) {
//            d(TAG, "행동 번호 " + actionData.id + "미완료 : " + actionData.done)
            actionTitle.isChecked = false
            actionTitle.paintFlags = 0
        } else if(actionData.done == 1) {
//            d(TAG, "행동 번호 " + actionData.id + "완료 : " + actionData.done)
            actionTitle.isChecked = true
            actionTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }



        // 체크박스 태그에 행동의 고유 번호를 담아둠(메소드에서 활용)
        actionTitle.tag = hashMapOf("id" to actionData.id)

        // 루틴 수정 or 삭제 메뉴 팝업 나오는 버튼.
        // 수정 또는 삭제 시 루틴의 고유 번호(id)를 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        moreBtn.tag = hashMapOf("id" to actionData.id, "rtId" to actionData.rtId)

        // 예상 소요 시간 표시하기
        time.text = actionData.time + "분" // ~ 분

        // 루틴이나 할 일 구분 표시하던 아이템 우측 하단의 글자 없애기
        typeText.visibility = View.GONE
        // 루틴이나 할 일의 반복 요일 표시하던 글자 없애기
        days.visibility = View.GONE
    }

    fun getRtInfo(days : String, date : String, actionEnabled : Boolean) {
        mDays = days
        mDate = date
        isActionEnabled = actionEnabled
    }

    // 레트로핏 서비스 객체 가져오기(doneRt에서 사용)
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            val id = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()

            val it = Intent(context, ActionUpdateActivity::class.java)
            it.putExtra("id", id)
            context.startActivity(it)
        }
    }

    // 행동 목록의 체크박스 체크 시 동작할 내용
    inner class ActionClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val context : ActionListActivity = v!!.context as ActionListActivity
//            d(TAG, "context : $context")

            val checkbox = v as CompoundButton
            val id = ((checkbox.tag as HashMap<*, *>)["id"]).toString().toInt()
            if(checkbox.isChecked) {
                checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                context.doneAction(context, serviceInViewHolder, id, 1, mDays, mDate)
            } else {
                checkbox.paintFlags = 0
                context.doneAction(context, serviceInViewHolder, id, 0, mDays, mDate)
            }
        }
    }

    // 행동 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(ActionPopupMenuListener(v, serviceInViewHolder))
                inflate(R.menu.menu_rt_more)
                show()
            }
        }
    }

    // 루틴 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class ActionPopupMenuListener(v: View, serviceInput: RetrofitService) : Main(), PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val actionItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private val context = actionItem.context as ActionListActivity
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.update -> { // 행동 수정
//                    d(TAG, "update : "+rtItem.tag)
                    val id = ((actionItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val it = Intent(context, ActionUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.delete -> { // 행동 삭제
//                    d(TAG, "delete")
                    val actionId = ((actionItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val rtId = ((actionItem.tag as HashMap<*, *>)["rtId"]).toString().toInt()
                    d(TAG, "ids : $actionId, $rtId")
                    showAlert("삭제하기", "정말 삭제하시겠어요?", "확인", "취소", actionId, rtId, svc)
                    true
                }
                else -> false
            }
        }

        // 루틴 또는 할 일 삭제 시 재확인 받는 다이얼로그 띄우기
        // 삭제 후 메인액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, pos: String, neg: String, actionId : Int, rtId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(pos) { _: DialogInterface, _: Int ->
                    context.deleteAction(service, actionId, rtId, context)
                }
                .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}