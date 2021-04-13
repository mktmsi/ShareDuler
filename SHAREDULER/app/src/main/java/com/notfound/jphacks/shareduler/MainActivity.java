package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    CaldroidFragment caldroidFragment = new CaldroidFragment();
    TextView dayText;
    TextView monthText;
    Date date_click = new Date();
    ListView listView;

    private void changeEvent(SQLiteDatabase db) {
        Cursor cursor = null;
        Calendar c = Calendar.getInstance();
        c.setTime(date_click);
        long s = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        long e = c.getTimeInMillis();


        try {
            String query = "select id, schedule, dateMillis, placeName" +
                    " from calendarData" +
                    " where dateMillis >= " + String.valueOf(s) +
                    " and dateMillis < " + String.valueOf(e) +
                    " order by dateMillis";
            cursor = db.rawQuery(query, null);

            int indexTime = cursor.getColumnIndex("dateMillis");


            if (cursor.moveToNext()) {
                // 検索結果をCursorから取り出す
                Drawable sld = ResourcesCompat.getDrawable(getResources(), R.drawable.mycell_event, null);
                caldroidFragment.setBackgroundDrawableForDate(sld, date_click);
            } else {
                caldroidFragment.clearBackgroundDrawableForDate(date_click);
                caldroidFragment.clearTextColorForDate(date_click);
            }

        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setEvent(SQLiteDatabase db) {
        Cursor cursor = null;
        Date d = new Date();

        try {
            String query = "select dateMillis" +
                    " from calendarData" +
                    " ";
            cursor = db.rawQuery(query, null);

            int indexTime = cursor.getColumnIndex("dateMillis");

            while (cursor.moveToNext()) {
                // 検索結果をCursorから取り出す
                long t = cursor.getLong(indexTime);
                d.setTime(t);
                Drawable sld = ResourcesCompat.getDrawable(getResources(), R.drawable.mycell_event, null);
                caldroidFragment.setBackgroundDrawableForDate(sld, d);
            }
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setList(SQLiteDatabase db, Date date, ListView listView) {
        ArrayList<DaySchedule> list = new ArrayList<>();
        MiniCalendarAdapter adapter = new MiniCalendarAdapter(getApplicationContext());
        adapter.setScheduleList(list);
        listView.setAdapter(adapter);
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM月");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd日");
        monthText.setText(sdf1.format(date));
        dayText.setText(sdf2.format(date));
        Calendar c = Calendar.getInstance();
        Cursor cursor = null;
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long s = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        long e = c.getTimeInMillis();

        try {
            String query = "select id, schedule, dateMillis, placeName" +
                    " from calendarData" +
                    " where dateMillis >= " + String.valueOf(s) +
                    " and dateMillis < " + String.valueOf(e) +
                    " order by dateMillis";
            cursor = db.rawQuery(query, null);

            int indexID = cursor.getColumnIndex("id");
            int indexSchedule = cursor.getColumnIndex("schedule");
            int indexTime = cursor.getColumnIndex("dateMillis");
            int indexPlace = cursor.getColumnIndex("placeName");

            while (cursor.moveToNext()) {
                // 検索結果をCursorから取り出す
                int id = cursor.getInt(indexID);
                String schedule = cursor.getString(indexSchedule);
                long t = cursor.getLong(indexTime);
                String location = cursor.getString(indexPlace);
                DaySchedule ds = new DaySchedule();
                ds.setTime(t);
                ds.setSchedule(schedule);
                if (location == null) {
                    ds.setLocation(" ");
                } else {
                    ds.setLocation(location);
                }
                list.add(ds);
            }
            adapter.setScheduleList(list);
            adapter.notifyDataSetChanged();
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------------------------------------------------------------
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            int requestCode = 1000;
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
        startService(new Intent(MainActivity.this, AlarmService.class));
        //--------------------------------------------------------------------

        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        String name = data.getString("NAME", "");
        if (name.length() == 0) {
            Intent intent;
            intent = new Intent(this, SetNameActivity.class);
            startActivity(intent);
        }

        dayText = findViewById(R.id.date);
        monthText = findViewById(R.id.month);

        // DB
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getReadableDatabase();

        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidTheme);
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();


        listView = (ListView) findViewById(R.id.miniListView);

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                setList(db, date, listView);

                SimpleDateFormat sdf1 = new SimpleDateFormat("MM月");
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd日");
                monthText.setText(sdf1.format(date));
                dayText.setText(sdf2.format(date));

                //TODO:ノーてふぃケーションを消すテスト
                //stopService(new Intent(getApplicationContext(), AlarmService.class));

            }

            @Override
            public void onChangeMonth(int month, int year) {
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Intent intent;
                intent = new Intent(view.getContext(), CalendarListActivity.class);
                date_click.setTime(date.getTime());
                intent.putExtra("TIME", date.getTime());
                startActivity(intent);
            }

            @Override
            public void onCaldroidViewCreated() {
            }

        };


        caldroidFragment.setCaldroidListener(listener);
        setEvent(db);
        changeEvent(db);
        setList(db, date_click, listView);
        caldroidFragment.refreshView();


    }

    @Override
    protected void onResume() {
        super.onResume();  // Always call the superclass method first

        // DB
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getReadableDatabase();

        changeEvent(db);
        setList(db, date_click, listView);
        caldroidFragment.refreshView();
    }

    public void editPref(View view) {
        Intent intent;
        intent = new Intent(this, EditPrefActivity.class);
        startActivity(intent);
    }

}
