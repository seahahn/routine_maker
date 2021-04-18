package com.seahahn.routinemaker.main

import android.content.Intent
import android.util.Log.d
import android.view.MenuItem
import android.widget.Toast
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.util.Main
import org.jetbrains.anko.startActivity
import java.lang.Integer.parseInt

class RtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName

    private val rtTitle : CheckBox = itemView.findViewById(R.id.rt_title)
    private val moreBtn : ImageButton = itemView.findViewById(R.id.more_btn)
    private val time : TextView = itemView.findViewById(R.id.rt_time)
    private val days : TextView = itemView.findViewById(R.id.rt_days)

    init {
        itemView.setOnClickListener {
            Toast.makeText(itemView.context, "itemView click test", Toast.LENGTH_SHORT).show()
        }
    }

    fun onBind(rtData : RtData){
        rtTitle.text = rtData.rtTitle // 루틴 제목

        // 루틴 수정 or 삭제 메뉴 팝업 나오는 버튼.
        // 수정 또는 삭제 시 루틴의 고유 번호(id)값을 넘겨서 데이터 받아옴
        moreBtn.tag = rtData.id
        moreBtn.setOnClickListener(MoreBtnClickListener())

        time.text = rtData.time // 루틴 시작 예정 시각

        // 요일 데이터는 리스트로 저장했기 때문에 이를 하나의 문자열로 바꿔줌
//        val showDays = rtData.mDays.joinToString(separator = " ")
        days.text = rtData.mDays // 루틴 반복 요일
    }

    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(RtPopupMenuListener(v))
                inflate(R.menu.menu_rt_more)
                show()
            }
        }
    }

    // 루틴 목록의 아이템 더보기 아이콘의 팝업 메뉴 항목별 동작할 내용
    inner class RtPopupMenuListener(v: View) : Main(), PopupMenu.OnMenuItemClickListener {

        private val TAG = this::class.java.simpleName

        // 루틴 수정 또는 삭제 시 해당 루틴의 DB 내 고유 번호 전달하기
        private val rtItem = v // 더보기 버튼에 지정해둔 태그를 통해 해당 루틴 데이터를 갖고 오기 위함
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.rtUpdate -> { // 루틴 수정
                    d(TAG, "rtUpdate : "+rtItem.tag)
                    val id = parseInt(rtItem.tag.toString())
                    val context = rtItem.context
//                    startActivity<RtUpdateActivity>("id" to id)
                    val it = Intent(context, RtUpdateActivity::class.java)
                    it.putExtra("id", id)
                    context.startActivity(it)
//                    rtItem.tag
                    true
                }
                R.id.rtDelete -> { // 루틴 삭제
                    d(TAG, "rtDelete")
                    true
                }
                else -> false
            }
        }
    }
}
