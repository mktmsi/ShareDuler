package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MiniCalendarAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<DaySchedule> scheduleList;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.JAPAN);

    public MiniCalendarAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setScheduleList(ArrayList<DaySchedule> foodList) {
        this.scheduleList = foodList;
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return scheduleList.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.mini_list_schedule, parent, false);

        ((TextView) convertView.findViewById(R.id.time)).setText(String.valueOf(sdf.format(scheduleList.get(position).getTime())));
        ((TextView) convertView.findViewById(R.id.schedule)).setText(String.valueOf(scheduleList.get(position).getSchedule()));
        ((TextView) convertView.findViewById(R.id.location)).setText("@ " + String.valueOf(scheduleList.get(position).getLocation()));


        return convertView;
    }

}
