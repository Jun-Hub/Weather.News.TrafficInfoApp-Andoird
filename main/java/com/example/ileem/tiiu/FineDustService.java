
package com.example.ileem.tiiu;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.ColorInt;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ileem on 2016-12-16.
 */
public class FineDustService extends Service {

    private String strNow2;
    private String time;
    private int i=0;
    private PowerManager.WakeLock sCpuWakeLock;
    private AsyncTask3 asyncTask3;

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.e("test", "서비스의 onStartCommand");

        time = SetActivity.ff;

        //asyncTask3 = new AsyncTask3();
        //asyncTask3.execute(null, null, null);

        mHandler.sendEmptyMessage(0); // 시간표시 핸들러에 메세지 전달

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        Log.d("test", "서비스의 onDestroy");

        mHandler.removeMessages(0);
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // 현재 시간을 msec으로 구한다.
            long now = System.currentTimeMillis();
            // 현재 시간을 저장 한다.
            Date date = new Date(now);
            // 시간 포맷으로 만든다.
            SimpleDateFormat sdfNow2 = new SimpleDateFormat("HH : mm : ss");
            strNow2 = sdfNow2.format(date);

            Log.e("Time : ", "" + time);
            Log.e("strNow2 : ", "" + strNow2);

            if(time.equals(strNow2)) {  //현재시간과 설정시간이 같다면

                Log.e("24624", "현재시간과 설정시간이 같다면");

                //잠금화면 깨우는 메소드
                if (sCpuWakeLock != null) {
                    return;
                }
                PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                sCpuWakeLock = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE, "hi");

                sCpuWakeLock.acquire();

                if (sCpuWakeLock != null) {
                    sCpuWakeLock.release();
                    sCpuWakeLock = null;
                }

                //노티 클릭 이벤트에 전달할 인텐트 생성
                Intent notificationIntent = new Intent(FineDustService.this, MainActivity.class);
                Intent notificationIntent2 = new Intent(FineDustService.this, LocationService.class);
                Intent notificationIntent3 = new Intent(FineDustService.this, SetActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(FineDustService.this, 27, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent2 = PendingIntent.getActivity(FineDustService.this, 75, notificationIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent3 = PendingIntent.getActivity(FineDustService.this, 11, notificationIntent3, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(FineDustService.this);
                builder.setSmallIcon(R.drawable.hell);
                builder.setContentTitle("미세먼지 농도 알림~");
                builder.setTicker("서비스 실행됨");
                builder.setContentText("49µg [보통]");
                builder.setContentIntent(pendingIntent);
                builder.setPriority(Notification.PRIORITY_MAX);
                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                builder.setAutoCancel(true);

                builder.addAction(android.R.drawable.ic_input_add, "알림끄기", pendingIntent3);
                builder.addAction(android.R.drawable.dark_header, "닫기", pendingIntent2);

                Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
                inboxStyle.addLine("미세먼지 농도 알림~");
                inboxStyle.addLine("49µg [보통]");
                inboxStyle.addLine("마스크를 굳이 착용할 필요없습니다.");
                inboxStyle.setSummaryText("닫기");
                builder.setStyle(inboxStyle);

                nm.notify(109, builder.build());

            }

            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    };

    public class AsyncTask3 extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread

        }

        @Override
        protected String doInBackground(Void... params) {


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }
}
