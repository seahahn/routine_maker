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
import com.bumptech.glide.Glide
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.GroupData
import com.seahahn.routinemaker.sns.GroupMemberData
import com.seahahn.routinemaker.util.AppVar
import com.seahahn.routinemaker.util.AppVar.setNextLeaderId
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.startActivity
import java.util.*

class GroupNextLeaderViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    val contextSns: Sns = Sns()
    lateinit var serviceInViewHolder : RetrofitService

//    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val radioBtn : RadioButton = itemView.findViewById(R.id.radioBtn)
    private val img : ImageView = itemView.findViewById(R.id.img)
    private val nick : TextView = itemView.findViewById(R.id.nick)

    init {
//        d(TAG, "RtViewHolder init")
        radioBtn.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
//        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
        img.setOnClickListener(Sns.ProfileClickListener())
        nick.setOnClickListener(Sns.ProfileClickListener())
    }

    fun onBind(groupMemberData : GroupMemberData){
        // 라디오버튼 태그에 그룹 멤버의 고유 번호를 담아둠
        radioBtn.tag = hashMapOf("id" to groupMemberData.id)

        // 그룹 멤버 프로필 사진 표시하기
        Glide.with(context).load(groupMemberData.photo)
            .placeholder(R.drawable.warning)
            .error(R.drawable.warning)
            .into(img)

        // 그룹 멤버 닉네임 표시하기
        nick.text = groupMemberData.nick

        // 프로필 사진 및 닉네임에 사용자의 고유 번호를 태그로 담아둠(누르면 해당 사용자의 프로필 정보를 볼 수 있는 액티비티로 이동하기 위함)
        img.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
        nick.tag = hashMapOf("id" to groupMemberData.id, "nick" to groupMemberData.nick)
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

//    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : Sns(), View.OnClickListener {
        override fun onClick(v: View?) {
            val newLeaderId = ((v!!.tag as HashMap<*, *>)["id"]).toString().toInt()
            setNextLeaderId(context, newLeaderId)
            d(TAG, "newLeaderId = $newLeaderId")
        }
    }

    // 그룹 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(GroupPopupMenuListener(v, serviceInViewHolder))
                // 사용자가 그룹 생성자이면 그룹 수정 및 삭제 가능, 아니라면 정보 보기만 가능
//                if(((v.tag as HashMap<*, *>)["userId"]).toString().toInt() == getUserId(context)) {
                    inflate(R.menu.menu_group_more)
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
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
//                R.id.info -> { // 그룹 정보 보기
//                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
//                    val it = Intent(context, GroupInfoActivity::class.java)
//                    it.putExtra("id", id)
//                    context.startActivity(it)
//                    true
//                }
                R.id.update -> { // 그룹 정보 수정
//                    d(TAG, "rtUpdate : "+groupItem.tag)
                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val it = Intent(context, GroupUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.delete -> { // 그룹 해체
//                    d(TAG, "rtDelete")
                    val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    showAlert("그룹 해체하기", "정말 해체하시겠어요?", "확인", "취소", id, svc)
                    true
                }
                else -> false
            }
        }

        // 그룹 해체 시 재확인 받는 다이얼로그 띄우기
        // 해체 후 가입한 그룹 목록 액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, pos: String, neg: String, groupId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                    deleteGroup(service, groupId, context)
                    finish()
                    startActivity<GroupListActivity>()
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
