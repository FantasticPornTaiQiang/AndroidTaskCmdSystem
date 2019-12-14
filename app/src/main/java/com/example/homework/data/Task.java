package com.example.homework.data;

import java.io.Serializable;
import java.util.Calendar;

public class Task implements Serializable {

    private String createTime;

    private String name;

    private String detail;

    private boolean isFinished;

    private Calendar createTimeCalendar;

    public Task(String name, String detail, Calendar createTimeCalendar) {
        this.name = name;
        this.detail = detail;
        this.isFinished = false;
        this.setCreateTimeCalendar(createTimeCalendar);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Calendar getCreateTimeCalendar() {
        return createTimeCalendar;
    }

    public void setCreateTimeCalendar(Calendar createTimeCalendar) {
        this.createTimeCalendar = createTimeCalendar;
        String hour;
        String minute;
        String second;
        if(createTimeCalendar.get(Calendar.HOUR_OF_DAY) < 10) hour = "0" + createTimeCalendar.get(Calendar.HOUR_OF_DAY);
        else hour = createTimeCalendar.get(Calendar.HOUR_OF_DAY) + "";
        if(createTimeCalendar.get(Calendar.MINUTE) < 10) minute = "0" + createTimeCalendar.get(Calendar.MINUTE);
        else minute = createTimeCalendar.get(Calendar.MINUTE) + "";
        if(createTimeCalendar.get(Calendar.SECOND) < 10) second = "0" + createTimeCalendar.get(Calendar.SECOND);
        else second = createTimeCalendar.get(Calendar.SECOND) + "";


        StringBuilder createTimeStringBuilder = new StringBuilder();
        createTimeStringBuilder.append(createTimeCalendar.get(Calendar.YEAR)).append("-")
                .append(createTimeCalendar.get(Calendar.MONTH) + 1).append("-")
                .append(createTimeCalendar.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(hour).append(":")
                .append(minute).append(":")
                .append(second);
        this.createTime = createTimeStringBuilder.toString();

    }
}
