package com.notfound.jphacks.shareduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class BackGroundService extends Service {
    // Toastを表示させるために使うハンドラ
    private Handler mHandler = new Handler();
    // スレッドを停止するために必要
    private boolean mThreadActive = true;
    private Thread mThread;
    // スレッド処理
    private Runnable mTask = new Runnable() {

        @Override
        public void run() {

            // アクティブな間だけ処理をする
            while (mThreadActive) {

            }

            showPost("スレッド終了");
        }
    };


    public BackGroundService() {
    }

    /**
     * テキストを表示する
     *
     * @param text 表示したいテキスト
     */
    private void showText(Context ctx, final String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * テキストをハンドラ経由で表示
     *
     * @param text 表示したいテキスト
     */

    private void showPost(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showText(text);
            }
        });
    }

    // ______________________________________________________________________________

    /**
     * テキストを表示する
     *
     * @param text テキスト
     */
    private void showText(final String text) {
        showText(this, text);
    }


    // ______________________________________________________________________________
    @Override   // onBind:サービスがバインドされたときに呼び出される
    public IBinder onBind(Intent intent) {
        this.showText("サービスがバインドされました。");
        return null;
    }


    // ______________________________________________________________________________
    @Override   // onStartCommand:
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.showText("onStartCommand");
        this.mThread = new Thread(null, mTask, "NortifyingService");
        this.mThread.start();

        // 通知バーを表示する
        showNotification(this);

        // 戻り値でサービスが強制終了されたときの挙動が変わる
        // START_NOT_STICKY,START_REDELIVER_INTENT,START_STICKY_COMPATIBILITY
        return START_STICKY;
    }

    // ______________________________________________________________________________
    @Override   // onCreate:サービスが作成されたときに呼びされる(最初に1回だけ)
    public void onCreate() {
        this.showText("サービスが開始されました。");
        // データベース宣言
        DBHelper cdbHelper = new DBHelper(this);
        final SQLiteDatabase db = cdbHelper.getWritableDatabase();

        // 書き込み
        ContentValues val = new ContentValues();
        val.put("schedule", "Go to heaven");
        val.put("dateMillis", System.currentTimeMillis());
        val.put("placeName", "NewYork");
        val.put("longitude", 0);
        val.put("latitude", 0);
        val.put("mode", 1);
        val.put("alertTime", 1);

        try {
            db.insert("calendarData", null, val);
            showText("データの作成に成功しました");
        } catch (Exception e) {
            showText("データの保存に失敗しました");
        }

        super.onCreate();
    }


    // ______________________________________________________________________________
    @Override   // onDestroy:
    public void onDestroy() {
        this.showText("サービスが終了されました。");

        // スレッド停止
        this.mThread.interrupt();
        this.mThreadActive = false;

        this.stopNotification(this);
        super.onDestroy();
    }

    // ______________________________________________________________________________
    // 通知バーを消す
    private void stopNotification(final Context ctx) {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(R.layout.activity_main);
    }

    // ______________________________________________________________________________
    // 通知バーを出す
    private void showNotification(final Context ctx) {

        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(ctx, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 通知バーの内容を決める
        Notification n = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("サービスが起動しました。")
                .setWhen(System.currentTimeMillis())    // 時間
                .setContentTitle("サービス起動中")
                .setContentText("このバーをタップ後に「サービス終了」を選択してください。")
                .setContentIntent(contentIntent)// インテント
                .build();
        n.flags = Notification.FLAG_NO_CLEAR;

        startForeground(R.layout.activity_main, n);

        mgr.notify(R.layout.activity_main, n);

    }
}
