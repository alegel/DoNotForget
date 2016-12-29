package com.donotforget.user.donotforget.objects;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.donotforget.user.donotforget.DBObjects.DBContacts;
import com.donotforget.user.donotforget.R;

import java.util.ArrayList;

/**
 * Created by user on 05.07.2016.
 */
public class ScheduleListAdapter extends BaseAdapter {
    private ArrayList<Schedule> schedules;
    private Context context;
    private LayoutInflater layoutInflater;
    private int period = -1;

    public void setPeriod(int period) {
        this.period = period;
    }

    public ScheduleListAdapter(ArrayList<Schedule> schedules, Context context) {
        this.schedules = schedules;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Schedule getItem(int pos) {
        return schedules.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        DBContacts dbContacts = new DBContacts(context);
        String strContactName = "";
        Schedule schedule = getItem(position);
        View row;
        Holder holder;

        if(convertView == null){
            row = layoutInflater.inflate(R.layout.schedules_list,viewGroup,false);
            holder = new Holder();
            holder.tvFrom = (TextView) row.findViewById(R.id.tvFrom);
            holder.tvMessage = ((TextView)row.findViewById(R.id.tvMessage));
            holder.tvListDateFrom = ((TextView) row.findViewById(R.id.tvPhone));
            holder.tvListDateTo = ((TextView) row.findViewById(R.id.tvNotifDateTo));
            holder.tvListTime = ((TextView)row.findViewById(R.id.tvListTime));
            holder.tvSpace = ((TextView)row.findViewById(R.id.tvSpace));

            row.setTag(holder);
        }
        else{
            row = convertView;
            holder = (Holder) row.getTag();
        }

        strContactName = dbContacts.getContactName(schedule.getScheduleFrom());
        if(!strContactName.equals("")) {
            holder.tvFrom.setText(context.getResources().getString(R.string.scheduleFrom) + " " + strContactName);
        }
        holder.tvMessage.setText(schedule.getText());
        if(schedule.getRecurring() != MyUsefulFuncs.ONE_TIME) {
            holder.tvListDateFrom.setText(schedule.getFromDate());
            holder.tvSpace.setText(" -- ");
            holder.tvListDateTo.setText(schedule.getToDate());
        }
        else{
            holder.tvSpace.setText("");
            holder.tvListDateTo.setText("");
            holder.tvListDateFrom.setText(schedule.getOnceDate());
        }
        holder.tvListTime.setText(schedule.getAtTime());
        if(period == 0) {       // Only for Todays Schedules
            if (MyUsefulFuncs.compareTime(schedule) == true) {
                holder.tvListTime.setTextColor(Color.parseColor("#FFFFFFFF"));  // FF282727
            }
        }
        return row;
    }

    public ArrayList<Schedule> getSchedulesArray(){
        ArrayList<Schedule> scheduleArrayList = new ArrayList<>();
        for (int i = 0; i <schedules.size(); i++) {
            scheduleArrayList.add(schedules.get(i));
        }
        return scheduleArrayList;
    }

    static  class Holder{
        TextView tvFrom;
        TextView tvListDateFrom;
        TextView tvSpace;
        TextView tvListDateTo;
        TextView tvListTime;
        TextView tvMessage;

    }
}
