package com.notfound.jphacks.shareduler;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.notfound.jphacks.shareduler.R.id.date;


public class CalendarListActivity extends AppCompatActivity {


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void resetList(SQLiteDatabase db, long time, ArrayList<DaySchedule> list) {
        list.clear();
        Cursor cursor = null;
        Date d = new Date();
        d.setTime(time);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        long s = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        long e = c.getTimeInMillis();

        try {
            String query = "select id, schedule, dateMillis, placeName, latitude, longitude, creatorName, url" +
                    " from calendarData" +
                    " where dateMillis >= " + String.valueOf(s) +
                    " and dateMillis < " + String.valueOf(e) +
                    " order by dateMillis";
            cursor = db.rawQuery(query, null);

            int indexID = cursor.getColumnIndex("id");
            int indexSchedule = cursor.getColumnIndex("schedule");
            int indexTime = cursor.getColumnIndex("dateMillis");
            int indexPlace = cursor.getColumnIndex("placeName");
            int indexLat = cursor.getColumnIndex("latitude");
            int indexLon = cursor.getColumnIndex("longitude");
            int indexCreator = cursor.getColumnIndex("creatorName");
            int indexURL = cursor.getColumnIndex("url");

            while (cursor.moveToNext()) {
                // 検索結果をCursorから取り出す
                int id = cursor.getInt(indexID);
                String schedule = cursor.getString(indexSchedule);
                long t = cursor.getLong(indexTime);
                String location = cursor.getString(indexPlace);
                double lat = cursor.getDouble(indexLat);
                double lon = cursor.getDouble(indexLon);
                int ci = cursor.getInt(indexCreator);
                String url = cursor.getString(indexURL);

                DaySchedule ds = new DaySchedule();
                ds.setID(id);
                ds.setTime(t);
                ds.setSchedule(schedule);
                ds.setLat(lat);
                ds.setLon(lon);
                ds.setURL(url);
                if (location == null) {
                    ds.setLocation(" ");
                } else {
                    ds.setLocation(location);
                }
                ds.setCreator(ci);
                list.add(ds);
            }
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void getURL(final DaySchedule ds, final SQLiteDatabase db) {
        //RequestQueue queue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        String result = "エラーが発生しました";
        String url = "http://150.95.184.107/insert_event.php?";
        String s;
        String l;

        try {
            s = URLEncoder.encode(ds.getSchedule(), "utf-8");
            l = URLEncoder.encode(ds.getLocation(), "utf-8");

            url += ("sche=" + s);

            url += ("&date=" + String.valueOf(ds.getTime()));
            url += ("&place=" + l);
            url += ("&lat=" + String.valueOf(ds.getLat()));
            //Log.d("TAG", String.valueOf(ds.getTime()), null);
            //Toast.makeText(getApplicationContext(), String.valueOf(ds.getTime()), Toast.LENGTH_SHORT).show();
            url += ("&lon=" + String.valueOf(ds.getLon()));
            url += ("&name=" + String.valueOf(ds.getCreator()));
            //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();

            final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Toast.makeText(getApplicationContext(), response.getString("url"), Toast.LENGTH_SHORT).show();
                                ContentValues val = new ContentValues();
                                val.put("url", response.getString("url"));
                                try {
                                    //Toast.makeText(getApplicationContext(), "share", Toast.LENGTH_SHORT).show();
                                    db.update("calendarData", val, "id = " + String.valueOf(ds.getID()), null);
                                    ds.setURL(response.getString("url"));
                                    // builderの生成　ShareCompat.IntentBuilder.from(Context context);
                                    ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(CalendarListActivity.this);
                                    builder.setChooserTitle("アプリを選択");
                                    builder.setSubject("イベントをシェアしましょう");
                                    builder.setText("http://150.95.184.107/share.php?url=" + response.getString("url"));
                                    builder.setType("text/plain");
                                    builder.startChooser();
                                    AlarmService.update_serverDB(db, getApplicationContext(), ds.getID());

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "データの保存に失敗しました", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException je) {
                                Toast.makeText(getApplicationContext(), je.toString(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            RequestSingleton.getInstance(this).addToRequestQueue(getRequest);

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendarlist);

        // DB
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();

        // windowの大きさの調整
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);

        getWindow().setLayout(width, height);
        // setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        // 前のactivityから時間の受け取り
        Intent intent = getIntent();
        final long time = intent.getLongExtra("TIME", 0L);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        ((TextView) findViewById(date)).setText(sdf.format(time));


        final SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.listView);

        final ArrayList<DaySchedule> list = new ArrayList<>();
        final CalendarAdapter adapter = new CalendarAdapter(this);

        adapter.setScheduleList(list);
        listView.setAdapter(adapter);

        // スワイプメニューの定義
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem editItem = new SwipeMenuItem(
                        getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.rgb(0x87, 0xce, 0xeb)));
                editItem.setWidth(dp2px(60));
                editItem.setIcon(android.R.drawable.ic_menu_edit);
                // add to menu
                menu.addMenuItem(editItem);

                SwipeMenuItem shareItem = new SwipeMenuItem(
                        getApplicationContext());
                shareItem.setBackground(new ColorDrawable(Color.rgb(0xf5, 0xf5, 0xf5)));
                shareItem.setWidth(dp2px(60));
                shareItem.setIcon(android.R.drawable.ic_menu_share);
                menu.addMenuItem(shareItem);

                SwipeMenuItem listItem = new SwipeMenuItem(
                        getApplicationContext());
                listItem.setBackground(new ColorDrawable(Color.GREEN));
                listItem.setWidth(dp2px(60));
                listItem.setIcon(android.R.drawable.ic_menu_my_calendar);
                menu.addMenuItem(listItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(dp2px(60));
                deleteItem.setIcon(android.R.drawable.ic_menu_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // edit
                        Intent intent_edit;
                        intent_edit = new Intent(getApplicationContext(), EditScheduleActivity.class);
                        intent_edit.putExtra("ID", list.get(position).getID());
                        intent_edit.putExtra("TIME", time);
                        startActivity(intent_edit);
                        break;
                    case 1:
                        // share
                        if (list.get(position).getURL() == null) {
                            new AlertDialog.Builder(CalendarListActivity.this)
                                    .setTitle("共有")
                                    .setMessage("このイベントを共有しますか？\n(共有すると編集・削除ができなくなります)")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // OK button pressed
                                            getURL(list.get(position), db);

                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

                        } else {
                            String url = list.get(position).getURL();
                            // builderの生成　ShareCompat.IntentBuilder.from(Context context);
                            ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(CalendarListActivity.this);
                            builder.setChooserTitle("アプリを選択");
                            builder.setSubject("イベントをシェアしましょう");
                            builder.setText("http://150.95.184.107/share.php?url=" + url);
                            builder.setType("text/plain");
                            builder.startChooser();

                        }
                        break;

                    case 2:
                        // list
                        if (list.get(position).getURL() == null) {
                            Toast.makeText(getApplicationContext(), "イベントを共有するとリストを表示できます", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent_list;
                            intent_list = new Intent(getApplicationContext(), MemberListActivity.class);
                            intent_list.putExtra("ID", list.get(position).getID());
                            startActivity(intent_list);
                        }
                        break;

                    case 3:
                        // delete
                        if(list.get(position).getURL()==null) {
                            db.delete("calendarData", "id = " + String.valueOf(list.get(position).getID()), null);
                            list.remove(position);
                            reload();
                        } else{
                            Toast.makeText(getApplicationContext(), "共有されたイベントは削除できません" , Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listView.setMenuCreator(creator);

        resetList(db, time, list);

        findViewById(R.id.buttonFloat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                Intent intent;
                intent = new Intent(getApplicationContext(), EditScheduleActivity.class);
                intent.putExtra("ID", -1);
                intent.putExtra("TIME", time);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRestart() {
        super.onRestart();
        reload();
    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }


}