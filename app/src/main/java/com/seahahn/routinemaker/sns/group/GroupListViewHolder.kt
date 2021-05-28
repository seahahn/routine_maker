package com.seahahn.routinemaker.sns.group

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo.getUserId
import java.util.*

class GroupListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val title : TextView = itemView.findViewById(R.id.group_title)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val headLimit : TextView = itemView.findViewById(R.id.head_limit)
    private val tags : TextView = itemView.findViewById(R.id.tags)

    init {
//        d(TAG, "RtViewHolder init")
//        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(groupData : GroupData){
        // 아이템 태그에 그룹의 고유 번호와 제목을 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to groupData.id, "title" to groupData.title)

        // 그룹명 표시하기
        title.text = groupData.title

        // 그룹의 태그 표시하기
        val memberList = groupData.members.split(" ") as MutableList<String> // 문자열로 저장되어 있던 그룹 멤버 고유 번호 모음을 리스트로 변환
        d(TAG, "memberList : $memberList")
        val headCount = memberList.size // 현재 가입되어 있는 그룹 멤버 수
        val headLimitValue = groupData.headLimit // 인원 제한 수
        if(headLimitValue > 1) {
            headLimit.text = "$headCount / $headLimitValue 명"
        } else if(headLimitValue == 0){
            headLimit.text = "$headCount 명"
        }

        // 그룹의 태그 표시하기
        val tagList = groupData.tags.split(" ") as MutableList<String>
        d(TAG, "tagList : $tagList")
        var tagTxt = ""
        if(tagList[0].isNotEmpty()) {
            for(i in 0 until tagList.size) {
                tagTxt += "#"+tagList[i]+" "
            }
            tags.text = tagTxt
        } else {
            tags.text = tagTxt
        }

        // 그룹 정보 또는 수정 or 삭제 메뉴 팝업 나오는 버튼.
        // 그룹 수정 및 삭제는 그룹 생성자와 현재 사용자의 고유 번호가 일치하는 경우에만 가능
        // 수정 또는 삭제 시 그룹의 고유 번호(id)와 그룹 생성자의 고유 번호값을 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        moreBtn.tag = hashMapOf("id" to groupData.id, "userId" to groupData.userId)
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

//    // 아이템 클릭 시 동작할 내용
//    inner class ItemClickListener() : View.OnClickListener {
//        override fun onClick(v: View?) {
//            val id = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()
//            val type = ((v.tag as HashMap<*, *>)["type"]).toString()
//            val title = ((v.tag as HashMap<*, *>)["title"]).toString()
//            val mDays = ((v.tag as HashMap<*, *>)["mDays"]).toString()
//            val mDate = ((v.tag as HashMap<*, *>)["mDate"]).toString()
//
//            when(type) {
//                "rt" -> { // 루틴인 경우 루틴 내 행동 목록으로 이동
//                    val it = Intent(context, ActionListActivity::class.java)
//                    it.putExtra("id", id)
//                    it.putExtra("title", title)
//                    it.putExtra("mDays", mDays)
//                    it.putExtra("mDate", mDate)
//                    it.putExtra("isActionEnabled", isActionEnabled) // 루틴의 체크박스 활성화 여부에 따라 루틴에 속한 행동들의 체크박스 활성화 여부를 결정
//                    isRtChecked = rtTitle.isChecked
//                    it.putExtra("isRtChecked", isRtChecked)
//                    context.startActivity(it)
//                }
//            }
//        }
//    }
//
//    // 루틴 목록의 체크박스 체크 시 동작할 내용
//    inner class RtTodoClickListener : View.OnClickListener {
//        override fun onClick(v: View?) {
//            val context : MainActivity = v!!.context as MainActivity
////            d(TAG, "context : $context")
//
//            val checkbox = v as CompoundButton
//            val id = ((checkbox.tag as HashMap<*, *>)["id"]).toString().toInt()
//            val type = ((checkbox.tag as HashMap<*, *>)["type"]).toString()
//            val mDays = ((checkbox.tag as HashMap<*, *>)["mDays"]).toString()
//            val mDate = ((checkbox.tag as HashMap<*, *>)["mDate"]).toString()
//            if(checkbox.isChecked) {
//                checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
//                context.doneRt(context, serviceInViewHolder, id, 1, mDays, mDate)
////                context.getRts(serviceInViewHolder, UserInfo.getUserId(context))
//            } else {
//                checkbox.paintFlags = 0
//                context.doneRt(context, serviceInViewHolder, id, 0, mDays, mDate)
////                context.getRts(serviceInViewHolder, UserInfo.getUserId(context))
//            }
//        }
//    }
//
//    // 루틴 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(GroupPopupMenuListener(v, serviceInViewHolder))
                if(((v.tag as HashMap<*, *>)["userId"]).toString().toInt() == getUserId(context)) {
                    inflate(R.menu.menu_group_more)
                } else {
                    inflate(R.menu.menu_group_more_normal)
                }
                show()
            }
        }
    }
//
//    // 루틴 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class GroupPopupMenuListener(v: View, serviceInput: RetrofitService) : Main(), PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val groupItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private val context = groupItem.context as GroupListActivity
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.info -> { // 그룹 정보 보기
                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val it = Intent(context, GroupInfoActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.update -> { // 그룹 정보 수정
//                    d(TAG, "rtUpdate : "+groupItem.tag)
                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val it = Intent(context, GroupUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.delete -> { // 그룹 삭제
//                    d(TAG, "rtDelete")
                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    showAlert("삭제하기", "정말 삭제하시겠어요?", "확인", "취소", id, svc)
                    true
                }
                else -> false
            }
        }

        // 루틴 또는 할 일 삭제 시 재확인 받는 다이얼로그 띄우기
        // 삭제 후 메인액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, pos: String, neg: String, groupId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(pos) { _: DialogInterface, _: Int ->
                    context.deleteGroup(service, groupId, context)
                }
                .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
