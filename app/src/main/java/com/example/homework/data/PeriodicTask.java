package com.example.homework.data;

import java.util.Calendar;

public class PeriodicTask extends Task{

    private String startDate;

    private Calendar startDateCalendar;

    private String repetitionTimes;

    private String repetitionPeriod;

    public PeriodicTask(String name, String detail, Calendar startDateCalendar, String repetitionTimes, String repetitionPeriod, Calendar createTimeCalendar) {
        super(name, detail, createTimeCalendar);
        this.repetitionTimes = repetitionTimes;
        this.repetitionPeriod = repetitionPeriod;
        this.setStartDateCalendar(startDateCalendar);
    }

    public String getRepetitionPeriod() {
        return repetitionPeriod;
    }

    public void setRepetitionPeriod(String repetitionPeriod) {
        this.repetitionPeriod = repetitionPeriod;
    }

    public String getRepetitionTimes() {
        return repetitionTimes;
    }

    public void setRepetitionTimes(String repetitionTimes) {
        this.repetitionTimes = repetitionTimes;
    }

    public String getStartDate() {
        return startDate;
    }

    public Calendar getStartDateCalendar() {
        return startDateCalendar;
    }

    public void setStartDateCalendar(Calendar startDateCalendar) {
        this.startDateCalendar = startDateCalendar;
        this.startDate = startDateCalendar.get(Calendar.YEAR) + "年" + (startDateCalendar.get(Calendar.MONTH) + 1)
                + "月" + startDateCalendar.get(Calendar.DAY_OF_MONTH) + "日";
    }
}
