package com.notfound.jphacks.shareduler;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.sqrt;
import static java.lang.System.currentTimeMillis;

/**
 * Created by owner on 2017/10/21.
 */

public class AlarmService extends Service {
    NotificationCompat.Builder n;
    NotificationManager mgr;
    // 繰り返し間隔、millisec
    long repeatPeriod = 1000 * 60 * 3; //五分
    // setWindow()でのwindow幅、milsecs(誤差)
    long windowLengthMillis = 1000 * 10;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("onStart", "起動！");
        int MinTime = 300;
        int MinDistance = 1;
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d("get_gps_listener", "おまこれPermissionとれてねーぞ");
        }
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GPSLISTENER", "GPSが利用できません");
         }
        Location location= mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = data.edit();
        editor.putFloat("longitude", (float)location.getLongitude());
        editor.putFloat("latitude",(float)location.getLatitude());
        editor.apply();
        GPSListener gpslistener = new GPSListener(getApplicationContext(), mLocationManager, MinTime, MinDistance);
        showNotification(getApplicationContext());
        n.setContentText("おしらせが表示されます");
        mgr.notify(R.layout.activity_main, n.build());
        issue();
        setNextAlarmService();

        return super.onStartCommand(intent, flags, startId);
    }

    private void setNextAlarmService() {

        Context context = getApplicationContext();
        Intent intent = new Intent(context, AlarmService.class);

        long startMillis = currentTimeMillis() + repeatPeriod;

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // SDK 19 以下ではsetを使う
        if (android.os.Build.VERSION.SDK_INT < 19) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, startMillis, windowLengthMillis, pendingIntent);
        }
    }

    private void issue() {
        Log.d("issue", "StartIssue");
        check_event_notice();

        DBHelper cdbHelper = new DBHelper(getApplicationContext());
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();

        ArrayList<Integer> Ids = get_eventId_Within3h(cdbHelper, db);
        Log.d("issue", "3h以内のイベントは" + Ids.size() + "個です");
        for (Integer id : Ids) {
            if (is_shared(id, cdbHelper, db)) {
                Log.d("issue", "id=" + id + "はシェア済みかと思うのでアップデート");
                update_serverDB(db, getApplicationContext(),id);
            } else {
                Log.d("issue", "id=" + id + "は未シェア");
            }
        }
        Log.d("issue", "issueおしまい！");

    }

    public static void update_serverDB(final SQLiteDatabase db, final Context context , final int localEventId) {

        SharedPreferences data = context.getSharedPreferences("save_data", Context.MODE_PRIVATE);
        double gps_longitude = data.getFloat("longitude", -1);
        double gps_latitude = data.getFloat("latitude", -1);
        if (gps_latitude == -1 || gps_latitude == 0 || gps_longitude == -1 || gps_longitude == 0) {
            Log.d("update_server", "GPS信号を入手できていません");
            return;
        }

        // 取得
        Cursor cursor = null;
        double longitude = 0, latitude = 0;
        int mode = 0, noticeFlag = 0;
        long t = 0;
        long a = 0;
        int _id = 0;
        String url = null;
        try {
            String query = "select id, schedule, dateMillis," +
                    " placeName, latitude , longitude ,mode," +
                    " noticeFlag ,alertTime ,url" +
                    " from calendarData" +
                    " where id= " + localEventId +//一意に定まる
                    " limit 1" +
                    " ";
            cursor = db.rawQuery(query, null);

            int indexid = cursor.getColumnIndex("id");
            int indexSchedule = cursor.getColumnIndex("schedule");
            int indexTime = cursor.getColumnIndex("dateMillis");
            int indexPlace = cursor.getColumnIndex("placeName");
            int indexlatitude = cursor.getColumnIndex("latitude");
            int indexlongitude = cursor.getColumnIndex("longitude");
            int indexmode = cursor.getColumnIndex("mode");
            int indexnoticeFlag = cursor.getColumnIndex("noticeFlag");
            int indexalertTime = cursor.getColumnIndex("alertTime");
            int indexurl = cursor.getColumnIndex("url");

            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                // 検索結果をCursorから取り出す
                String schedule = cursor.getString(indexSchedule);
                t = cursor.getLong(indexTime);
                String place = cursor.getString(indexPlace);
                latitude = cursor.getDouble(indexlatitude);
                longitude = cursor.getDouble(indexlongitude);
                mode = cursor.getInt(indexmode);
                noticeFlag = cursor.getInt(indexnoticeFlag);
                a = cursor.getLong(indexalertTime);
                _id = cursor.getInt(indexid);
                url = cursor.getString(indexurl);
            } else {
                Log.d("update_sever", "更新対象がありません．");
                return;
            }
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
        final String _url = url;
        final int _mode = mode;
        Log.d("update_server", "localEventId=" + localEventId + "をアップデートしまーす");
        get_jsonfromgoogle_and_do(context, latitude, longitude, gps_latitude, gps_longitude, mode, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int time = get_duration_from_json(response);//秒？
                int distance = get_distance_from_json(response);

                //RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                RequestQueue queue = RequestSingleton.getInstance(context).getRequestQueue();
                SharedPreferences data = context.getSharedPreferences("save_data", Context.MODE_PRIVATE);
                int userid = data.getInt("ID", -1);
                String DB_url = "http://150.95.184.107/update_list.php?" +
                        "url=" + _url +
                        "&mode=" + _mode +
                        "&distance=" + distance + //距離(m)
                        "&timeMillis=" + (time * 1000) +//移動にかかる時間(ミリ秒)
                        "&id=" + userid;
                Log.d("update_server", DB_url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, DB_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("update_server", response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("update_server", "JSON_ERRORRRRRRR");
                        return;
                    }
                }
                );
                RequestSingleton.getInstance(context).addToRequestQueue(stringRequest);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        return;
    }

    private static int get_distance_from_json(String json) {
        try {
            JSONObject json_obj = new JSONObject(json);
            //TODO:geocoder_statusを確認する
            Log.d("get_dist_from_google", json_obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONObject("distance").getString("text"));
            return json_obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONObject("distance").getInt("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int get_duration_from_json(String json) {
        try {
            JSONObject json_obj = new JSONObject(json);
            //TODO:geocoder_statusを確認する
            Log.d("get_dist_from_google", json_obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONObject("duration").getString("text"));
            return json_obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONObject("duration").getInt("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void get_jsonfromgoogle_and_do(final Context context, double latitude, double longitude,
                                                  double gps_latitude, double gps_longitude,
                                                  int mode, Response.Listener<String> listener,
                                                  Response.ErrorListener errlistener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + String.format("%.7f,%.7f", gps_latitude, gps_longitude) +
                "&destination=" + String.format("%.7f,%.7f", latitude, longitude) +
                "&mode=" + map_mode(mode) +
                "&key=AIzaSyB7-l4EegDy5hi98LkPziP6vE6vbSIKOz4";
        Log.d("get_json_from_google", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errlistener);
        RequestSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private static String map_mode(int mode) {
        switch (mode) {
            case 0:
                return "walking";
            case 1:
                return "bicycling";
            case 2:
                return "driving";
            case 3:
                return "transit";
            default:
                return "driving";
        }
    }


    private boolean is_shared(Integer id, DBHelper cdbHelper, SQLiteDatabase db) {
        Cursor cursor = null;
        String url = null;
        int _id = 0;
        try {
            String query = "select id , dateMillis," +
                    " url" +
                    " from calendarData " +
                    " where id=" + id + //必ず一意に定まる
                    " limit 1";
            cursor = db.rawQuery(query, null);
            int indexid = cursor.getColumnIndex("id");
            int indexurl = cursor.getColumnIndex("url");
            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                _id = cursor.getInt(indexid);
                url = cursor.getString(indexurl);
            } else {
                return false;
            }

        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
        if (id == _id && url != null) {//URLに文字がありゃシェア済み
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Integer> get_eventId_Within3h(DBHelper cdbHelper, SQLiteDatabase db) {
        Cursor cursor = null;
        int id = 0;
        long time = 0;
        String url = null;
        ArrayList<Integer> result = new ArrayList<Integer>();
        try {
            String query = "select id , dateMillis," +
                    " url" +
                    " from calendarData " +
                    " where ((dateMillis - " + Calendar.getInstance().getTimeInMillis() + ") >= 0) and " +
                    "((dateMillis - " + Calendar.getInstance().getTimeInMillis() + ") <= 3 * 3600 * 1000)" +
                    " order by dateMillis";
            cursor = db.rawQuery(query, null);
            int indexid = cursor.getColumnIndex("id");
            while (cursor.moveToNext()) {
                id = cursor.getInt(indexid);
                if (id != 0) result.add(id);
            }
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private void check_event_notice() {
        DBHelper cdbHelper = new DBHelper(getApplicationContext());
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();
        // 取得
        Cursor cursor = null;
        double longitude = 0, latitude = 0;
        int mode = 0, noticeFlag = 0;
        long t = 0;
        long a = 0;
        int id = 0;
        String schedule = null;
        try {
            String query = "select id, schedule, dateMillis," +
                    " placeName, latitude , longitude ,mode," +
                    " noticeFlag ,alertTime" +
                    " from calendarData" +
                    " where dateMillis >= " + Calendar.getInstance().getTimeInMillis() +
                    " order by dateMillis" +
                    " limit 1" +
                    " ";
            cursor = db.rawQuery(query, null);

            int indexid = cursor.getColumnIndex("id");
            int indexSchedule = cursor.getColumnIndex("schedule");
            int indexTime = cursor.getColumnIndex("dateMillis");
            int indexPlace = cursor.getColumnIndex("placeName");
            int indexlatitude = cursor.getColumnIndex("latitude");
            int indexlongitude = cursor.getColumnIndex("longitude");
            int indexmode = cursor.getColumnIndex("mode");
            int indexnoticeFlag = cursor.getColumnIndex("noticeFlag");
            int indexalertTime = cursor.getColumnIndex("alertTime");

            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                // 検索結果をCursorから取り出す
                schedule = cursor.getString(indexSchedule);
                t = cursor.getLong(indexTime);
                String place = cursor.getString(indexPlace);
                latitude = cursor.getDouble(indexlatitude);
                longitude = cursor.getDouble(indexlongitude);
                mode = cursor.getInt(indexmode);
                noticeFlag = cursor.getInt(indexnoticeFlag);
                a = cursor.getLong(indexalertTime);
                id = cursor.getInt(indexid);
            } else {
                Log.d("check_event_notice", "通知対象はありません．");
                return;
            }

        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }

        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        double gps_longitude = data.getFloat("longitude", -1);
        double gps_latitude = data.getFloat("latitude", -1);
        if (gps_latitude == -1 || gps_latitude == 0 || gps_longitude == -1 || gps_longitude == 0) {
            Log.d("check_event_notice", "GPS信号を入手できていません");
            return;
        }
        final DB_Record rec = new DB_Record(latitude, longitude, gps_latitude, gps_longitude, id, mode, t, a, noticeFlag);
        Log.d("check_event_notice", "イベント名:" + schedule);
        Log.d("check_event_notice", "イベント時刻:" + t);

        Log.d("check_event_notice", "longitude=" + longitude);
        Log.d("check_event_notice", "latitude=" + latitude);

        Log.d("check_event_notice", "gps_longitude=" + gps_longitude);
        Log.d("check_event_notice", "gps_latitude=" + gps_latitude);
        if (gps_latitude == 0 && gps_longitude == 0) {
            Log.d("check_event_notice", "GPSが反応しませんでした");
            return;
        }
        double dist_km = sqare_sqrt(gps_latitude - latitude, gps_longitude - longitude) * 11.1263283;//1度は11kmらしいよ(適当)

        Log.d("check_event_notice", "dist_km=" + dist_km);

        if (!check_distance(dist_km, mode)) {
            Log.d("check_event_notice", "距離条件を満たしませんでした");
            return;
        }
        get_jsonfromgoogle_and_do(getApplicationContext(), latitude, longitude, gps_latitude, gps_longitude, mode, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int dist_time_sec = get_duration_from_json(response);
                int dist_time_msec = dist_time_sec * 1000; //ミリセカンド

                Log.d("check_event_notice", "dist_time_msec=" + dist_time_msec);

                if (dist_time_sec < 0) return;//えらー？

                if (is_noticetime(dist_time_msec, rec.getNoticeFlag(), rec.getT(), rec.getA())) {
                    notice();
                    setNoticeFlag(rec.getId(), db);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        });

    }

    private void notice() {
        Toast.makeText(getApplicationContext(),
                "そろそろ出発の時間です！",
                Toast.LENGTH_LONG).show();
        n.setContentText("そろそろ出発の時間です！");
        mgr.notify(R.layout.activity_main, n.build());
    }

    private boolean setNoticeFlag(int id, SQLiteDatabase db) {
        try {
            ContentValues val = new ContentValues();
            val.put("noticeFlag", 1);
            db.update("calendarData", val, "id=" + id, null);
            return true;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "データの保存に失敗しました", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean is_noticetime(int dist_time_msec, int noticeFlag, long eventtime, long a) {
        long noticetime = eventtime - dist_time_msec - a;//イベント開始時刻-移動時間-指定時間＝通知時刻?
        Log.d("is_noticetime", "noticetime=" + noticetime);
        if (Calendar.getInstance().getTimeInMillis() >= noticetime) {
            if (noticeFlag == 0) {//まだお知らせしていなければ
                Log.d("is_noticetime", "もしかしてお知らせが必要では？");
                return true;
            }
        }
        return false;
    }

    private boolean check_distance(double dist_km, int mode) {
        double limit = 0;//km
        switch (mode) {
            case 0: //徒歩
                limit = 20;
                break;
            case 1: //チャリ
                limit = 100;
                break;
            case 2: //車
                limit = 200;
                break;
            case 3: //交通期間
                limit = 400;
                break;
        }

        if (dist_km >= limit) {
            return false;
        } else {
            return true;
        }

    }

    private double sqare_sqrt(double a, double b) {
        return sqrt(a * a + b * b);
    }

    private void showNotification(Context ctx) {
        mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(ctx, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);


        // 通知バーの内容を決める
        n = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.mipmap.icon)
                //.setTicker("サービスが起動しました。")
                .setWhen(currentTimeMillis())    // 時間
                .setContentTitle("おしらせ")
                .setContentText("お知らせが表示されます")
                .setContentIntent(contentIntent);// インテント
        //n.flags = Notification.FLAG_NO_CLEAR;

        startForeground(R.layout.activity_main, n.build());

        mgr.notify(R.layout.activity_main, n.build());
    }

    @Override
    public void onDestroy() {
        this.stopNotification(this);
        super.onDestroy();
    }

    private void stopNotification(final Context ctx) {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(R.layout.activity_main);
    }

}
