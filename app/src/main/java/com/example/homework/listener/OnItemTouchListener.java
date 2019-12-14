package com.example.homework.listener;

public interface OnItemTouchListener {
    boolean onMove(int fromPosition,int toPosition);
    void onSwiped(int position);
}
