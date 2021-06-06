package com.seahahn.routinemaker.sns.group

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import java.util.*

class GroupListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val title : TextView = itemView.findViewById(R.id.group_title)
    private val leaderMark : ImageView = itemView.findViewById(R.id.leaderMark)
    private val lockMark : ImageView = itemView.findViewById(R.id.lockMark)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val headLimit : TextView = itemView.findViewById(R.id.head_limit)
    private val tags : TextView = itemView.findViewById(R.id.tags)

//    private val layoutParams = ConstraintLayout.LayoutParams(
//        ConstraintLayout.LayoutParams.WRAP_CONTENT,
//        ConstraintLayout.LayoutParams.WRAP_CONTENT
//    )

    init {
//        d(TAG, "RtViewHolder init")
        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(groupData : GroupData){
        // 아이템 태그에 그룹의 고유 번호와 제목을 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to groupData.id)

        // 그룹명 표시하기
        title.text = groupData.title
        if(!groupData.onPublic) lockMark.visibility = View.VISIBLE
        // 검색 시 대소문자 구분 없이 검색 결과에 출력되기 위해서 전부 소문자로 변환
        groupData.title = groupData.title.toLowerCase(Locale.getDefault())

        // 그룹의 인원 표시하기
        val headCount = groupData.memberCount // 현재 가입되어 있는 그룹 멤버 수
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
        // 검색 시 대소문자 구분 없이 검색 결과에 출력되기 위해서 전부 소문자로 변환
        groupData.tags = groupData.tags.toLowerCase(Locale.getDefault())


        // 그룹 정보 수정 or 해체 메뉴 팝업 나오는 버튼
        // 그룹 수정 및 해체는 그룹 생성자와 현재 사용자의 고유 번호가 일치하는 경우에만 가능
        // 수정 또는 해체 시 그룹의 고유 번호(id)와 그룹 생성자의 고유 번호값을 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        if(groupData.leaderId == getUserId(context)) {
            leaderMark.visibility = View.VISIBLE
        }
//            moreBtn.visibility = View.VISIBLE
            moreBtn.tag = hashMapOf("id" to groupData.id, "leaderId" to groupData.leaderId, "onPublic" to groupData.onPublic)
    }

    // 마진값의 단위를 dp로 변환해주는 메소드
    private fun changeDP(value : Int) : Int {
        var displayMetrics = context.resources.displayMetrics
        var dp = Math.round(value * displayMetrics.density)
        return dp
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

//    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            val id = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()
            val it = Intent(context, GroupInfoActivity::class.java)
            it.putExtra("id", id)
            context.startActivity(it)
        }
    }

    // 그룹 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            d(TAG, "group onClick")
            val leaderId = (v?.tag as HashMap<*, *>)["leaderId"].toString().toInt()
            val onPublic = (v.tag as HashMap<*, *>)["onPublic"].toString().toBoolean()
            val popup = PopupMenu(v!!.context, v)
            popup.apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(GroupPopupMenuListener(v, serviceInViewHolder))
                // 사용자가 그룹 생성자이면 그룹 수정 및 가입 신청자 목록 보기 가능, 아니라면 그룹 정보 보기만 가능
//                if(((v.tag as HashMap<*, *>)["userId"]).toString().toInt() == getUserId(context)) {
                    inflate(R.menu.menu_group_more)
                    if(leaderId == getUserId(context)) {
                        popup.menu.setGroupVisible(R.id.group_manage, true)
                        if(!onPublic) { // 그룹이 비공개일 경우에만 그룹 가입 신청자 목록 보기 가능
                            popup.menu.setGroupVisible(R.id.group_applicant, true)
                        } else {
                            popup.menu.setGroupVisible(R.id.group_applicant, false)
                        }
                    } else {
                        popup.menu.setGroupVisible(R.id.group_manage, false)
                        popup.menu.setGroupVisible(R.id.group_applicant, false)
                    }
//                } else {
//                    inflate(R.menu.menu_group_more_normal)
//                }
                show()
            }
        }
    }
//
//    // 루틴 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class GroupPopupMenuListener(v: View, serviceInput: RetrofitService) : Sns(), PopupMenu.OnMenuItemClickListener {

    private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val groupItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private var context = groupItem.context

        override var groupId = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
        override var leaderId = ((groupItem.tag as HashMap<*, *>)["leaderId"]).toString().toInt()
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.info -> { // 그룹 정보 보기
//                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val it = Intent(context, GroupInfoActivity::class.java)
                    it.putExtra("id", groupId)
                    context.startActivity(it)
                    true
                }
                R.id.update -> { // 그룹 정보 수정
//                    d(TAG, "rtUpdate : "+groupItem.tag)
//                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    if(leaderId == getUserId(context)) {
                        item.isVisible
                    } else {
                        !item.isVisible
                    }
                    val it = Intent(context, GroupUpdateActivity::class.java)
                    it.putExtra("id", groupId)
                    context.startActivity(it)
                    true
                }
                R.id.applicants -> { // 그룹 가입 신청자 목록 보기
//                    d(TAG, "rtUpdate : "+groupItem.tag)
//                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    if(leaderId == getUserId(context)) {
                        item.isVisible
                    } else {
                        !item.isVisible
                    }
                    val it = Intent(context, GroupApplicantListActivity::class.java)
                    it.putExtra("id", groupId)
                    context.startActivity(it)
                    true
                }
//                R.id.delete -> { // 그룹 해체
////                    d(TAG, "rtDelete")
//                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
////                    showAlert("그룹 해체하기", "정말 해체하시겠어요?", id, svc)
//                    showAlert("그룹 해체하기", "정말 해체하시겠어요?")
//                    true
//                }
                else -> false
            }
        }

//        override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//            d(TAG, "onPrepareOptionsMenu")
//            if(leaderId == getUserId(context)) {
//                menu!!.setGroupVisible(R.id.group_manage, true)
//            } else {
//                menu!!.setGroupVisible(R.id.group_manage, false)
//            }
//            return super.onPrepareOptionsMenu(menu)
//        }

        // 그룹 해체 시 재확인 받는 다이얼로그 띄우기
        // 해체 후 가입한 그룹 목록 액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
//        private fun showAlert(title: String, msg: String, groupId: Int, service: RetrofitService) {
//            AlertDialog.Builder(context)
//                .setTitle(title)
//                .setMessage(msg)
//                .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
//                    if(headCount > 1) {
//                        selectNextLeader()
//                    } else {
//                        deleteGroup(service, groupId, context)
//                        finish()
//                        startActivity<GroupListActivity>()
//                    }
//                }
//                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
//                .show()
//        }
    }
}
