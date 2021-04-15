package com.seahahn.routinemaker.main
import android.util.Log.d
import android.view.MenuItem
import android.widget.Toast

import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import org.jetbrains.anko.toast

class RtViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){

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
        val showDays = rtData.days.joinToString(separator = " ")
        days.text = showDays // 루틴 반복 요일
    }

    inner class MoreBtnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            PopupMenu(v!!.context, v).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(MainActivity())
                inflate(R.menu.menu_rt_more)
                show()
            }
        }
    }
}
