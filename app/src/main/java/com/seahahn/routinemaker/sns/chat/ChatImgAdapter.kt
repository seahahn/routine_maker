package com.seahahn.routinemaker.sns.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seahahn.routinemaker.R

class ChatImgAdapter(mContext : Context) : RecyclerView.Adapter<ChatImgAdapter.ChatImgViewHolder>() {

    private val TAG = this::class.java.simpleName
    val context : Context = mContext

    var isFullScreen = false

    var data = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatImgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_img, parent, false)
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

    inner class ChatImgViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        private val photoView : ImageView = itemView.findViewById(R.id.photoView)

        init {
            photoView.setOnClickListener(ImgClickListener()) // 우측 상단 삭제 버튼 눌렀을 때의 리스너 초기화하기
        }

        fun onBind(img : String, pos : Int) {
            photoView.tag = pos
            Glide.with(context).load(img)
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(photoView)
        }

        inner class ImgClickListener : View.OnClickListener {
            override fun onClick(v: View?) {
                if(context is ChatActivity) {
                    context.fullImgAdapter.replaceList(data)
                    context.fullImgLayout.visibility = View.VISIBLE
                    context.fullImgPager.setCurrentItem(v!!.tag as Int, false)
                }
            }
        }
    }
}