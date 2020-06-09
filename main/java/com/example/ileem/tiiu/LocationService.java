
package com.example.ileem.tiiu;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ileem on 2016-12-16.
 */
public class LocationService extends Service {

    LocationManager lm;
    ArrayList<MarkerItem> sampleList;
    private String dangerSpot;

    ServiceThread thread;

    private PowerManager.WakeLock sCpuWakeLock;

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

        getSampleMarkerItems();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        Log.d("test", "서비스의 onDestroy");
        thread.interrupt();
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    private LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.


            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            double distance;
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            Log.e("내 위도 : " + latitude, "내 경도 : " + longitude);

            for(int i=0; i<37; i++) {

                Location locationA = new Location("point A");
                locationA.setLatitude(latitude);
                locationA.setLongitude(longitude);

                Location locationB = new Location("point B");
                locationB.setLatitude(sampleList.get(i).getPosition().latitude);
                locationB.setLongitude(sampleList.get(i).getPosition().longitude);

                distance = locationA.distanceTo(locationB);

                Log.e("distance", " : " + distance);

                if(distance < 1700) {

                    try {
                        thread.sleep(6000);
                        Log.e("6sin" , "sdf55555");
                    } catch (InterruptedException ex) {

                    }

                    dangerSpot = sampleList.get(i).getSpotname();

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
                    Intent notificationIntent = new Intent(LocationService.this, MainActivity.class);
                    Intent notificationIntent2 = new Intent(LocationService.this, LocationService.class);
                    Intent notificationIntent3 = new Intent(LocationService.this, SetActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(LocationService.this, 26, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntent2 = PendingIntent.getActivity(LocationService.this, 29, notificationIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntent3 = PendingIntent.getActivity(LocationService.this, 85, notificationIntent3, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification.Builder builder = new Notification.Builder(LocationService.this);
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.car_accident));
                    builder.setSmallIcon(R.drawable.hell);
                    builder.setContentTitle("" + dangerSpot);
                    builder.setTicker("서비스 실행됨");
                    builder.setContentText("※200m 내 보행자 무단횡단 사고 다발지역 안전운행 하세요!!");
                    builder.setContentIntent(pendingIntent);
                    builder.setPriority(Notification.PRIORITY_MAX);
                    builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    builder.setAutoCancel(true);

                    builder.addAction(android.R.drawable.btn_star_big_on, "자세히보기", pendingIntent);
                    builder.addAction(android.R.drawable.ic_input_add, "알림끄기", pendingIntent3);
                    builder.addAction(android.R.drawable.dark_header, "닫기", pendingIntent2);

                    Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
                    inboxStyle.addLine("" + dangerSpot);
                    inboxStyle.addLine("※200m 내 보행자 무단횡단 사고 다발지역※");
                    inboxStyle.addLine("안전운행 하세요!!");
                    inboxStyle.setSummaryText("자세히보기");
                    builder.setStyle(inboxStyle);

                    nm.notify(1018, builder.build());
                }
            }

            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                lm.removeUpdates(mLocationListener);
            } catch (SecurityException ex){
            }

        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    private void getSampleMarkerItems() {
        sampleList = new ArrayList();

        sampleList.add(new MarkerItem(0,new LatLng(37.5567290716902, 126.948737606357), "마포구 대흥동\n(21세기토탈웨딩 부근)", "서울 서울마포1", 8, 9, 1, 5, 3, 0));
        sampleList.add(new MarkerItem(1,new LatLng(37.5443245536362, 126.949863977542), "마포구 공덕동\n(국민서관앞삼거리 부근)", "서울 서울마포2", 6, 6, 2, 2, 2, 0));
        sampleList.add(new MarkerItem(2,new LatLng(37.516454716313, 126.907011927759), "영등포구 영등포동\n(영등포역 부근)", "서울 서울영등포1", 14, 14, 1, 12, 1, 0));
        sampleList.add(new MarkerItem(3,new LatLng(37.4801001891733, 126.904641414671), "금천구 독산동\n(구로전화국_남부순환로_진출 부근)", "서울 서울금천1", 15, 18, 0, 15, 2, 1));
        sampleList.add(new MarkerItem(4,new LatLng(37.4644617935778, 126.902888320341), "금천구 독산동\n(시흥4동사거리 부근)", "서울 서울금천2", 9, 12, 0, 6, 5, 1));
        sampleList.add(new MarkerItem(5,new LatLng(37.4487336618299, 126.902487179541), "금천구 시흥동\n(박미삼거리 부근)", "서울 서울금천3", 6, 7, 0, 4, 3, 0));
        sampleList.add(new MarkerItem(6,new LatLng(37.4588175899607, 126.898998326213), "금천구 독산동\n(금천구청입구 부근)", "서울 서울금천4", 5, 5, 1, 2, 2, 0));
        sampleList.add(new MarkerItem(7,new LatLng(37.5036985354599, 126.937295475307), "동작구 상도동\n(교보증권 부근)", "서울 서울동작1", 9, 9, 0, 7, 1, 1));
        sampleList.add(new MarkerItem(8,new LatLng(37.4997400336765, 126.928220805047), "동작구 대방동\n(신대방삼거리역 부근)", "서울 서울동작2", 7, 9, 0, 4, 5, 0));
        sampleList.add(new MarkerItem(9,new LatLng(37.4807071686018, 126.952318397377), "관악구 봉천동\n(서울대입구역_관악로_진입_2 부근)", "서울 서울관악1", 14, 14, 1, 8, 4, 1));
        sampleList.add(new MarkerItem(10,new LatLng(37.4776140547457, 126.9157308496), "관악구 신림동\n(난곡우체국앞 부근)", "서울 서울관악2", 6, 6, 0, 3, 3, 0));
        sampleList.add(new MarkerItem(11,new LatLng(37.5615252119199, 126.809667924536), "강서구 공항동\n(신한은행앞 부근)", "서울 서울강서1", 6, 7, 2, 2, 3, 0));
        sampleList.add(new MarkerItem(12,new LatLng(37.547160598084, 126.87299116651), "강서구 염창동\n(국일빌딩앞 부근)", "서울 서울강서2", 4, 4, 1, 1, 2, 0));
        sampleList.add(new MarkerItem(13,new LatLng(37.6115701113724, 127.056202936917), "성북구 장위동\n(돌곶이역 부근)", "서울 서울종암1", 10, 10, 1, 6, 3, 0));
        sampleList.add(new MarkerItem(14,new LatLng(37.6147482894029, 127.059084728978), "성북구 장위동\n(한전빌라 부근)", "서울 서울종암2", 5, 5, 1, 4, 0, 0));
        sampleList.add(new MarkerItem(15,new LatLng(37.4806150807223, 126.89154676767), "구로구 가리봉동\n(만민중앙교회 부근)",  "서울 서울구로1", 22, 25, 1, 15, 6, 3));
        sampleList.add(new MarkerItem(16,new LatLng(37.5034714368582, 126.888423832295), "구로구 구로동\n(미래초교_거리공원길_진입 부근)", "서울 서울구로2", 5, 5, 1, 2, 2, 0));
        sampleList.add(new MarkerItem(17,new LatLng(37.484465283447, 127.032645467515), "서초구 서초동\n(양재역_남부순환로_진입_2 부근)", "서울 서울서초1", 7, 7, 1, 2, 2, 2));
        sampleList.add(new MarkerItem(18,new LatLng(37.5360514913841, 126.82801488568), "양천구 신월동\n(신월사거리 부근)", "서울 서울양천1", 8, 9, 0, 5, 4, 0));
        sampleList.add(new MarkerItem(19,new LatLng(37.5331847206992, 126.863738734456), "양천구 목동\n(목동사거리 부근)", "서울 서울양천2", 6, 6, 0, 3, 2, 1));
        sampleList.add(new MarkerItem(20,new LatLng(37.5710861132345, 127.000661781317), "중구 방산동\n(종로5가 부근)", "서울 서울중부1", 15, 15, 0, 11, 2, 2));
        sampleList.add(new MarkerItem(21,new LatLng(37.6401119643965, 127.06641515064), "노원구 하계동\n(중계근린공원 부근)", "서울 서울노원1", 10, 10, 0, 8, 1, 1));
        sampleList.add(new MarkerItem(22,new LatLng(37.6455601257001, 127.070713238226), "노원구 하계동\n(중계1공단 부근)",  "서울 서울노원2", 5, 6, 0, 2, 3, 1));
        sampleList.add(new MarkerItem(23,new LatLng(37.5653821253857, 127.017102273252), "중구 신당동\n(신당역 부근)",  "서울 서울중부2", 11, 12, 0, 8, 3, 1));
        sampleList.add(new MarkerItem(24,new LatLng(37.5639175389642, 126.979604903844), "중구 소공동\n(소공로지하상가 부근)", "서울 서울남대문1", 8, 8, 2, 4, 2, 0));
        sampleList.add(new MarkerItem(25,new LatLng(37.6522051443528, 127.036907937452), "도봉구 창동\n(서영실업 부근)", "서울 서울도봉1", 9, 10, 0, 9, 1, 0));
        sampleList.add(new MarkerItem(26,new LatLng(37.6140334875563, 126.926927574549), "은평구 대조동\n(동명여고 부근)", "서울 서울은평1", 4, 4, 1, 3, 0, 0));
        sampleList.add(new MarkerItem(27,new LatLng(37.5736580987432, 127.017047953301), "종로구 창신동\n(아람보습학원 부근)","서울 서울혜화1", 11, 11, 1, 6, 3, 1));
        sampleList.add(new MarkerItem(28,new LatLng(37.5778711680209, 127.015325712013), "종로구 창신동\n(창신쌍용아파트 부근)", "서울 서울혜화2", 5, 6, 0, 2, 3, 1));
        sampleList.add(new MarkerItem(29,new LatLng(37.4923031151975, 126.989035299141), "서초구 방배동\n(함지박사거리 부근)", "서울 서울방배1", 4, 4, 0, 2, 1, 1));
        sampleList.add(new MarkerItem(30,new LatLng(37.4990143320467, 127.060901044748), "강남구 대치동\n(은마아파트입구 부근)", "서울 서울수서1", 6, 6, 1, 4, 1, 0));
        sampleList.add(new MarkerItem(31,new LatLng(37.5663327402647, 126.965977940031), "서대문구 냉천동\n(서대문아트홀_의주로_진출_2 부근)", "서울 서울서대문1", 7, 7, 2, 1, 3, 1));
        sampleList.add(new MarkerItem(32,new LatLng(37.5826068126552, 126.913137498898), "서대문구 북가좌동\n(북가좌사거리 부근)", "서울 서울서대문2", 4, 4, 0, 2, 2, 0));
        sampleList.add(new MarkerItem(33,new LatLng(37.5883638551087, 126.912772717688), "은평구 신사동\n(증산손칼국수 부근)", "서울 서울서부1", 5, 5, 2, 2, 1, 0));
        sampleList.add(new MarkerItem(34,new LatLng(37.6427130330809, 127.030773726527), "강북구 수유동\n(강북구-도봉구경계(4) 부근)","서울 서울강북1", 6, 6, 1, 5, 0, 0));
        sampleList.add(new MarkerItem(35,new LatLng(37.5405178079513, 127.071002718302), "광진구 화양동\n(건대입구역 부근)","서울 서울광진1", 5, 6, 0, 3, 2, 1));
        sampleList.add(new MarkerItem(36,new LatLng(37.556840342109, 127.079921097182), "광진구 능동\n(군자역 부근)","서울 서울광진2", 4, 5, 0, 5, 0, 0));


    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {

            try {
                Log.e("ssggh", "dgjjg");

                // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                        100, // 통지사이의 최소 시간간격 (miliSecond)
                        1, // 통지사이의 최소 변경거리 (m)
                        mLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                        100, // 통지사이의 최소 시간간격 (miliSecond)
                        1, // 통지사이의 최소 변경거리 (m)
                        mLocationListener);

            } catch (SecurityException ex) {
            }

        }
    }

    //잠금화면 깨우는 메소드
    public static boolean isScreenOn(Context context)
    {
        return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }
}
