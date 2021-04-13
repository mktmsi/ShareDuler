package com.notfound.jphacks.shareduler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ShareScheduleActivity extends AppCompatActivity {
    DaySchedule ds = new DaySchedule();
    private TextView dateTextView;
    private TextView timeTextView;
    private EditText editTextSchedule;
    private TextView editTextLocation;
    private int mode = 0;
    private long alertTime = 300000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_schedule);

        timeTextView = findViewById(R.id.time);
        dateTextView = findViewById(R.id.day);
        editTextSchedule = findViewById(R.id.editSchedule);
        editTextLocation = findViewById(R.id.editLocation);
        editTextSchedule.setFocusable(false);

        final ContentValues val = new ContentValues();
        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        final int ci = data.getInt("ID", -1);
        val.put("creatorName", ci);
        val.put("mode", data.getInt("MODE", 0));
        val.put("alertTime", data.getLong("ALERT", 300000));
        mode = data.getInt("MODE", 0);
        alertTime = data.getLong("ALERT", 300000);


        // uriに「event://shareduler?url=xxxxxxxxx」が入る
        String url = "http://150.95.184.107/get_event.php?";
        Uri uri = getIntent().getData();
        // id=を取得するために"id"をgetQueryParameter
        final String u = uri.getQueryParameter("url");
        url += ("url=" + u);
        //Toast.makeText(getApplicationContext(), u, Toast.LENGTH_SHORT).show();

        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(getApplicationContext(), String.valueOf(response.getInt("dateMillis")), Toast.LENGTH_SHORT).show();
                            String schedule = response.getString("schedule");
                            long dateMillis = response.getLong("dateMillis");
                            String placeName = response.getString("placeName");
                            double latitude = response.getDouble("latitude");
                            double longitude = response.getDouble("longitude");
                            int creatorName = response.getInt("creatorName");

                            if (creatorName == ci) {
                                Toast.makeText(getApplicationContext(), "自分で作成したイベントです", Toast.LENGTH_LONG).show();
                                Intent intent;
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }


                            Date d = new Date();
                            d.setTime(dateMillis);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                            dateTextView.setText(sdf.format(d));
                            sdf = new SimpleDateFormat("HH:mm");
                            timeTextView.setText(sdf.format(d));
                            editTextLocation.setText(placeName);
                            editTextSchedule.setText(schedule);

                            ds.setSchedule(schedule);
                            ds.setTime(dateMillis);
                            ds.setLocation(placeName);
                            ds.setLat(latitude);
                            ds.setLon(longitude);
                            ds.setCreator(creatorName);
                            ds.setURL(u);

                            //Toast.makeText(getApplicationContext(), String.valueOf(ds.getLat()), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(ds.getLon()), Toast.LENGTH_SHORT).show();

                        } catch (JSONException je) {
                            Toast.makeText(getApplicationContext(), je.toString(), Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
        RequestSingleton.getInstance(this).addToRequestQueue(getRequest);

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DB
                DBHelper cdbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = cdbHelper.getWritableDatabase();
                // クリック時の処理

                val.put("schedule", ds.getSchedule());
                val.put("dateMillis", ds.getTime());
                val.put("placeName", ds.getLocation());
                val.put("url", ds.getURL());
                val.put("latitude", ds.getLat());
                val.put("longitude", ds.getLon());
                val.put("creatorName", ds.getCreator());
                val.put("noticeFlag", 0);

                try {
                    long id = db.insert( "calendarData", null, val);
                    AlarmService.update_serverDB(db, getApplicationContext(), (int)id);

                    //Toast.makeText(v.getContext(), "データを保存しました", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "データの保存に失敗しました", Toast.LENGTH_SHORT).show();
                }
                Intent intent;
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                Intent intent;
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Adapterの作成
        //移動手段
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //通知タイミング

// Adapterにアイテムを追加
        //移動手段
        adapter1.add("徒歩");
        adapter1.add("自転車");
        adapter1.add("車");
        adapter1.add("交通機関");
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        // SpinnerにAdapterを設定
        spinner1.setAdapter(adapter1);
        spinner1.setSelection(mode);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner1 = (Spinner) parent;
                // 選択したアイテムを取得
                String item = (String) spinner1.getSelectedItem();
                int mode;
                if (item == "徒歩") {
                    mode = 0;
                } else if (item == "自転車") {
                    mode = 1;
                } else if (item == "車") {
                    mode = 2;
                } else {
                    mode = 3;
                }
                val.put("mode", mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {// アイテムを選択しなかったとき
            }
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //通知タイミング
        adapter2.add("5分前");
        adapter2.add("15分前");
        adapter2.add("30分前");
        adapter2.add("60分前");
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
// SpinnerにAdapterを設定
        spinner2.setAdapter(adapter2);
        if (alertTime == 300000) {
            spinner2.setSelection(0);
        } else if (alertTime == 900000) {
            spinner2.setSelection(1);
        } else if (alertTime == 1800000) {
            spinner2.setSelection(2);
        } else {
            spinner2.setSelection(3);
        }


        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner2 = (Spinner) parent;
                // 選択したアイテムを取得
                String item = (String) spinner2.getSelectedItem();
                long tm;
                if (item == "5分前") {
                    tm = 300000;
                } else if (item == "15分前") {
                    tm = 900000;
                } else if (item == "30分前") {
                    tm = 1800000;
                } else {
                    tm = 3600000;
                }
                val.put("alertTime", tm);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {// アイテムを選択しなかったとき
            }
        });

    }

}

