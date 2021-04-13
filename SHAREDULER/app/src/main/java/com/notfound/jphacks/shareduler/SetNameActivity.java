package com.notfound.jphacks.shareduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class SetNameActivity extends AppCompatActivity {

    private EditText editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editName = findViewById(R.id.editName);

        findViewById(R.id.buttonName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                String text = editName.getText().toString();
                if (text.length() == 0) {
                    Toast.makeText(v.getContext(), "名前を入力してください", Toast.LENGTH_SHORT).show();
                } else {
                    int id;

                    String url = "http://150.95.184.107/insert_user.php?";
                    try {
                        String name = URLEncoder.encode(text, "utf-8");
                        url += ("name=" + name);
                        //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
                        SharedPreferences data = getSharedPreferences("save_data", Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = data.edit();
                        editor.putString("NAME", text);
                        editor.putInt("MODE", 0);
                        editor.putLong("ALERT", 300000);

                        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            //Toast.makeText(getApplicationContext(), response.getString("url"), Toast.LENGTH_SHORT).show();
                                            int id = response.getInt("id");
                                            editor.putInt("ID", id);
                                            //Toast.makeText(getApplicationContext(), String.valueOf(id) , Toast.LENGTH_SHORT).show();
                                            editor.apply();
                                            finish();
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
                        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest);

                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // バックボタンが押されたときの処理
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK && e.getAction() == KeyEvent.ACTION_DOWN) {
            // trueを返すことでbackKeyの動作を無効化
            return true;
        }

        // バックボタンじゃなかったら通常の動作をする
        return super.dispatchKeyEvent(e);
    }

}
