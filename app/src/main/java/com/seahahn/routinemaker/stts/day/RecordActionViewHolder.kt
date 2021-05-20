package com.seahahn.routinemaker.stts.day

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionData
import com.seahahn.routinemaker.network.RetrofitService

class RecordActionViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

    private val TAG = this::class.java.simpleName
    val context: Context = itemView.context
    lateinit var serviceInViewHolder : RetrofitService

    private val recordTitle : TextView = itemView.findViewById(R.id.action_title)
    private val num : TextView = itemView.findViewById(R.id.num)
    private val resultImg : ImageView = itemView.findViewById(R.id.action_result)

    init {
    }

    fun onBind(actionData : ActionData){
        recordTitle.tag = hashMapOf("id" to actionData.id, "rtId" to actionData.rtId, "title" to actionData.actionTitle)
        recordTitle.text = actionData.actionTitle

        num.text = actionData.time

        when(actionData.done) {
            0 -> resultImg.setImageResource(R.drawable.stts_red)
            1 -> resultImg.setImageResource(R.drawable.stts_green)
        }
    }
}
