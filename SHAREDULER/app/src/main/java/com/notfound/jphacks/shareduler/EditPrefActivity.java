package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EditPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pref);

        final EditText editName = findViewById(R.id.editName);

        final SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = data.edit();

        int mode = data.getInt("MODE", 0);
        long alertTime = data.getLong("ALERT", 300000);

        String name = data.getString("NAME", "mikami");
        editName.setText(name);

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                try {
                    int id = data.getInt("ID", 0);
                    String name = editName.getText().toString();
                    editor.putString("NAME", name);
                    String n = URLEncoder.encode(name, "utf-8");
                    String url = "http://150.95.184.107/update_user.php?";


                    url += ("id=" + String.valueOf(id));
                    url += ("&name=" + name);

                    final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest);
                    editor.apply();
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                finish();
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
                editor.putInt("MODE", mode);
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
                editor.putLong("ALERT", tm);
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
    }
}
