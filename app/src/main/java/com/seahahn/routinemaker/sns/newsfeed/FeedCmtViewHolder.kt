package com.seahahn.routinemaker.sns.newsfeed

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.CmtData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FeedCmtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val profilePic : ImageView = itemView.findViewById(R.id.profile_pic)
    private val nick : TextView = itemView.findViewById(R.id.nick)
    private val contentTV : TextView = itemView.findViewById(R.id.content)

    private val cmtImg : ImageView = itemView.findViewById(R.id.cmt_img)

    private val underContent : LinearLayout = itemView.findViewById(R.id.under_content) // 작성시점과 답글 달기 버튼 있는 영역
    private val createdAt : TextView = itemView.findViewById(R.id.createdAt)
    private val commentIcon : ImageView = itemView.findViewById(R.id.commentIcon)
    private val commentTxt : TextView = itemView.findViewById(R.id.commentTxt)

    private val dividerTv : View = itemView.findViewById(R.id.divider_tv)

    private val contentET : TextView = itemView.findViewById(R.id.contentET) // 댓글 수정 시 출현할 에딧텍스트
    private val underEditText : LinearLayout = itemView.findViewById(R.id.under_edittext) // 작성시점과 답글 달기 버튼 있는 영역
    private val cmtCancel : TextView = itemView.findViewById(R.id.cmt_cancel)
    private val cmtUpdate : TextView = itemView.findViewById(R.id.cmt_update)
    private val dividerEt : View = itemView.findViewById(R.id.divider_et)

    private lateinit var imageURL : String
    private var tempImgList = mutableListOf<Any>()
//    private var likeState : Boolean = false
//    private var commentState : Boolean = false

    init {
//        d(TAG, "RtViewHolder init")
        item.setOnLongClickListener(ItemLongClickListener()) // 아이템 길게 눌렀을 때의 리스너 초기화하기
//        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(cmtData : CmtData) {
        // 아이템 태그에 피드의 고유 번호와 제목을 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to cmtData.id, "writerId" to cmtData.writerId)

        // 댓글 작성자 프로필 사진, 닉네임 및 작성일자 표시하기
        getUserData(cmtData.writerId)
        createdAt.text = cmtData.createdAt

        // 피드 내용 표시하기
        contentTV.text = cmtData.content
        if(cmtData.image.isNotEmpty() || cmtData.image != "[]") {
            d(TAG, "img is not blank")
            imageURL = cmtData.image.substring(1, cmtData.image.length - 1)
            Glide.with(context).load(imageURL).into(cmtImg)
        }
        cmtImg.setOnClickListener(ImgClickListener())

        // 답글 달기 기능 초기화
        commentIcon.tag = hashMapOf("id" to cmtData.id, "mainCmt" to cmtData.mainCmt) // 댓글 고유 번호 넣어두기. 대댓글인 경우 대상 댓글의 고유 번호도 함께 넣어두기
        commentIcon.setOnClickListener(IconClickListener())
        commentTxt.tag = hashMapOf("id" to cmtData.id, "mainCmt" to cmtData.mainCmt) // 댓글 고유 번호 넣어두기. 대댓글인 경우 대상 댓글의 고유 번호도 함께 넣어두기
        commentTxt.setOnClickListener(IconClickListener())

        cmtCancel.tag = hashMapOf("id" to cmtData.id, "mainCmt" to cmtData.mainCmt) // 댓글 고유 번호 넣어두기. 대댓글인 경우 대상 댓글의 고유 번호도 함께 넣어두기
        cmtCancel.setOnClickListener(IconClickListener()) // 댓글 수정에서 취소 눌렀을 때 리스너
        cmtUpdate.tag = hashMapOf("id" to cmtData.id, "mainCmt" to cmtData.mainCmt) // 댓글 고유 번호 넣어두기. 대댓글인 경우 대상 댓글의 고유 번호도 함께 넣어두기
        cmtUpdate.setOnClickListener(IconClickListener()) // 댓글 수정하기 눌렀을 때 리스너
    }

    // 레트로핏 서비스 객체 가져오기
    fun getService(serviceInput : RetrofitService) {
        serviceInViewHolder = serviceInput
    }

    // 피드 작성자 프로필 사진, 닉네임 표시하기
    fun getUserData(writerId : Int) {
        d(TAG, "getUserData 변수들 : $writerId")
        serviceInViewHolder.getUserData(writerId).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "사용자 데이터 가져오기 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "사용자 데이터 가져오기 요청 응답 수신 성공")
                d(TAG, "사용자 데이터 : ${response.body().toString()}")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val nickname = gson.get("nick").asString
                val photo = gson.get("photo").asString

                Glide.with(context).load(photo)
                    .placeholder(R.drawable.warning)
                    .error(R.drawable.warning)
                    .into(profilePic)
                nick.text = nickname
            }
        })
    }

    // 아이템 롱클릭 시 동작할 내용
    inner class ItemLongClickListener() : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            val writerId = (v?.tag as HashMap<*, *>)["writerId"].toString().toInt()
            val popup = PopupMenu(v.context, v)
            popup.apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(CmtPopupMenuListener(v, serviceInViewHolder))
                inflate(R.menu.menu_feed_more)
                // 사용자가 피드 작성자이면 피드 수정 및 삭제 가능
                if(writerId == getUserId(context)) {
                    popup.menu.setGroupVisible(R.id.feed_manage, true)
                    popup.menu.setGroupVisible(R.id.feed_unfollow, false)
                }
                show()
            }
            return true
        }
    }

    // 아이콘 클릭 시 동작할 내용
    inner class IconClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            val context = v!!.context as GroupFeedDetailActivity
            val cmtId = ((v.tag as HashMap<*, *>)["id"]).toString().toInt()
            val mainCmtId = ((v.tag as HashMap<*, *>)["mainCmt"]).toString().toInt()
            when(v.id) {
                R.id.commentIcon -> {
                    toggleSubCmtState(context, cmtId, mainCmtId)
                }
                R.id.commentTxt -> {
                    toggleSubCmtState(context, cmtId, mainCmtId)
//                    val subCmtPh = nick.text.toString() + context.getString(R.string.subCmtPh)
//                    context.chatInput.hint = subCmtPh // "~님에게 답글 남기기"로 힌트 변경
//                    context.chatInput.setText("@"+nick.text.toString()) // 상대방 닉네임 넣기
//                    context.showSoftKeyboard()
//
//                    context.isSubCmt = true
//                    if(mainCmtId == 0) {
//                        context.mainCmt = cmtId
//                    } else {
//                        context.mainCmt = mainCmtId
//                    }
//                    context.subCmtCtrl.visibility = View.VISIBLE
//                    val subCmtInfo = nick.text.toString() + context.getString(R.string.subCmtInfo)
//                    context.subCmtInfo.text = subCmtInfo // "~님에게 답글 남기는 중"으로 텍스트 변경
                }
                R.id.cmt_cancel -> {
                    toggleViewVisibility(context.fullBtmChat)
                    context.hideSoftKeyboard()
                }
                R.id.cmt_update -> {
                    context.updateCmt(serviceInViewHolder, cmtId, contentET.text.toString()) // 댓글 수정하기
                    toggleViewVisibility(context.fullBtmChat)
                    context.hideSoftKeyboard()
                }
            }
        }
        // 대댓글 작성 여부에 따라 보여줄 뷰와 숨길 뷰를 전환
        private fun toggleSubCmtState(context : GroupFeedDetailActivity, cmtId : Int, mainCmtId : Int) {
            val subCmtPh = nick.text.toString() + context.getString(R.string.subCmtPh)
            context.chatInput.hint = subCmtPh // "~님에게 답글 남기기"로 힌트 변경
            context.chatInput.setText("@"+nick.text.toString()) // 상대방 닉네임 넣기
            context.showSoftKeyboard()

            context.isSubCmt = true
            if(mainCmtId == 0) {
                context.mainCmt = cmtId
            } else {
                context.mainCmt = mainCmtId
            }
            context.subCmtCtrl.visibility = View.VISIBLE
            val subCmtInfo = nick.text.toString() + context.getString(R.string.subCmtInfo)
            context.subCmtInfo.text = subCmtInfo // "~님에게 답글 남기는 중"으로 텍스트 변경
        }

        // 댓글 수정 여부에 따라 보여줄 뷰와 숨길 뷰를 전환
        private fun toggleViewVisibility(fullBtmChat : ConstraintLayout) {
            contentTV.visibility = View.VISIBLE // 댓글 내용 텍스트뷰 보이기
            contentET.visibility = View.GONE // 댓글 내용 에딧텍스트 숨기기

            underContent.visibility = View.VISIBLE // 작성시점과 답글 달기 아이콘 보이기
            underEditText.visibility = View.GONE // 취소, 수정하기 버튼 숨기기

            dividerTv.visibility = View.VISIBLE
            dividerEt.visibility = View.GONE

            cmtImg.visibility = View.VISIBLE // 이미지 보이기

            fullBtmChat.visibility = View.VISIBLE // 하단 댓글 입력창 보이기
        }
    }

    // 댓글 이미지 클릭 시 동작할 내용
    inner class ImgClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when (val context = v?.context) {
                is GroupFeedDetailActivity -> {
                    context.fullImgLayout.visibility = View.VISIBLE
                    tempImgList.clear()
                    tempImgList.add(imageURL)
                    context.feedImgAdapter.replaceList(tempImgList)
                    context.feedImgAdapter.isFullScreen(true)
                    context.feedImgAdapter.isCmt(true)
//                    context.fullImgPager.setCurrentItem(context.mViewPager.currentItem, false)
                }
            }
        }
    }

    // 피드 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
//    inner class MoreBtnClickListener : View.OnClickListener {
//        override fun onClick(v: View?) {
//            d(TAG, "group onClick")
//            val writerId = (v?.tag as HashMap<*, *>)["writerId"].toString().toInt()
//            val popup = PopupMenu(v.context, v)
//            popup.apply {
//                // MainActivity implements OnMenuItemClickListener
//                setOnMenuItemClickListener(CmtPopupMenuListener(v, serviceInViewHolder))
//                inflate(R.menu.menu_feed_more)
//                // 사용자가 피드 작성자이면 피드 수정 및 삭제 가능, 아니라면 해당 사용자 글 숨기기 가능
//                if(writerId == getUserId(context)) {
//                    popup.menu.setGroupVisible(R.id.feed_manage, true)
//                    popup.menu.setGroupVisible(R.id.feed_unfollow, false)
//                } else {
//                    popup.menu.setGroupVisible(R.id.feed_manage, false)
//                    popup.menu.setGroupVisible(R.id.feed_unfollow, true)
//                }
//                show()
//            }
//        }
//    }

    // 피드 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class CmtPopupMenuListener(v: View, serviceInput: RetrofitService) : Sns(), PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 피드 수정 또는 삭제 시 해당 피드의 DB 내 고유 번호 전달하기
        private val cmtItem = v // 아이템에 지정해둔 태그를 통해 해당 댓글 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private var context = cmtItem.context as GroupFeedDetailActivity

        val cmtId = ((cmtItem.tag as HashMap<*, *>)["id"]).toString().toInt()
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.update -> { // 댓글 수정하기
                    contentET.visibility = View.VISIBLE // 에딧텍스트 보이기
                    contentET.text = contentTV.text // 에딧텍스트에 댓글 내용 넣기
                    contentTV.visibility = View.INVISIBLE // 댓글 내용 텍스트뷰 숨기기

                    underContent.visibility = View.INVISIBLE // 작성시점과 답글 달기 아이콘 숨기기
                    underEditText.visibility = View.VISIBLE // 취소, 수정하기 버튼 보이기

                    dividerTv.visibility = View.INVISIBLE
                    dividerEt.visibility = View.VISIBLE

                    cmtImg.visibility = View.GONE // 이미지 숨기기

                    context.fullBtmChat.visibility = View.GONE // 하단 댓글 입력창 숨기기
                    true
                }
                R.id.delete -> { // 댓글 삭제하기
                    showAlert("댓글 삭제하기", "정말 삭제하시겠어요?", cmtId, serviceInViewHolder)
                    true
                }
//                R.id.hide -> { // 해당 사용자의 댓글 숨기기
//
//                    true
//                }
                else -> false
            }
        }

        // 댓글 삭제 시 재확인 받는 다이얼로그 띄우기
        // 삭제 후 댓글 목록 다시 불러와서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, cmtId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                    context.deleteCmt(service, cmtId)
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
