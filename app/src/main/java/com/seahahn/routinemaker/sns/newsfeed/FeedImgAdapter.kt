package com.seahahn.routinemaker.sns.newsfeed

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import java.util.HashMap

class FeedImgAdapter : RecyclerView.Adapter<FeedImgAdapter.FeedImgViewHolder>() {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService

    var isChangable = false
    var isFullScreen = false
    var isCmt = false

    //데이터들을 저장하는 변수
    var data = mutableListOf<Any>()
    var tagForFeedId = 0

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedImgViewHolder {
        lateinit var view : View
        when(viewType) {
            0 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_img, parent, false)
            1 -> view = LayoutInflater.from(parent.context).inflate(R.layout.item_full_img, parent, false)
        }
        return FeedImgViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: FeedImgViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.onBind(data[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        return if(isFullScreen) {
            1
        } else {
            0
        }
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<Any>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun returnList(): MutableList<Any> {
        return data
    }

    fun isChangableActivity(input : Boolean) {
        isChangable = input
    }

    fun isFullScreen(input : Boolean) {
        isFullScreen = input
    }

    fun isCmt(input : Boolean) {
        isCmt = input
    }

    fun setFeedIdTag(input : Int) {
        tagForFeedId = input
    }

    inner class FeedImgViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = this::class.java.simpleName
        val context: Context = itemView.context

        private val photoView : PhotoView = itemView.findViewById(R.id.photoView)
        private val imgDelete : ImageButton = itemView.findViewById(R.id.img_delete)

        init {
//        d(TAG, "RtViewHolder init")
            // 피드 누르면 해당 피드의 내용과 좋아요, 댓글만 보이는 액티비티로 이동
            photoView.setOnClickListener(ImgClickListener()) // 우측 상단 삭제 버튼 눌렀을 때의 리스너 초기화하기
            imgDelete.setOnClickListener(ImgDeleteClickListener()) // 우측 상단 삭제 버튼 눌렀을 때의 리스너 초기화하기
        }

        fun onBind(img : Any, pos : Int) {
//            photoView.setImageURI(uri)
            Glide.with(context).load(img).into(photoView)
            photoView.tag = hashMapOf("feedId" to tagForFeedId)
            imgDelete.tag = hashMapOf("pos" to pos)

            if(!isChangable) imgDelete.visibility = View.GONE
        }

        inner class ImgClickListener : View.OnClickListener {
            override fun onClick(v: View?) {
                when (val context = v?.context) {
                    is GroupFeedDetailActivity -> {
                        if(!isFullScreen) {
                            isFullScreen(true)
                            context.fullImgLayout.visibility = View.VISIBLE
                            context.feedImgAdapter.replaceList(data)
                            context.fullImgPager.setCurrentItem(context.mViewPager.currentItem, false)
                        }
                    }
                    is GroupFeedActivity -> {
                        d(TAG, "이미지 누름")
                        // 피드 누르면 해당 피드의 내용과 좋아요, 댓글만 보이는 액티비티로 이동
                        val feedId = (v.tag as HashMap<*, *>)["feedId"].toString().toInt()
                        val it = Intent(context, GroupFeedDetailActivity::class.java)
                        it.putExtra("feedId", feedId)
                        context.startActivity(it)
                    }
                }
            }
        }

        // 피드 작성, 수정하기 액티비티에서 이미지 삭제 버튼 누를 시 동작할 내용
        inner class ImgDeleteClickListener : View.OnClickListener {
            override fun onClick(v: View?) {
                val pos = (v?.tag as HashMap<*, *>)["pos"].toString().toInt()
                val context = v.context
                data.removeAt(pos)
                notifyDataSetChanged()
            }
        }
    }
}