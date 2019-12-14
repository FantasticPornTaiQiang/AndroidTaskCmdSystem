package com.example.homework.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homework.listener.OnItemTouchListener;

public class MyItemTouchHelper extends ItemTouchHelper.Callback {

    private OnItemTouchListener mListener;
    private boolean sort = false;//拖动排序，默认false
    private boolean delete = false;//滑动删除，默认false

    public MyItemTouchHelper(OnItemTouchListener listener){
        this.mListener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int swipeFlags = ItemTouchHelper.START;//代表左滑删除
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        //其实只有UP、DOWN即可完成排序，加上LEFT、RIGHT只是为了滑动更飘逸
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    //滑动删除,true 开启滑动删除，false关闭滑动删除
    public void setDelete(boolean delete){
        this.delete = delete;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return delete;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mListener.onSwiped(viewHolder.getAdapterPosition());
    }

    //长按拖拽,true 开启拖动排序，false关闭拖动排序
    public void setSort(boolean sort) {
        this.sort = sort;
    }
    @Override
    public boolean isLongPressDragEnabled() {
        return sort;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        return mListener.onMove(fromPosition, toPosition);
    }

}

