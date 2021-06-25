package com.seahahn.routinemaker.util

import androidx.recyclerview.widget.DiffUtil
import com.seahahn.routinemaker.sns.GroupMemberData

object GroupMemberDiffCallback : DiffUtil.ItemCallback<GroupMemberData>() {
    override fun areItemsTheSame(
        oldItem: GroupMemberData,
        newItem: GroupMemberData
    ): Boolean {
        return oldItem.nick == newItem.nick
    }

    override fun areContentsTheSame(
        oldItem: GroupMemberData,
        newItem: GroupMemberData
    ): Boolean {
        return oldItem == newItem
    }

}