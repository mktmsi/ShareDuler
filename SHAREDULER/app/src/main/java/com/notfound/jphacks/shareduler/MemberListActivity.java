package com.notfound.jphacks.shareduler;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MemberListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);


        // 前のactivityからidの受け取り
        Intent intent = getIntent();
        final int id = intent.getIntExtra("ID", -1);


        // URLを取得

        // DB
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();

        Cursor cursor = null;
        String url = null;

        try {
            String query = "select url" +
                    " from calendarData" +
                    " where id = " + String.valueOf(id) +
                    " ";
            cursor = db.rawQuery(query, null);

            int indexURL = cursor.getColumnIndex("url");

            while (cursor.moveToNext()) {
                // 検索結果をCursorから取り出す
                url = cursor.getString(indexURL);
            }
        } finally {
            // Cursorを忘れずにcloseする
            if (cursor != null) {
                cursor.close();
            }
        }

        /*
        if (url==null){
            // urlがnullだったらローカルのイベントなのでなにもしない
        }else{
        */

        final ArrayList<MemberList> list = new ArrayList<>();
        final ListView listView = (ListView) findViewById(R.id.listView);

        final MemberAdapter adapter = new MemberAdapter(this);

        adapter.setMemberList(list);
        listView.setAdapter(adapter);

        // テーブルを得る
        String str = "http://150.95.184.107/get_list.php?";


        str += ("url=" + url);
        Log.d("urlです", str);

        final JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, str, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //Toast.makeText(getApplicationContext(), response.getString("url"), Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < response.length(); i++) {
                                //JSONオブジェクトをパースして、レコードのname属性をログ出力
                                JSONObject jsonObject = response.getJSONObject(i);
                                MemberList ml = new MemberList();
                                ml.setName(jsonObject.getString("name"));
                                ml.setMode(jsonObject.getInt("mode"));
                                ml.setDistance(jsonObject.getInt("distance"));
                                ml.setTimeMillis(jsonObject.getInt("timeMillis"));
                                Log.d("name:", jsonObject.getString("name"));
                                Log.d("mode:", jsonObject.getString("mode"));
                                Log.d("distance:", jsonObject.getString("distance"));
                                Log.d("timeMillis:", jsonObject.getString("timeMillis"));
                                Log.d("追加します", ml.getName());
                                list.add(ml);
                                adapter.notifyDataSetChanged();
                                Log.d("list adapter", "NotifyDataSetChanged!!");
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

        //s}


    }
}
