
package com.example.ileem.tiiu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ileem.tiiu.data.Channel;
import com.example.ileem.tiiu.data.Item;
import com.example.ileem.tiiu.service.WeatherServiceCallback;
import com.example.ileem.tiiu.service.YahooWeatherService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ileem on 2016-11-12.
 */
public class Activity1_1 extends AppCompatActivity implements WeatherServiceCallback {

    private ImageView weatherIconImageView;
    private TextView temperatureTextView;
    private TextView conditionTextView;
    private TextView locationTextView;
    private Drawable weatherIconDrawable;
    private LinearLayout linearLayout;
    private TextView timeTextView, timeTextView2;

    private String strNow, strNow2, strNow3;

    private YahooWeatherService service;

    ImageButton updateBtn; // 날씨 업뎃 버튼
    LocationManager lm;

    private boolean updated = false;    //날씨 업뎃이 됬나 안됬나 확인하는 변수

    String con, lo, tem;  // Sharedpreferences에 저장할 날씨와 위치
    String address; //지오코더로부터 얻은 한글주소~

    //weatherIconDrawable의 resourceId
    int resourceId;

    //잠근화면 해제 터치이벤트 위치값 변수
    float xDelta, yDelta;

    public static Fragment1_1 newInstance() {  //잠금화면 액티비티에 보여줄 프래그먼트 생성자
        Fragment1_1 fragment = new Fragment1_1();
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1_1);

        weatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        conditionTextView = (TextView) findViewById(R.id.conditionTextView);
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        timeTextView = (TextView)findViewById(R.id.time);
        timeTextView2 = (TextView)findViewById(R.id.time2);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        mHandler.sendEmptyMessage(0); // 시간표시 핸들러에 메세지 전달

        conditionTextView.setVisibility(View.GONE);

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        xDelta = (int) (X - linearLayout.getTranslationX());
                        yDelta = (int) (Y - linearLayout.getTranslationY());
                        break;
                    case MotionEvent.ACTION_UP:
                        finish();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        linearLayout.setTranslationX(X - xDelta);
                        linearLayout.setTranslationY(Y - yDelta);
                        break;
                }

                return true;
            }
        });

        // 지난번 저장해놨던 사용자 입력값을 꺼내서 보여주기
        SharedPreferences sf = getSharedPreferences("weather", Context.MODE_PRIVATE);
        Boolean updated2 = sf.getBoolean("updated", false);
        String cond = sf.getString("condition", "날씨"); // 키값으로 꺼냄
        String loca = sf.getString("location", "위치");
        String temp = sf.getString("temperature", "18℃");

        if(updated2) {  //날씨업뎃이 되었더라면
            int resource_id = sf.getInt("weatherIconResource", R.drawable.icon_30);
            Log.e("fragment1_1", "1: " + resource_id);
            Drawable weatherDrawable = getResources().getDrawable(resource_id); // 저장되있던 resourceId를 Drawable에 넣기
            weatherIconImageView.setImageDrawable(weatherDrawable);
        }
        else {  //날씨 업뎃이 안되고 onPause()됬을 경우
            String bitmapString = sf.getString("weatherIconResource_", "없음");
            if(!bitmapString.equals("없음")) {
                Bitmap bitmap = StringToBitMap(bitmapString);
                weatherIconImageView.setImageBitmap(bitmap);
            }
        }
        conditionTextView.setText(cond); // TextView에 반영함
        locationTextView.setText(loca);
        temperatureTextView.setText(temp);

        updated = false;    //아직 날씨업뎃 안됬으니 초기화

        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.

        // LocationManager 객체를 얻어온다
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //GPS가 켜져있다면
            try {  //따로 업뎃버튼 안눌러도 GPS가 켜져있다면
                // 프래그먼트가 보여지자마자 날씨 업뎃해주기
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

        updateBtn = (ImageButton)findViewById(R.id.updateButton);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {   //설정에서 위치사용이 off되어있다면
                    alertCheckGPS();    //설정에서 위치사용을 On하시겠습니까?
                }
                else {  //설정에서 GPS가 켜져있다면
                    try {
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
        });

    }

    @Override  //YahooWeatherService 오버라이드
    public void serviceSuccess(Channel channel) {

        //dialog.hide();

        Item item = channel.getItem();  //channel 안에 있는 item 생성
        com.example.ileem.tiiu.data.Location location = channel.getLocation();  //channel 안에 있는 location 생성

        //JSON파일 내에서 code값 추출하기
            resourceId = getResources().getIdentifier("drawable/icon_" + item.getCondition().getCode()
                    , null, getPackageName());

            Log.e("fragment1_1", "2:" + resourceId);

            // @SuppressWarnings("deprecation")  //code값에 맞는 drawable 대입
            weatherIconDrawable = getResources().getDrawable(resourceId);

            //code값에 맞는(상황에 맞는) 날씨 이미지뷰로 셋
            weatherIconImageView.setImageDrawable(weatherIconDrawable);


        temperatureTextView.setText(item.getCondition().getTemperature() + "\u00B0 " + channel.getUnits().getTemperature());
        //섭씨로 바꿔준 item내의 condition내의
        // temperature  와  channel의 unit의 temperature = C
        conditionTextView.setText(item.getCondition().getDescription());
        locationTextView.setText(location.getCity() + ", " + location.getRegion());
        locationTextView.setText(address);

        //Sharedpreferences에 저장할 값들
        con = item.getCondition().getDescription();
        //lo = location.getCity() + ", " + location.getRegion();
        lo = address;
        tem = item.getCondition().getTemperature() + "\u00B0 " + channel.getUnits().getTemperature();

        updated = true;
    }

    @Override  //YahooWeatherService 오버라이드
    public void serviceFailure(Exception exception) {
        //dialog.hide();
        Log.e("에러다 에러", exception.getMessage());
    }

    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.e("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.


            address = getAddress(Activity1_1.this, latitude, longitude);
            Log.e("sdsh42324", " " + address);

            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                lm.removeUpdates(mLocationListener);
            } catch (SecurityException ex){
            }

            service = new YahooWeatherService(Activity1_1.this);
            service.refreshWeather("(" + latitude + ", " + longitude + ")"); //위도,경도 정보로 날씨서비스 시작

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

    private void alertCheckGPS() {   //설정에서 위치사용을 On하시겠습니까?
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("내 위치 정보를 사용하려면, 단말기의 설정에서 '위치 서비스' 사용을 허용해주세요.")
                .setCancelable(false)
                .setPositiveButton("설정하기",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveConfigGPS();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // GPS 설정화면으로 이동
    private void moveConfigGPS() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.e("Fragment1_1", "onPause");

        // Activity 가 종료되기 전에 저장한다
        // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
        SharedPreferences sf = this.getSharedPreferences("weather", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        if(updated) {
            editor.putBoolean("updated", updated);
            editor.putString("condition", con); // 입력
            editor.putString("location", lo);
            editor.putString("temperature", tem);
            Log.e("fragment1_1", "3: " + resourceId);
            editor.putInt("weatherIconResource", resourceId);
            editor.commit(); // 파일에 최종 반영함
        }
        else {
            editor.putBoolean("updated", updated);
            editor.putString("condition", conditionTextView.getText().toString());
            editor.putString("location", locationTextView.getText().toString());
            editor.putString("temperature", temperatureTextView.getText().toString());
            Bitmap bitmap = ((BitmapDrawable)weatherIconImageView.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString = BitMapToString(bitmap); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource_", bitmapString);    //변환한 스트링 저장
            editor.commit();
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    private String doDayOfWeek() {  //요일구하는 매소드
        Calendar cal = Calendar.getInstance();
        String strWeek = null;

        int nWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (nWeek == 1) {
            strWeek = "일요일";
        } else if (nWeek == 2) {
            strWeek = "월요일";
        } else if (nWeek == 3) {
            strWeek = "화요일";
        } else if (nWeek == 4) {
            strWeek = "수요일";
        } else if (nWeek == 5) {
            strWeek = "목요일";
        } else if (nWeek == 6) {
            strWeek = "금요일";
        } else if (nWeek == 7) {
            strWeek = "토요일";
        }

        return strWeek;
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // 현재 시간을 msec으로 구한다.
            long now = System.currentTimeMillis();
            // 현재 시간을 저장 한다.
            Date date = new Date(now);
            // 시간 포맷으로 만든다.
            SimpleDateFormat sdfNow = new SimpleDateFormat("MM월 dd일");
            SimpleDateFormat sdfNow2 = new SimpleDateFormat("HH : mm");
            strNow = sdfNow.format(date);
            strNow2 = sdfNow2.format(date);
            strNow3 = doDayOfWeek();

            timeTextView.setText(strNow2);
            timeTextView2.setText(strNow +" "+ strNow3);

            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static String getAddress(Context mContext,double lat, double lng) {  //한글 주소 얻기~
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List <Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;   //풀주소

                    Address a = address.get(0); //시,구 주소만
                    String adrr = a.getLocality() + " " + a.getThoroughfare();

                    nowAddress = adrr;

                }
            }

        } catch (IOException e) {
            Log.e("Error", "주소를 가져 올 수 없습니다.");

            e.printStackTrace();
        }
        return nowAddress;
    }


}