package com.example.homework.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.PopupMenu;

import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homework.TaskActivity;
import com.example.homework.data.LongTermTask;
import com.example.homework.data.PeriodicTask;
import com.example.homework.data.TaskKind;
import com.example.homework.data.TemporaryTask;
import com.example.homework.listener.OnItemClickListener;
import com.example.homework.R;
import com.example.homework.utils.SerializeAndDeserialize;

import java.io.IOException;
import java.util.List;

import static com.example.homework.utils.MyActivityManager.getRunningActivity;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter {
    public static class TaskViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskNameTextView;
        private final TextView taskCreateTimeTextView;
        private final TextView taskisFinishedTextView;
        private final TextView temporaryTaskItemMenuButton;
        private final TextView longTermTaskNameTextView;
        private final TextView longTermTaskCreateTimeTextView;
        private final TextView longTermTaskChildNumberTextView;
        private final TextView longTermTaskisFinishedTextView;
        private final TextView longTermTaskItemMenuButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.temporary_and_periodic_task_name_text_view);
            taskCreateTimeTextView = itemView.findViewById(R.id.temporary_and_periodic_task_create_time_text_view);
            taskisFinishedTextView = itemView.findViewById(R.id.temporary_and_periodic_task_is_finished_text_view);
            temporaryTaskItemMenuButton = itemView.findViewById(R.id.temporary_and_periodic_task_item_menu_button);
            longTermTaskNameTextView = itemView.findViewById(R.id.long_term_task_name_text_view);
            longTermTaskCreateTimeTextView = itemView.findViewById(R.id.long_term_task_create_time_text_view);
            longTermTaskChildNumberTextView = itemView.findViewById(R.id.long_term_task_child_number_text_view);
            longTermTaskisFinishedTextView = itemView.findViewById(R.id.long_term_task_is_finished_text_view);
            longTermTaskItemMenuButton = itemView.findViewById(R.id.long_term_task_item_menu_button);
        }
    }

    public static final int MENU_DELETE_THIS_TASK = 0;
    public static final int MENU_CHANGE_TASK_STATE = 1;
    public static final int MENU_CUT_THIS_TASK = 2;
    public static final int MENU_INSERT_A_TASK = 3;
    public static final int MENU_CREATE_CHILD_TASK = 4;
    public static final int MENU_ENTER_CHILD_TASK = 5;

    private List<?> taskList;
    private TaskKind taskKind;
    private Context context;
    private TaskViewHolder taskViewHolder;

    public TasksRecyclerViewAdapter(List<?> taskList, TaskKind taskKind, Context context) {
        this.taskList = taskList;
        this.taskKind = taskKind;
        this.context = context;
    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @Override
    public TasksRecyclerViewAdapter.TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if(taskKind == TaskKind.TEMPORARYTASK || taskKind == TaskKind.PERIODICTASK){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temporary_and_periodic_task_item_layout, parent, false);
        } else if (taskKind == TaskKind.LONGTERMTASK){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.long_term_task_item_layout, parent, false);
        }
        return new TasksRecyclerViewAdapter.TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        taskViewHolder = (TaskViewHolder) viewHolder;
        if(taskKind == TaskKind.TEMPORARYTASK) {
            taskViewHolder.taskNameTextView.setText((position + 1) + "   " +(((TemporaryTask)taskList.get(position)).getName()));
            taskViewHolder.taskCreateTimeTextView.setText("创建时间  " + (((TemporaryTask)taskList.get(position)).getCreateTime()));
            if(((TemporaryTask) taskList.get(position)).isFinished()) taskViewHolder.taskisFinishedTextView.setText("已完成");
            else taskViewHolder.taskisFinishedTextView.setText("未完成");
            taskViewHolder.temporaryTaskItemMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    popupMenu.getMenuInflater().inflate(R.menu.temporary_and_periodic_task_popup_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                            Intent intent = new Intent("com.example.broadcast.LOCAL_BROADCAST");
                            intent.putExtra("position",position);
                            switch (item.getItemId()) {
                                case R.id.delete_this_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_DELETE_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.change_task_state:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_CHANGE_TASK_STATE);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.copy_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    return true;
                                case R.id.cut_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    intent.putExtra("do_which", MENU_CUT_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.insert_a_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_INSERT_A_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        } else if (taskKind == TaskKind.PERIODICTASK) {
            taskViewHolder.taskNameTextView.setText((position + 1) + "   " +(((PeriodicTask)taskList.get(position)).getName()));
            taskViewHolder.taskCreateTimeTextView.setText("创建时间  " + (((PeriodicTask)taskList.get(position)).getCreateTime()));
            if(((PeriodicTask) taskList.get(position)).isFinished()) taskViewHolder.taskisFinishedTextView.setText("已完成");
            else taskViewHolder.taskisFinishedTextView.setText("未完成");
            taskViewHolder.temporaryTaskItemMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    popupMenu.getMenuInflater().inflate(R.menu.temporary_and_periodic_task_popup_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                            Intent intent = new Intent("com.example.broadcast.LOCAL_BROADCAST");
                            intent.putExtra("position",position);
                            switch (item.getItemId()) {
                                case R.id.delete_this_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_DELETE_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.change_task_state:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_CHANGE_TASK_STATE);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.copy_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    return true;
                                case R.id.cut_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    intent.putExtra("do_which", MENU_CUT_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.insert_a_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_INSERT_A_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        } else if(taskKind == TaskKind.LONGTERMTASK) {
            taskViewHolder.longTermTaskNameTextView.setText((position + 1) + "   " +(((LongTermTask)taskList.get(position)).getName()));
            taskViewHolder.longTermTaskCreateTimeTextView.setText("创建时间  " + (((LongTermTask)taskList.get(position)).getCreateTime()));
            taskViewHolder.longTermTaskChildNumberTextView.setText("有" + ((LongTermTask) taskList.get(position)).longTermTaskList.size() + "个子任务");
            if(((LongTermTask) taskList.get(position)).isFinished()) taskViewHolder.longTermTaskisFinishedTextView.setText("已完成");
            else taskViewHolder.longTermTaskisFinishedTextView.setText("未完成");
            taskViewHolder.longTermTaskItemMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(getRunningActivity(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.long_term_task_popup_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                            Intent intent = new Intent("com.example.broadcast.LOCAL_BROADCAST");
                            intent.putExtra("position",position);
                            switch (item.getItemId()) {
                                case R.id.long_term_task_create_child_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_CREATE_CHILD_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.long_term_task_enter_child_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_ENTER_CHILD_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.long_term_task_delete_this_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_DELETE_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.long_term_task_change_task_state:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_CHANGE_TASK_STATE);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.long_term_task_copy_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    return true;
                                case R.id.long_term_task_cut_this_task:
                                    popupMenu.dismiss();
                                    saveTempObject(position, taskKind);
                                    intent.putExtra("do_which", MENU_CUT_THIS_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                case R.id.long_term_task_insert_a_task:
                                    popupMenu.dismiss();
                                    intent.putExtra("do_which", MENU_INSERT_A_TASK);
                                    localBroadcastManager.sendBroadcast(intent);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        taskViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myClickListener != null) {
                    myClickListener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void saveTempObject(int position, TaskKind taskKind) {
        SharedPreferences.Editor editor = context.getSharedPreferences("copy_cache", Context.MODE_PRIVATE).edit();
        try {
            editor.clear();
            editor.putString("task", SerializeAndDeserialize.serializeforTask(taskList, position));
            if(taskKind == TaskKind.TEMPORARYTASK) editor.putInt("task_kind", 0);
            else if (taskKind == TaskKind.PERIODICTASK) editor.putInt("task_kind", 1);
            else if (taskKind == TaskKind.LONGTERMTASK) editor.putInt("task_kind", 2);
        } catch (IOException e) {

            e.printStackTrace();
        }
        editor.apply();
    }
}
