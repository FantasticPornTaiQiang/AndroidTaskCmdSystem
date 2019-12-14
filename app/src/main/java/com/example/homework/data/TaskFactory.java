package com.example.homework.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskFactory implements Serializable{

    private int id;

    private String name;

    @SerializedName("task kind")
    public TaskKind taskKind;

    public List<?> taskList;

    public TaskFactory(String name, TaskKind taskKind) {
        this.taskKind = taskKind;
        this.name = name;

        switch (taskKind) {
            case TEMPORARYTASK:
                taskList = new ArrayList<TemporaryTask>();
                break;
            case PERIODICTASK:
                taskList = new ArrayList<PeriodicTask>();
                break;
            case LONGTERMTASK:
                taskList = new ArrayList<LongTermTask>();
                break;
            default:
                break;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
