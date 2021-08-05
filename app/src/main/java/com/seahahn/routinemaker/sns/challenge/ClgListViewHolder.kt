package com.seahahn.routinemaker.sns.challenge

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChallengeData
import com.seahahn.routinemaker.sns.challengeData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.sns.chat.ChatActivity
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedActivity
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ClgListViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val title : TextView = itemView.findViewById(R.id.clgTitle)
    private val leaderMark : ImageView = itemView.findViewById(R.id.leaderMark)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.moreBtn)
    private val startDate : TextView = itemView.findViewById(R.id.startDate)
    private val period : TextView = itemView.findViewById(R.id.period)

    private var groupMemberIdData = mutableListOf<Int>()

    init {
//        d(TAG, "RtViewHolder init")
        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
//        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(challengeData : ChallengeData){
        // 아이템 태그에 그룹의 고유 번호와 제목을 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to challengeData.id)

        // 챌린지 제목, 시작일, 기간 표시하기
        title.text = challengeData.title
        startDate.text = challengeData.startDate
        period.text = challengeData.period.toString()

        // 사용자가 챌린지 생성자라면 챌린지 제목 앞에 왕관을 표시함
        if(challengeData.hostId == getUserId(context)) {
            leaderMark.visibility = View.VISIBLE
        } else {
            leaderMark.visibility = View.GONE
        }

        moreBtn.tag = hashMapOf("id" to challengeData.id)
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            val id = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()
            val it = Intent(context, ChallengeInfoActivity::class.java)
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
            val popup = PopupMenu(v.context, v)
            popup.apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(GroupPopupMenuListener(v))
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

                // 가입되어 있으면 그룹원 목록 보기 및 채팅 입장 가능
                if(groupMemberIdData.contains(getUserId(context))) {
                    popup.menu.setGroupVisible(R.id.group_in, true)
                } else {
                    popup.menu.setGroupVisible(R.id.group_in, false)
                }
                show()
            }
        }
    }

    // 루틴 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class GroupPopupMenuListener(v: View) : Sns(), PopupMenu.OnMenuItemClickListener {

    private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val groupItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        private var context = groupItem.context

        override var groupId = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
        override var groupTitle = ((groupItem.tag as HashMap<*, *>)["title"]).toString()
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
                R.id.chat -> { // 그룹 채팅 참여하기
                    val it = Intent(context, ChatActivity::class.java)
                    it.putExtra("title", groupTitle)
                    it.putExtra("isGroupchat", true)
                    it.putExtra("hostId", leaderId)
                    it.putExtra("audienceId", groupId)
                    context.startActivity(it)
                    true
                }
                R.id.memberList -> { // 그룹원 목록 보기
                    val it = Intent(context, GroupMemberListActivity::class.java)
                    it.putExtra("groupId", groupId)
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
                else -> false
            }
        }
    }

    fun getGroupMembers(groupId : Int) {
        d(TAG, "getGroupMembers 변수들 : $groupId")
        serviceInViewHolder.getGroupMembers(groupId, true).enqueue(object : Callback<MutableList<GroupMemberData>> {
            override fun onFailure(call: Call<MutableList<GroupMemberData>>, t: Throwable) {
                Log.d(TAG, "그룹 멤버 목록 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<MutableList<GroupMemberData>>, response: Response<MutableList<GroupMemberData>>) {
                Log.d(TAG, "그룹 멤버 목록 가져오기 요청 응답 수신 성공")
                d(TAG, "getGroupMembers : "+response.body().toString())
                val groupMemberDatas = response.body()
                groupMemberDatas!!
                for(i in 0 until groupMemberDatas.size) {
                    groupMemberIdData.add(groupMemberDatas[i].id)
                }
                moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기

            }
        })
    }
}
