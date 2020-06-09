package com.example.ileem.tiiu;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ileem on 2016-12-28.
 */

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //추가한것
        sendNotification(remoteMessage.getData().get("message"));
    }

    private void sendNotification(String messageBody) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("FCM Push Test")
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        //노티 클릭 이벤트에 전달할 인텐트 생성
        Intent notificationIntent = new Intent(this, MainActivity.class);
        Intent notificationIntent2 = new Intent(this, LocationService.class);
        Intent notificationIntent3 = new Intent(this, SetActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 26, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 29, notificationIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent3 = PendingIntent.getActivity(this, 85, notificationIntent3, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.car_accident));
        builder.setSmallIcon(R.drawable.hell);
        builder.setContentTitle("" + "jhk");
        builder.setTicker("서비스 실행됨");
        builder.setContentText("※200m 내 보행자 무단횡단 사고 다발지역 안전운행 하세요!!");
        builder.setContentIntent(pendingIntent1);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);

        builder.addAction(android.R.drawable.btn_star_big_on, "자세히보기", pendingIntent1);
        builder.addAction(android.R.drawable.ic_input_get, "알림끄기", pendingIntent3);
        builder.addAction(android.R.drawable.dark_header, "닫기", pendingIntent2);

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
        inboxStyle.addLine("" + "hjkl");
        inboxStyle.addLine("※200m 내 보행자 무단횡단 사고 다발지역※");
        inboxStyle.addLine("안전운행 하세요!!");
        inboxStyle.setSummaryText("자세히보기");
        builder.setStyle(inboxStyle);

        nm.notify(1018, builder.build());
    }

}

