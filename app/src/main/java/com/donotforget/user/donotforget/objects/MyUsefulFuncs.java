package com.donotforget.user.donotforget.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 21.06.2016.
 */
public class MyUsefulFuncs {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(MyUsefulFuncs.DEFAULT_DATE_FORMAT);
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat(MyUsefulFuncs.DEFAULT_TIME_FORMAT);

    public static final String ADD_CONTACT = "add_contact";
    public static final String USER_NAME = "name";
    public static final String USER_PHONE = "phone";
    public static final String USER_REG_ID = "regID";
    public static final String DELETE_SCHEDULE = "deleteSchedule";
    public static final String DELETE_SCHEDULES = "deleteSchedules";
    public static String myPhoneNumber = "";
    public static String myName  = "";
    public static String myReg_ID  = "";
    public static int registered  = 0;

    // global topic to receive app wide push notifications
    public static final String TOPIC_NEW_USER = "newUser";
    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "com.donotforget.user.donotforget.services.NEW_USER";
    public static final String PUSH_NOTIFICATION = "com.donotforget.user.donotforget.services.PUSH_NOTIFICATION";
    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;

    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String MESSAGE_TEXT = "msgText";
    public static final String MESSAGE_POSITION = "msg_pos";
    public static final String CONTACTS = "contactsList";
    public static final String ALL_SCHEDULES = "allSchedules";
    public static final String SCHEDULE = "schedule";
    public static final String CONTACT_SCHEDULES = "contactSchedules";
    public static final String GROUP_NAME = "group_name";
    public static final String GROUPS = "groups";
    public static final String SCHEDULE_OWNER = "schedule_owner";

    public static final int MENU_EDIT = 101;
    public static final int MENU_DELETE = 102;
    public static final int MENU_DELETE_REPEATING = 103;

    public static final int ONE_TIME = 0;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;
    public static final int MONTHLY = 3;
    public static final int YEARLY = 4;
    public static final int REMIND_TO_MYSELF = 0;
    public static final int REMIND_TO_CONTACT = 1;
    public static final int REMIND_TO_GROUP = 2;

    public static final StringBuilder sb = new StringBuilder();

    public static final int REQ_CODE_MESSAGES = 1;
    public static final int REQ_CODE_PREPARED_MSG = 2;
    public static final int REQ_CODE_CONTACTS = 3;
    public static final int REQ_CODE_COMPOSE_GRP = 4;


    public static ArrayList<MyContacts> contactsList = new ArrayList<>();

    public static String concat(Object... objects) {
        sb.setLength(0);
        for (Object obj : objects) {
            sb.append(obj);
        }
        return sb.toString();
    }
    public static String DateToString(int day, int month, int year){
        String date = "", strDay = "", strMonth = "";
        if(month < 10)
            strMonth = "0";
        if(day < 10)
            strDay = "0";

        date =  String.format("%s%d/%s%d/%4d",strDay, day,strMonth, month, year);

        return date;
    }

    public static String DateToSqlFormat(String date){
        String newDate = "";
        String [] parts = SplitString(date,"/");
        if(parts.length < 3) {
            return "";
        }
        newDate = concat(parts[2], "-",parts[1], "-", parts[0]);

        return newDate;
    }

    public static String DateFromSqlFormat(String date){
        String newDate = "";
        String [] parts = SplitString(date,"-");
        if(parts.length < 3) {
            return "";
        }
        newDate = concat(parts[2], "/",parts[1], "/", parts[0]);

        return newDate;
    }

    public static boolean compareTime(Schedule schedule) {
        Calendar calNow = Calendar.getInstance();
        Calendar calFromSchedule = Calendar.getInstance();
        int hour,min;
        String time;
        time = schedule.getAtTime();
        String [] parts = MyUsefulFuncs.SplitString(time,":");
        if(parts.length < 3) {
            return false;
        }
        hour = Integer.parseInt(parts[0]);
        min = Integer.parseInt(parts[1]);
        calFromSchedule.set(Calendar.HOUR_OF_DAY,hour);
        calFromSchedule.set(Calendar.MINUTE,min);
        calFromSchedule.set(Calendar.SECOND,0);
        calFromSchedule.set(Calendar.MILLISECOND,0);

        if(calFromSchedule.getTimeInMillis() < calNow.getTimeInMillis())
            return true;

        return false;
    }

    public static String TimeToString(int hour, int min){
        String h = "", m = "";
        if(hour <10)
            h = "0";
        if(min < 10)
            m = "0";
        return String.format("%s%d:%s%d:00",h,hour,m,min);
    }

    public static int getDayOfWeek(int dayOfMonth, int monthOfYear, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthOfYear,dayOfMonth);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static Calendar addDate(int dayOfMonth, int monthOfYear, int year, int data, int amount) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.set(year,monthOfYear,dayOfMonth);
        c.add(data,1);

        return c;
    }

    public static String [] SplitString(String strDate, String split){
        String[] parts = strDate.split(split);
        return parts;
    }

    public static String getMonthEnding(int day){
        String strMonthEnding;
        switch (day){
            case 1:
            case 21:
            case 31:
                strMonthEnding = "st";
                break;
            case 2:
            case 22:
                strMonthEnding = "nd";
                break;
            case 3:
            case 23:
                strMonthEnding = "rd";
                break;
            default:
                strMonthEnding = "th";
                break;
        }
        return strMonthEnding;
    }

    public static long getdateDiff(Date date1, Date date2, TimeUnit timeUnit){
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public static boolean compareIfExists(ArrayList<Schedule> schedules, Schedule schedule) {
        for (int i = 0; i <schedules.size(); i++) {
            if(schedules.get(i).equals(schedule) == true) {
                return true;
            }
        }
        return false;
    }
}
