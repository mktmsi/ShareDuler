package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<MemberList> member;

    public MemberAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setMemberList(ArrayList<MemberList> member) {
        this.member = member;
    }

    @Override
    public int getCount() {
        return member.size();
    }

    @Override
    public Object getItem(int position) {
        return member.get(position);
    }

    @Override
    public long getItemId(int position) {
        return member.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.list_member, parent, false);


        ((TextView) convertView.findViewById(R.id.name)).setText(String.valueOf(member.get(position).getName()));
        ((TextView) convertView.findViewById(R.id.distance)).setText(String.valueOf(member.get(position).getDistance() / 1000) + "km");
        ((TextView) convertView.findViewById(R.id.timeMillis)).setText(String.valueOf(member.get(position).getTimeMIllis() / 60000) + "分");
        int mode = member.get(position).getMode();
        int imageicon;
        if (mode == 0) {
            //str="歩き";
            imageicon = R.drawable.ic_run;
        } else if (mode == 2) {
            //str="車";
            imageicon = R.drawable.ic_car;
        } else if (mode == 3) {
            //str="交通機関";
            imageicon = R.drawable.ic_bus;
        } else {
            //str="自転車";
            imageicon = R.drawable.ic_bicycle;
        }
        ((ImageView) convertView.findViewById(R.id.mode)).setImageResource(imageicon);


        return convertView;
    }

}
