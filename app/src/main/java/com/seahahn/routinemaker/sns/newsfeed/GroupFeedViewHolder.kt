package com.seahahn.routinemaker.sns.newsfeed

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.FeedData
import com.seahahn.routinemaker.util.Sns
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class GroupFeedViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context as GroupFeedActivity
    lateinit var serviceInViewHolder : RetrofitService

    private val item : ConstraintLayout = itemView.findViewById(R.id.item)
    private val profile_pic : ImageView = itemView.findViewById(R.id.profile_pic)
    private val nick : TextView = itemView.findViewById(R.id.nick)
    private val createdAt : TextView = itemView.findViewById(R.id.createdAt)
    private val mViewPager : ViewPager2 = itemView.findViewById(R.id.mViewPager)
    private val content : TextView = itemView.findViewById(R.id.content)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)

    private val likeIcon : ImageView = itemView.findViewById(R.id.likeIcon)
    private val likeTxt : TextView = itemView.findViewById(R.id.likeTxt)
    private val commentIcon : ImageView = itemView.findViewById(R.id.commentIcon)
    private val commentTxt : TextView = itemView.findViewById(R.id.commentTxt)

    private var likeState : Boolean = false
    private var commentState : Boolean = false

    private lateinit var feedImgAdapter : FeedImgAdapter

    init {
//        d(TAG, "RtViewHolder init")
        // 피드 누르면 해당 피드의 내용과 좋아요, 댓글만 보이는 액티비티로 이동
        item.setOnClickListener(ItemClickListener()) // 아이템 눌렀을 때의 리스너 초기화하기
        moreBtn.setOnClickListener(MoreBtnClickListener()) // 더보기 버튼 눌렀을 때의 리스너 초기화하기
    }

    fun onBind(feedData : FeedData) {
        // 아이템 태그에 피드의 고유 번호와 제목을 담아둠(메소드에서 활용)
        item.tag = hashMapOf("id" to feedData.id)

        // 피드 작성자 프로필 사진, 닉네임 및 작성일자 표시하기
        getUserData(feedData.writerId)
        createdAt.text = feedData.createdAt

        // 피드 내용 표시하기
        content.text = feedData.content
        // 이미지 보여줄 뷰페이저 초기화
        feedImgAdapter = FeedImgAdapter() // 어댑터 초기화
        feedImgAdapter.setFeedIdTag(feedData.id)
        mViewPager.adapter = feedImgAdapter // 어댑터 연결
        if(feedData.images.isNotBlank()) {
            val imgArray = Arrays.stream(feedData.images.substring(1, feedData.images.length - 1).split(",").toTypedArray())
                .map { obj: String -> obj.trim { it <= ' ' } }.toArray() // 문자열을 먼저 배열로 변환
            val imgList = mutableListOf<Any>()
            for(element in imgArray){
                imgList.add(element.toString()) // 배열을 다시 리스트로 만듦
            }
            feedImgAdapter.replaceList(imgList) // 만든 리스트를 목록에 넣음
        }


        // 좋아요 수와 사용자의 좋아요 여부 및 댓글 수 불러오기
        if(feedData.liked) { likeIcon.setImageResource(R.drawable.like_black) } else { likeIcon.setImageResource(R.drawable.like_white) }
        likeState = feedData.liked
        if(feedData.cmt) { commentIcon.setImageResource(R.drawable.comment_black) } else { commentIcon.setImageResource(R.drawable.comment_white) }
        commentState = feedData.cmt

        likeIcon.setOnClickListener(IconClickListener())
        commentIcon.setOnClickListener(IconClickListener())
        likeTxt.text = feedData.likeCount.toString()
        commentTxt.text = feedData.commentCount.toString()
        likeIcon.tag = hashMapOf("id" to feedData.id)
        commentIcon.tag = hashMapOf("id" to feedData.id)

        moreBtn.visibility = View.VISIBLE
        moreBtn.tag = hashMapOf("id" to feedData.id, "writerId" to feedData.writerId)
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
                    .into(profile_pic)
                nick.text = nickname
            }
        })
    }

//    // 아이템 클릭 시 동작할 내용
    inner class ItemClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            // 피드 누르면 해당 피드의 내용과 좋아요, 댓글만 보이는 액티비티로 이동
            val feedId = (v?.tag as HashMap<*, *>)["id"].toString().toInt()
            val it = Intent(context, GroupFeedDetailActivity::class.java)
            it.putExtra("feedId", feedId)
            context.startActivity(it)
        }
    }

    // 피드 누르면 동작할 내용
    inner class IconClickListener() : Sns(), View.OnClickListener {
        override fun onClick(v: View?) {
            val feedId = (v?.tag as HashMap<*, *>)["id"].toString().toInt()
            val context = v.context
            when(v!!.id) {
                R.id.likeIcon -> {
                    setFeedLike(serviceInViewHolder, feedId, getUserId(context), !likeState) // 좋아요 누른 결과 서버에 보내기
                    likeState = !likeState // 기존의 좋아요 상태를 반대로 바꿔줌
                    // 바뀐 좋아요 상태에 따라 좋아요 아이콘의 색상 변경
                    if(likeState) {
                        likeIcon.setImageResource(R.drawable.like_black)
                        likeTxt.text = (likeTxt.text.toString().toInt()+1).toString()
                    } else {
                        likeIcon.setImageResource(R.drawable.like_white)
                        likeTxt.text = (likeTxt.text.toString().toInt()-1).toString()
                    }
                }
                R.id.commentIcon -> {
                    val it = Intent(context, GroupFeedDetailActivity::class.java)
                    it.putExtra("feedId", feedId)
                    context.startActivity(it)
                }
            }
        }
    }

    // 피드 목록의 아이템 내 더보기 버튼 누를 시 동작할 내용
    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            d(TAG, "group onClick")
            val writerId = (v?.tag as HashMap<*, *>)["writerId"].toString().toInt()
            val popup = PopupMenu(v.context, v)
            popup.apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(GroupFeedPopupMenuListener(v, serviceInViewHolder))
                inflate(R.menu.menu_feed_more)
                // 사용자가 피드 작성자이면 피드 수정 및 삭제 가능, 아니라면 해당 사용자 글 숨기기 가능
                if(writerId == getUserId(context)) {
                    popup.menu.setGroupVisible(R.id.feed_manage, true)
                    popup.menu.setGroupVisible(R.id.feed_unfollow, false)
                } else {
                    popup.menu.setGroupVisible(R.id.feed_manage, false)
                    popup.menu.setGroupVisible(R.id.feed_unfollow, true)
                }
                show()
            }
        }
    }
//
    // 피드 목록의 아이템 내 더보기 버튼의 팝업 메뉴 항목별 동작할 내용
    inner class GroupFeedPopupMenuListener(v: View, serviceInput: RetrofitService) : Sns(), PopupMenu.OnMenuItemClickListener {

    private val TAG = this::class.java.simpleName

        // 피드 수정 또는 삭제 시 해당 피드의 DB 내 고유 번호 전달하기
        private val groupItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 피드 데이터를 갖고 오기 위함
        private val svc = serviceInput // 레트로핏 서비스 객체
        private var context = groupItem.context as GroupFeedActivity

        val id = ((groupItem.tag as HashMap<*, *>)["id"]).toString().toInt()
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.update -> { // 피드 수정하기
                    val it = Intent(context, GroupFeedUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.delete -> { // 피드 삭제하기
                    showAlert("피드 삭제하기", "정말 삭제하시겠어요?", id, serviceInViewHolder)
                    true
                }
                R.id.unfollow -> { // 해당 사용자의 피드 숨기기

                    true
                }
                else -> false
            }
        }

        // 그룹 해체 시 재확인 받는 다이얼로그 띄우기
        // 해체 후 가입한 그룹 목록 액티비티 다시 열어서 삭제 완료 후의 목록을 보여줌
        private fun showAlert(title: String, msg: String, feedId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.okay) { _: DialogInterface, _: Int ->
                    context.deleteFeed(service, feedId, context)
                }
                .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
