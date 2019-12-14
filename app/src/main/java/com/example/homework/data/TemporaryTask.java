package com.example.homework.data;

import java.util.Calendar;

public class TemporaryTask extends Task {

    private String deadline;

    private Calendar deadlineCalendar;

    public TemporaryTask(String name, String detail, Calendar deadlineCalendar, Calendar createTimeCalendar) {
        super(name, detail, createTimeCalendar);
        this.setDeadlineCalendar(deadlineCalendar);
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadlineCalendar(Calendar deadlineCalendar){
        this.deadlineCalendar = deadlineCalendar;
        this.deadline = deadlineCalendar.get(Calendar.YEAR) + "年" + (deadlineCalendar.get(Calendar.MONTH) + 1)
                + "月" + deadlineCalendar.get(Calendar.DAY_OF_MONTH) + "日";
    }

    public Calendar getDeadlineCalendar() {
        return deadlineCalendar;
    }
}
