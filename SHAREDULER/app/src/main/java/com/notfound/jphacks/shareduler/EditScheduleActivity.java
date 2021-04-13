package com.notfound.jphacks.shareduler;


import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EditScheduleActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    int PLACE_PICKER_REQUEST = 124;
    private TextView dateTextView;
    private TextView timeTextView;
    private EditText editTextSchedule;
    private TextView editTextLocation;
    private int hour = 0;
    private int minute = 0;
    private double latitude = 200;
    private double longitude = 200;
    private int mode = 0;
    private long alertTime = 300000;
    private boolean enableEdit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        final ContentValues val = new ContentValues();
        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        int ci = data.getInt("ID", -1);
        val.put("creatorName", ci);
        val.put("mode", data.getInt("MODE", 0));
        mode = data.getInt("MODE", 0);
        alertTime = data.getLong("ALERT", 300000);
        val.put("alertTime", data.getLong("ALERT", 300000));

        // 前のactivityからidの受け取り
        Intent intent = getIntent();
        final int id = intent.getIntExtra("ID", -1);
        final long time = intent.getLongExtra("TIME", 0L);

        timeTextView = findViewById(R.id.time);
        dateTextView = findViewById(R.id.day);
        editTextSchedule = findViewById(R.id.editSchedule);
        editTextLocation = findViewById(R.id.editLocation);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date d = new Date();
        d.setTime(time);
        dateTextView.setText(sdf.format(d));

        Calendar cal = Calendar.getInstance();
        timeTextView.setText(String.format(Locale.JAPAN, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

        // DB
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();
        String url = null;

        Cursor cursor = null;

        if (id >= 0) {
            try {
                String query = "select id, schedule, dateMillis, placeName, url, mode, alertTime" +
                        " from calendarData" +
                        " where id = " + String.valueOf(id) +
                        " ";
                cursor = db.rawQuery(query, null);

                int indexSchedule = cursor.getColumnIndex("schedule");
                int indexTime = cursor.getColumnIndex("dateMillis");
                int indexPlace = cursor.getColumnIndex("placeName");
                int indexURL = cursor.getColumnIndex("url");
                int indexMode = cursor.getColumnIndex("mode");
                int indexAlert = cursor.getColumnIndex("alertTime");

                while (cursor.moveToNext()) {
                    // 検索結果をCursorから取り出す
                    String schedule = cursor.getString(indexSchedule);
                    long t = cursor.getLong(indexTime);
                    String location  = cursor.getString(indexPlace);
                    url  = cursor.getString(indexURL);
                    if(url!=null){
                        enableEdit = false;
                        editTextSchedule.setFocusable(false);
                    }
                    mode = cursor.getInt(indexMode);
                    alertTime = cursor.getLong(indexAlert);


                    editTextSchedule.setText(schedule);
                    editTextLocation.setText(location);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(t);
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                    timeTextView.setText(String.format(Locale.JAPAN, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));

                }
            } finally {
                // Cursorを忘れずにcloseする
                if (cursor != null) {
                    cursor.close();
                }
            }

        }

        final String _url = url;
        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                String text = editTextSchedule.getText().toString();
                String location = editTextLocation.getText().toString();
                if (text.length() == 0) {
                    Toast.makeText(v.getContext(), "スケジュール名を入力してください", Toast.LENGTH_SHORT).show();
                } else if (location.length() == 0) {
                    Toast.makeText(v.getContext(), "場所を入力してください", Toast.LENGTH_SHORT).show();
                } else {
                    // 既存のスケジュールの編集の場合
                    if (id >= 0) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(time);
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);
                        val.put("schedule", text);
                        val.put("dateMillis", c.getTimeInMillis());
                        val.put("placeName", location);
                        val.put("noticeFlag", 0);
                        val.put("latitude", latitude);
                        val.put("longitude", longitude);
                        try {
                            db.update("calendarData", val, "id = " + String.valueOf(id), null);
                            if(_url!=null){
                                AlarmService.update_serverDB(db, getApplicationContext(), (int)id);
                            }
                        } catch (Exception e) {
                            Toast.makeText(v.getContext(), "データの保存に失敗しました", Toast.LENGTH_SHORT).show();
                        }

                    }
                    // 新規スケジュールの作成の場合
                    else {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(time);
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);
                        val.put("schedule", text);
                        val.put("dateMillis", c.getTimeInMillis());
                        //Toast.makeText(v.getContext(), String.valueOf(c.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        val.put("placeName", location);
                        val.put("noticeFlag", 0);
                        val.put("latitude", latitude);
                        val.put("longitude", longitude);
                        try {
                            db.insert("calendarData", null, val);
                        } catch (Exception e) {
                            Toast.makeText(v.getContext(), "データの保存に失敗しました", Toast.LENGTH_SHORT).show();
                        }

                    }
                    finish();
                }
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
        spinner1.setSelection(this.mode);

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

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                finish();
            }
        });

        findViewById(R.id.setMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enableEdit) {
                    // クリック時の処理
                    try {
                        PlacePicker.IntentBuilder intentBuilder =
                                new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build((AppCompatActivity) v.getContext());
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                //Toast.makeText(getApplicationContext(), String.valueOf(latitude), Toast.LENGTH_SHORT).show();
                editTextLocation.setText(place.getName());
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (enableEdit) {
            this.hour = hourOfDay;
            this.minute = minute;
            timeTextView.setText(String.format(Locale.JAPAN, "%02d:%02d", hourOfDay, minute));
        }
    }

    public void showTimePickerDialog(View v) {
        if (enableEdit) {
            DialogFragment newFragment = new TimePick();
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }

    }


}
