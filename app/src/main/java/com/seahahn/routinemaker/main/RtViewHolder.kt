package com.seahahn.routinemaker.main

import android.content.DialogInterface
import android.content.Intent
import android.util.Log.d
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.user.MypageActivity
import com.seahahn.routinemaker.util.Main
import com.seahahn.routinemaker.util.UserInfo.getUserId
import org.jetbrains.anko.startActivity

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
        // 수정 또는 삭제 시 루틴의 고유 번호(id)와 구분(type)값을 넘겨서 이에 맞는 액티비티를 열고 데이터를 받아옴
        moreBtn.tag = hashMapOf("id" to rtData.id, "type" to rtData.mType)
        moreBtn.setOnClickListener(MoreBtnClickListener())

        time.text = rtData.time // 루틴 시작 예정 시각

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
        private val context = rtItem.context
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.rtUpdate -> { // 루틴 수정
                    d(TAG, "rtUpdate : "+rtItem.tag)
                    val id = ((rtItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    val type = ((rtItem.tag as HashMap<*, *>)["type"]).toString()
                    var it = Intent()
                    when(type) {
                        "rt" -> it = Intent(context, RtUpdateActivity::class.java)
                        "todo" -> it = Intent(context, TodoUpdateActivity::class.java)
                    }
                    it.putExtra("id", id)
                    context.startActivity(it)
                    true
                }
                R.id.rtDelete -> { // 루틴 삭제
                    d(TAG, "rtDelete")
                    service = initRetrofit()
                    val id = ((rtItem.tag as HashMap<*, *>)["id"]).toString().toInt()
                    showAlert("삭제하기", "정말 삭제하시겠어요?", "확인", "취소", id, service)
                    true
                }
                else -> false
            }
        }

        // 루틴 또는 할 일 삭제 시 재확인 받는 다이얼로그 띄우기
        fun showAlert(title: String, msg: String, pos: String, neg: String, rtId: Int, service: RetrofitService) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(pos) { _: DialogInterface, _: Int ->
                    deleteRt(service, rtId, context)
                    val it = Intent(context, MainActivity::class.java)
                    finish()
                    context.startActivity(it)
                }
                .setNegativeButton(neg) { _: DialogInterface, _: Int -> }
                .show()
        }
    }
}
