package com.seahahn.routinemaker.util

import android.graphics.Canvas
import android.util.Log.d
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.main.ActionAdapter

class ItemMoveCallback constructor(private val adapter : ActionAdapter) : ItemTouchHelper.Callback() {

    private val TAG = this::class.java.simpleName

    private var isMoved = false // 무빙 이벤트에 대한 boolean값

    // 리사이클러뷰 아이템을 움직일 방향을 설정함
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val flagDrag = ItemTouchHelper.UP or ItemTouchHelper.DOWN    //드래그앤 드롭 움직임 설정
        return makeMovementFlags(flagDrag, 0)
    }

    // 아이템을 움직였을 경우 순서를 바꿔줌
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemDragMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    // 좌우 스와이프를 이용할 경우 사용(여기서는 위아래로만 움직일 것이므로 사용하지 않음)
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if(isMoved){
            isMoved = false
            adapter.changeMoveEvent()
        }
    }

    // 아이템 이동 여부를 감지하는 메소드
    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        isMoved = true
        d(TAG, "onMoved 포지션 이동 감지 : $fromPos, $toPos")
    }

    // 아이템 드래그하는 동안 아이템 주변에 그림자 효과 주기 위한 메소드
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // make shadow
            val view = viewHolder.itemView
            view.translationX = dX
            view.translationY = dY
            if (isCurrentlyActive) {
//                    ViewCompat.setElevation(view, elevation)
                view.elevation = 100F
            }
        }
    }

    // 아이템 드래그 끝나면 드래그하는 동안 있었던 그림자 효과를 없애주는 메소드
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val view = viewHolder.itemView
        view.translationX = 0f
        view.translationY = 0f
//        ViewCompat.setElevation(view, 0f)
        view.elevation = 0F
    }
}