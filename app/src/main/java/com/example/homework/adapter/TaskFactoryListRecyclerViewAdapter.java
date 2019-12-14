package com.example.homework.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.homework.data.TaskFactory;
import com.example.homework.data.TaskKind;
import com.example.homework.listener.OnItemClickListener;
import com.example.homework.R;

import java.util.List;

public class TaskFactoryListRecyclerViewAdapter extends RecyclerView.Adapter {

    public static class TaskListViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskFactoryNameTextView;
        private final TextView taskFactoryKindTextView;

        public TaskListViewHolder(View itemView) {
            super(itemView);
            taskFactoryNameTextView = itemView.findViewById(R.id.task_factory_name_text_view);
            taskFactoryKindTextView = itemView.findViewById(R.id.task_factory_kind_text_view);
        }
    }

    List<TaskFactory> taskFactoryList;

    public TaskFactoryListRecyclerViewAdapter(List<TaskFactory> taskList) {
        this.taskFactoryList = taskList;

    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @Override
    public TaskListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item_layout, parent, false);
        return new TaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        TaskListViewHolder taskListViewHolder = (TaskListViewHolder) viewHolder;
        taskListViewHolder.taskFactoryNameTextView.setText((position + 1) + "   " + taskFactoryList.get(position).getName());
        if(taskFactoryList.get(position).taskKind == TaskKind.TEMPORARYTASK)
            taskListViewHolder.taskFactoryKindTextView.setText("临时任务");
        if(taskFactoryList.get(position).taskKind == TaskKind.PERIODICTASK)
            taskListViewHolder.taskFactoryKindTextView.setText("周期任务");
        if(taskFactoryList.get(position).taskKind == TaskKind.LONGTERMTASK)
            taskListViewHolder.taskFactoryKindTextView.setText("长期任务");

        taskListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myClickListener != null) {
                    myClickListener.onClick(position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return taskFactoryList.size();
    }
}
