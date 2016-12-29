package com.donotforget.user.donotforget.objects;

import java.io.Serializable;

/**
 * Created by user on 28.06.2016.
 */
public class Schedule implements Serializable {
    private int id, textId, recurring;
    private int  playRingtone, vibrate;
    private String onceDate, fromDate, toDate, onceTime, atTime;
    private String ringtoneName, daysOfWeek, text, status;
    private String scheduleFrom;

    public String getScheduleFrom() {
        return scheduleFrom;
    }

    public void setScheduleFrom(String scheduleFrom) {
        this.scheduleFrom = scheduleFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Schedule() {
        reset();
    }

    public int getVibrate() {
        return vibrate;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTextId() {
        return textId;
    }

    public void setTextId(int textId) {
        this.textId = textId;
    }

    public int getRecurring() {
        return recurring;
    }

    public void setRecurring(int recurring) {
        this.recurring = recurring;
    }

    public void setWeekDays(String weekDays) {
        this.daysOfWeek = weekDays;
    }

    public int getPlayRingtone() {
        return playRingtone;
    }

    public void setPlayRingtone(int playRingtone) {
        this.playRingtone = playRingtone;

    }

    public String getOnceDate() {

        return onceDate;
    }

    public void setOnceDate(String onceDate, int format) {
        if(format == 0)
            this.onceDate = MyUsefulFuncs.DateToSqlFormat(onceDate);
        else
            this.onceDate = MyUsefulFuncs.DateFromSqlFormat(onceDate);
    }

    public String getFromDate() {
//        if(!fromDate.isEmpty())
//            return MyUsefulFuncs.DateFromSqlFormat(fromDate);
        return fromDate;
    }

    public void setFromDate(String fromDate, int format) {
        if(format == 0)
            this.fromDate = MyUsefulFuncs.DateToSqlFormat(fromDate);
        else
            this.fromDate = MyUsefulFuncs.DateFromSqlFormat(fromDate);
    }

    public String getToDate() {

        return toDate;
    }

    public void setToDate(String toDate, int format) {
        if(format == 0)
            this.toDate = MyUsefulFuncs.DateToSqlFormat(toDate);
        else
            this.toDate = MyUsefulFuncs.DateFromSqlFormat(toDate);
    }

    public String getOnceTime() {
        return onceTime;
    }

    public void setOnceTime(String onceTime) {
        this.onceTime = onceTime;
    }

    public String getAtTime() {
        return atTime;
    }

    public void setAtTime(String atTime) {
        this.atTime = atTime;
    }

    public String getRingtoneName() {
        return ringtoneName;
    }

    public void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String strDaysOfWeek){
        this.daysOfWeek = strDaysOfWeek;
    }
    public void setDaysOfWeek(boolean sun, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat) {
        StringBuilder stringBuilder = new StringBuilder();
        if(sun == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(mon == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(tue == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(wed == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(thu == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(fri == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");
        if(sat == false)
            stringBuilder.append("0");
        else
            stringBuilder.append("1");

        this.daysOfWeek = stringBuilder.toString();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "from=" + scheduleFrom +
                ", id='" + id + '\'' +
                "text='" + text + '\'' +
//                ", daysOfWeek='" + daysOfWeek + '\'' +
//                ", ringtoneName='" + ringtoneName + '\'' +
                ", atTime=" + atTime +
//                ", onceTime=" + onceTime +
                ", toDate=" + toDate +
                ", fromDate=" + fromDate +
//                ", onceDate=" + onceDate +
//                ", playRingtone=" + playRingtone +
//                ", recurring=" + recurring +
//                ", textId=" + textId +
                '}';
    }

    public void reset() {
        id = 0;
        textId = 0;
        recurring = 0;
        playRingtone = 0;
        vibrate = 0;
        onceDate = "";
        fromDate = "";
        toDate = "";
        onceTime = "";
        atTime = "";
        ringtoneName = "";
        daysOfWeek = "";
        text = "";
        scheduleFrom = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule))
            return false;

        Schedule schedule = (Schedule) o;

        if (getTextId() != schedule.getTextId()) {
            return false;
        }
        if (getRecurring() != schedule.getRecurring()) {
            return false;
        }
        if (getPlayRingtone() != schedule.getPlayRingtone()) {
            return false;
        }
        if (getVibrate() != schedule.getVibrate()) {
            return false;
        }
        if (getFromDate() != null ? !getFromDate().equals(schedule.getFromDate()) : schedule.getFromDate() != null) {
            return false;
        }
        if (getToDate() != null ? !getToDate().equals(schedule.getToDate()) : schedule.getToDate() != null) {
            return false;
        }
        if (getAtTime() != null ? !getAtTime().equals(schedule.getAtTime()) : schedule.getAtTime() != null) {
            return false;
        }
        if (getRingtoneName() != null ? !getRingtoneName().equals(schedule.getRingtoneName()) : schedule.getRingtoneName() != null) {
            return false;
        }
        if (getText() != null ? !getText().equals(schedule.getText()) : schedule.getText() != null) {
            return false;
        }
        if(getStatus() != null ? !getStatus().equals(schedule.getStatus()) : schedule.getStatus() != null){
           return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getTextId();
        result = 31 * result + getRecurring();
        result = 31 * result + getPlayRingtone();
        result = 31 * result + getVibrate();
        result = 31 * result + (getFromDate() != null ? getFromDate().hashCode() : 0);
        result = 31 * result + (getToDate() != null ? getToDate().hashCode() : 0);
        result = 31 * result + (getAtTime() != null ? getAtTime().hashCode() : 0);
        result = 31 * result + (getRingtoneName() != null ? getRingtoneName().hashCode() : 0);
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        return result;
    }
}
