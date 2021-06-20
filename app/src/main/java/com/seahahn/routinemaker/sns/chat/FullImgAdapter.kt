package com.seahahn.routinemaker.sns.chat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.nhn.android.idp.common.logger.Logger
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedActivity
import com.seahahn.routinemaker.sns.newsfeed.GroupFeedDetailActivity
import java.util.HashMap

class FullImgAdapter(mContext : Context) : RecyclerView.Adapter<FullImgAdapter.ChatImgViewHolder>() {

    private val TAG = this::class.java.simpleName
    val context : Context = mContext

//    var isFullScreen = false

    var data = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatImgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_full_img, parent, false)
        return ChatImgViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatImgViewHolder, position: Int) {
        holder.onBind(data[position], position)
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<String>) {
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun clearList() {
        // 전체 화면 이미지를 닫을 때 이미지 목록을 비움
        data.clear()
    }
//
//    fun isFullScreen(input : Boolean) {
//        isFullScreen = input
//    }

    inner class ChatImgViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val photoView : ImageView = itemView.findViewById(R.id.photoView)

        fun onBind(img : String, pos : Int) {
            Glide.with(context).load(img)
//                .placeholder(R.drawable.warning)
//                .error(R.drawable.warning)
                .into(photoView)
        }
    }
}