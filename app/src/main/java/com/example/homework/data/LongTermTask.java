package com.example.homework.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LongTermTask extends Task implements Serializable {

    private String deadline;

    private Calendar deadlineCalendar;

    public List<LongTermTask> longTermTaskList = new ArrayList<>();

    public LongTermTask(String name, String detail, Calendar deadlineCalendar, Calendar createTimeCalendar) {
        super(name, detail, createTimeCalendar);
        this.setDeadlineCalendar(deadlineCalendar);
    }

    public void setDeadlineCalendar(Calendar deadlineCalendar){
        this.deadlineCalendar = deadlineCalendar;
        this.deadline = deadlineCalendar.get(Calendar.YEAR) + "年" + (deadlineCalendar.get(Calendar.MONTH) + 1)
                + "月" + deadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日";
    }

    public Calendar getDeadlineCalendar() {
        return deadlineCalendar;
    }

    public String getDeadline() {
        return deadline;
    }
}
