
package com.example.ileem.tiiu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ileem.tiiu.data.Channel;
import com.example.ileem.tiiu.data.Item;
import com.example.ileem.tiiu.service.WeatherServiceCallback;
import com.example.ileem.tiiu.service.YahooWeatherService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by ileem on 2016-11-05.
 */
public class Fragment1_2 extends Fragment implements WeatherServiceCallback {

    private ImageView weatherIconImageView1;
    private TextView maxTempTextView1;
    private TextView minTempTextView1;   //오늘

    private ImageView weatherIconImageView2;
    private TextView dayTextView2;
    private TextView minMaxTempTextView2; //내일

    private ImageView weatherIconImageView3;
    private TextView dayTextView3;
    private TextView minMaxTempTextView3; //3일차

    private ImageView weatherIconImageView4;
    private TextView dayTextView4;
    private TextView minMaxTempTextView4; //4일차

    private ImageView weatherIconImageView5;
    private TextView dayTextView5;
    private TextView minMaxTempTextView5; //5일차

    private ImageView weatherIconImageView6;
    private TextView dayTextView6;
    private TextView minMaxTempTextView6; //6일차

    private ImageView weatherIconImageView7;
    private TextView dayTextView7;
    private TextView minMaxTempTextView7; //7일차

    private YahooWeatherService service;

    LocationManager lm;

    private boolean updated = false;    //날씨 업뎃이 됬나 안됬나 확인하는 변수

    int code11, high11, low11;  //1~7일차 JSON 정보값을 담을 변수 생성
    int code22, high22, low22;
    int code33, high33, low33;
    int code44, high44, low44;
    int code55, high55, low55;
    int code66, high66, low66;
    int code77, high77, low77;
    String date11, day11;
    String date22, day22;
    String date33, day33;
    String date44, day44;
    String date55, day55;
    String date66, day66;
    String date77, day77;
    int resourceId1, resourceId2, resourceId3, resourceId4, resourceId5, resourceId6, resourceId7;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1_2, container, false);

        weatherIconImageView1 = (ImageView)v.findViewById(R.id.weatherIconImageView);
        maxTempTextView1 = (TextView)v.findViewById(R.id.maxTempText);
        minTempTextView1 = (TextView)v.findViewById(R.id.minTempText);

        weatherIconImageView2 = (ImageView)v.findViewById(R.id.iconImage2);
        dayTextView2 = (TextView)v.findViewById(R.id.dateText2);
        minMaxTempTextView2 = (TextView)v.findViewById(R.id.tempText2);

        weatherIconImageView3 = (ImageView)v.findViewById(R.id.iconImage3);
        dayTextView3 = (TextView)v.findViewById(R.id.dateText3);
        minMaxTempTextView3 = (TextView)v.findViewById(R.id.tempText3);

        weatherIconImageView4 = (ImageView)v.findViewById(R.id.iconImage4);
        dayTextView4 = (TextView)v.findViewById(R.id.dateText4);
        minMaxTempTextView4 = (TextView)v.findViewById(R.id.tempText4);

        weatherIconImageView5 = (ImageView)v.findViewById(R.id.iconImage5);
        dayTextView5 = (TextView)v.findViewById(R.id.dateText5);
        minMaxTempTextView5 = (TextView)v.findViewById(R.id.tempText5);

        weatherIconImageView6 = (ImageView)v.findViewById(R.id.iconImage6);
        dayTextView6 = (TextView)v.findViewById(R.id.dateText6);
        minMaxTempTextView6 = (TextView)v.findViewById(R.id.tempText6);

        weatherIconImageView7 = (ImageView)v.findViewById(R.id.iconImage7);
        dayTextView7 = (TextView)v.findViewById(R.id.dateText7);
        minMaxTempTextView7 = (TextView)v.findViewById(R.id.tempText7);

        // 지난번 저장해놨던 사용자 입력값을 꺼내서 보여주기
        SharedPreferences sf = this.getActivity().getSharedPreferences("weather2", Context.MODE_PRIVATE);
        Boolean updated2 = sf.getBoolean("updated", false);

        String dayText2 = sf.getString("dayText2", "Sat"); // 키값으로 꺼냄
        String dayText3 = sf.getString("dayText3", "Sun"); // 키값으로 꺼냄
        String dayText4 = sf.getString("dayText4", "Mon");
        String dayText5 = sf.getString("dayText5", "Tue");
        String dayText6 = sf.getString("dayText6", "Wed");
        String dayText7 = sf.getString("dayText7", "Thu");

        String maxTemp1 = sf.getString("maxTemp1", "18℃");
        String minTemp1 = sf.getString("minTemp1", "6℃");

        String minMaxTemp2 = sf.getString("minMaxTemp2", "22℃/7℃");
        String minMaxTemp3 = sf.getString("minMaxTemp3", "22℃/7℃");
        String minMaxTemp4 = sf.getString("minMaxTemp4", "22℃/7℃");
        String minMaxTemp5 = sf.getString("minMaxTemp5", "22℃/7℃");
        String minMaxTemp6 = sf.getString("minMaxTemp6", "22℃/7℃");
        String minMaxTemp7 = sf.getString("minMaxTemp7", "22℃/7℃");

        if(updated2) {  //날씨업뎃이 되었더라면
            int weatherIconResource1 = sf.getInt("weatherIconResource1", R.drawable.icon_30);
            int weatherIconResource2 = sf.getInt("weatherIconResource2", R.drawable.icon_30);
            int weatherIconResource3 = sf.getInt("weatherIconResource3", R.drawable.icon_30);
            int weatherIconResource4 = sf.getInt("weatherIconResource4", R.drawable.icon_30);
            int weatherIconResource5 = sf.getInt("weatherIconResource5", R.drawable.icon_30);
            int weatherIconResource6 = sf.getInt("weatherIconResource6", R.drawable.icon_30);
            int weatherIconResource7 = sf.getInt("weatherIconResource7", R.drawable.icon_30);

            Log.e("fragment1_2", "1: " + weatherIconResource1);

            Drawable weatherDrawble1 = getResources().getDrawable(weatherIconResource1); // 저장되있던 resourceId를 Drawable에 넣기
            Drawable weatherDrawble2 = getResources().getDrawable(weatherIconResource2);
            Drawable weatherDrawble3 = getResources().getDrawable(weatherIconResource3);
            Drawable weatherDrawble4 = getResources().getDrawable(weatherIconResource4);
            Drawable weatherDrawble5 = getResources().getDrawable(weatherIconResource5);
            Drawable weatherDrawble6 = getResources().getDrawable(weatherIconResource6);
            Drawable weatherDrawble7 = getResources().getDrawable(weatherIconResource7);

            weatherIconImageView1.setImageDrawable(weatherDrawble1);
            weatherIconImageView2.setImageDrawable(weatherDrawble2);
            weatherIconImageView3.setImageDrawable(weatherDrawble3);
            weatherIconImageView4.setImageDrawable(weatherDrawble4);
            weatherIconImageView5.setImageDrawable(weatherDrawble5);
            weatherIconImageView6.setImageDrawable(weatherDrawble6);
            weatherIconImageView7.setImageDrawable(weatherDrawble7);
        }
        else {  //날씨 업뎃이 안되고 onPause()됬을 경우
            String bitmapString1 = sf.getString("weatherIconResource1_", "없음");
            if(!bitmapString1.equals("없음")) {
                Bitmap bitmap1 = StringToBitMap(bitmapString1);
                weatherIconImageView1.setImageBitmap(bitmap1);

                String bitmapString2 = sf.getString("weatherIconResource2_", "없음");
                Bitmap bitmap2 = StringToBitMap(bitmapString2);
                weatherIconImageView2.setImageBitmap(bitmap2);

                String bitmapString3 = sf.getString("weatherIconResource3_", "없음");
                Bitmap bitmap3 = StringToBitMap(bitmapString3);
                weatherIconImageView3.setImageBitmap(bitmap3);

                String bitmapString4 = sf.getString("weatherIconResource4_", "없음");
                Bitmap bitmap4 = StringToBitMap(bitmapString4);
                weatherIconImageView4.setImageBitmap(bitmap4);

                String bitmapString5 = sf.getString("weatherIconResource5_", "없음");
                Bitmap bitmap5 = StringToBitMap(bitmapString5);
                weatherIconImageView5.setImageBitmap(bitmap5);

                String bitmapString6 = sf.getString("weatherIconResource6_", "없음");
                Bitmap bitmap6 = StringToBitMap(bitmapString6);
                weatherIconImageView6.setImageBitmap(bitmap6);

                String bitmapString7 = sf.getString("weatherIconResource7_", "없음");
                Bitmap bitmap7 = StringToBitMap(bitmapString7);
                weatherIconImageView7.setImageBitmap(bitmap7);
            }
        }

        dayTextView2.setText(dayText2);
        dayTextView3.setText(dayText3);
        dayTextView4.setText(dayText4);
        dayTextView5.setText(dayText5);
        dayTextView6.setText(dayText6);
        dayTextView7.setText(dayText7);

        maxTempTextView1.setText(maxTemp1);
        minTempTextView1.setText(minTemp1);

        minMaxTempTextView2.setText(minMaxTemp2);
        minMaxTempTextView3.setText(minMaxTemp3);
        minMaxTempTextView4.setText(minMaxTemp4);
        minMaxTempTextView5.setText(minMaxTemp5);
        minMaxTempTextView6.setText(minMaxTemp6);
        minMaxTempTextView7.setText(minMaxTemp7);

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
        lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //설정에서 GPS가 켜져잇다면
            try {  //따로 업뎃버튼 안눌러도 프래그먼트가 보여지자마자 날씨 업뎃해주기
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

        return v;

    }

    @Override  //YahooWeatherService 오버라이드
    public void serviceSuccess(Channel channel) {

        Item item = channel.getItem();  //channel 안에 있는 item 생성

        String day1 = item.getForecast().getDay1();   //1~7일차 JSON오브젝트 생성
        String day2 = item.getForecast().getDay2();
        String day3 = item.getForecast().getDay3();
        String day4 = item.getForecast().getDay4();
        String day5 = item.getForecast().getDay5();
        String day6 = item.getForecast().getDay6();
        String day7 = item.getForecast().getDay7();

        try
        {    //1~7일차 JSON 오브젝트를 전부 파싱하기
            JSONObject jsonObject1 = new JSONObject(day1);
            code11 = jsonObject1.getInt("code");
            date11 = jsonObject1.getString("date");
            day11 = jsonObject1.getString("day");
            high11 = jsonObject1.getInt("high");
            low11 = jsonObject1.getInt("low");

            JSONObject jsonObject2 = new JSONObject(day2);
            code22 = jsonObject2.getInt("code");
            date22 = jsonObject2.getString("date");
            day22 = jsonObject2.getString("day");
            high22 = jsonObject2.getInt("high");
            low22 = jsonObject2.getInt("low");

            JSONObject jsonObject3 = new JSONObject(day3);
            code33 = jsonObject3.getInt("code");
            date33 = jsonObject3.getString("date");
            day33 = jsonObject3.getString("day");
            high33 = jsonObject3.getInt("high");
            low33 = jsonObject3.getInt("low");

            JSONObject jsonObject4 = new JSONObject(day4);
            code44 = jsonObject4.getInt("code");
            date44 = jsonObject4.getString("date");
            day44 = jsonObject4.getString("day");
            high44 = jsonObject4.getInt("high");
            low44 = jsonObject4.getInt("low");

            JSONObject jsonObject5 = new JSONObject(day5);
            code55 = jsonObject5.getInt("code");
            date55 = jsonObject5.getString("date");
            day55 = jsonObject5.getString("day");
            high55 = jsonObject5.getInt("high");
            low55 = jsonObject5.getInt("low");

            JSONObject jsonObject6 = new JSONObject(day6);
            code66 = jsonObject6.getInt("code");
            date66 = jsonObject6.getString("date");
            day66 = jsonObject6.getString("day");
            high66 = jsonObject6.getInt("high");
            low66 = jsonObject6.getInt("low");

            JSONObject jsonObject7 = new JSONObject(day7);
            code77 = jsonObject7.getInt("code");
            date77 = jsonObject7.getString("date");
            day77 = jsonObject7.getString("day");
            high77 = jsonObject7.getInt("high");
            low77 = jsonObject7.getInt("low");

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


        if(isAdded()) {
            //JSON파일 내에서 code값 추출하기
             resourceId1 = getResources().getIdentifier("drawable/icon_" + code11, null, getActivity().getPackageName());
             resourceId2 = getResources().getIdentifier("drawable/icon_" + code22, null, getActivity().getPackageName());
             resourceId3 = getResources().getIdentifier("drawable/icon_" + code33, null, getActivity().getPackageName());
             resourceId4 = getResources().getIdentifier("drawable/icon_" + code44, null, getActivity().getPackageName());
             resourceId5 = getResources().getIdentifier("drawable/icon_" + code55, null, getActivity().getPackageName());
             resourceId6 = getResources().getIdentifier("drawable/icon_" + code66, null, getActivity().getPackageName());
             resourceId7 = getResources().getIdentifier("drawable/icon_" + code77, null, getActivity().getPackageName());

            Log.e("fragment1_2", "2: " + resourceId1);

            @SuppressWarnings("deprecation")  //code값에 맞는 drawable 대입
                    Drawable weatherIconDrawable1 = getResources().getDrawable(resourceId1);
            Drawable weatherIconDrawable2 = getResources().getDrawable(resourceId2);
            Drawable weatherIconDrawable3 = getResources().getDrawable(resourceId3);
            Drawable weatherIconDrawable4 = getResources().getDrawable(resourceId4);
            Drawable weatherIconDrawable5 = getResources().getDrawable(resourceId5);
            Drawable weatherIconDrawable6 = getResources().getDrawable(resourceId6);
            Drawable weatherIconDrawable7 = getResources().getDrawable(resourceId7);

            //code값에 맞는(상황에 맞는) 날씨 이미지뷰로 셋
            weatherIconImageView1.setImageDrawable(weatherIconDrawable1);
            weatherIconImageView2.setImageDrawable(weatherIconDrawable2);
            weatherIconImageView3.setImageDrawable(weatherIconDrawable3);
            weatherIconImageView4.setImageDrawable(weatherIconDrawable4);
            weatherIconImageView5.setImageDrawable(weatherIconDrawable5);
            weatherIconImageView6.setImageDrawable(weatherIconDrawable6);
            weatherIconImageView7.setImageDrawable(weatherIconDrawable7);
        }

        dayTextView2.setText(day22);
        dayTextView3.setText(day33);
        dayTextView4.setText(day44);
        dayTextView5.setText(day55);
        dayTextView6.setText(day66);
        dayTextView7.setText(day77);

        maxTempTextView1.setText(high11 + "°");
        minTempTextView1.setText(low11 + "°");
        minMaxTempTextView2.setText(high22 + "℃/" + low22 +"℃");
        minMaxTempTextView3.setText(high33 + "℃/" + low33 +"℃");
        minMaxTempTextView4.setText(high44 + "℃/" + low44 +"℃");
        minMaxTempTextView5.setText(high55 + "℃/" + low55 +"℃");
        minMaxTempTextView6.setText(high66 + "℃/" + low66 +"℃");
        minMaxTempTextView7.setText(high77 + "℃/" + low77 +"℃");

        updated = true; //업뎃 됬으니까 true로 바꿔주기
    }

    @Override  //YahooWeatherService 오버라이드
    public void serviceFailure(Exception exception) {
        Log.e("에러다 에러", exception.getMessage());
    }

    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                lm.removeUpdates(mLocationListener);
            } catch (SecurityException ex){
            }

            service = new YahooWeatherService(Fragment1_2.this);
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

    @Override
    public void onPause() {

        super.onPause();

        Log.e("Fragment1_2", "onPause");

        // Activity 가 종료되기 전에 저장한다
        // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
        SharedPreferences sf = this.getActivity().getSharedPreferences("weather2", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        if(updated) {   //날씨정보가 업뎃 되었었더라면
            editor.putBoolean("updated", updated);
            editor.putString("dayText2", day22); // 입력
            editor.putString("dayText3", day33);
            editor.putString("dayText4", day44);
            editor.putString("dayText5", day55);
            editor.putString("dayText6", day66);
            editor.putString("dayText7", day77);

            editor.putString("maxTemp1", high11 + "°");
            editor.putString("minTemp1", low11 + "°");

            editor.putString("minMaxTemp2", high22 + "℃/" + low22 + "℃");
            editor.putString("minMaxTemp3", high33 + "℃/" + low33 + "℃");
            editor.putString("minMaxTemp4", high44 + "℃/" + low44 + "℃");
            editor.putString("minMaxTemp5", high55 + "℃/" + low55 + "℃");
            editor.putString("minMaxTemp6", high66 + "℃/" + low66 + "℃");
            editor.putString("minMaxTemp7", high77 + "℃/" + low77 + "℃");

            editor.putInt("weatherIconResource1", resourceId1);
            Log.e("fragment1_2", "3: " + resourceId1);
            editor.putInt("weatherIconResource2", resourceId2);
            editor.putInt("weatherIconResource3", resourceId3);
            editor.putInt("weatherIconResource4", resourceId4);
            editor.putInt("weatherIconResource5", resourceId5);
            editor.putInt("weatherIconResource6", resourceId6);
            editor.putInt("weatherIconResource7", resourceId7);

            editor.commit(); // 파일에 최종 반영함
        }
        else {
            editor.putBoolean("updated", updated);
            editor.putString("dayText2", dayTextView2.getText().toString()); // 입력
            editor.putString("dayText3", dayTextView3.getText().toString());
            editor.putString("dayText4", dayTextView4.getText().toString());
            editor.putString("dayText5", dayTextView5.getText().toString());
            editor.putString("dayText6", dayTextView6.getText().toString());
            editor.putString("dayText7", dayTextView7.getText().toString());

            editor.putString("maxTemp1", maxTempTextView1.getText().toString());
            editor.putString("minTemp1", minTempTextView1.getText().toString());

            editor.putString("minMaxTemp2", minMaxTempTextView2.getText().toString());
            editor.putString("minMaxTemp3", minMaxTempTextView3.getText().toString());
            editor.putString("minMaxTemp4", minMaxTempTextView4.getText().toString());
            editor.putString("minMaxTemp5", minMaxTempTextView5.getText().toString());
            editor.putString("minMaxTemp6", minMaxTempTextView6.getText().toString());
            editor.putString("minMaxTemp7", minMaxTempTextView7.getText().toString());


            Bitmap bitmap1 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString1 = BitMapToString(bitmap1); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource1_", bitmapString1);    //변환한 스트링 저장
            Bitmap bitmap2 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString2 = BitMapToString(bitmap2); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource2_", bitmapString2);    //변환한 스트링 저장
            Bitmap bitmap3 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString3 = BitMapToString(bitmap3); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource3_", bitmapString3);    //변환한 스트링 저장
            Bitmap bitmap4 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString4 = BitMapToString(bitmap4); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource4_", bitmapString4);    //변환한 스트링 저장
            Bitmap bitmap5 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString5 = BitMapToString(bitmap5); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource5_", bitmapString5);    //변환한 스트링 저장
            Bitmap bitmap6 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString6 = BitMapToString(bitmap6); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource6_", bitmapString6);    //변환한 스트링 저장
            Bitmap bitmap7 = ((BitmapDrawable) weatherIconImageView1.getDrawable()).getBitmap();   //이미지뷰의 비트맵 추출
            String bitmapString7 = BitMapToString(bitmap7); //비트맵을 스트링으로 변환
            editor.putString("weatherIconResource7_", bitmapString7);    //변환한 스트링 저장

            editor.commit(); // 파일에 최종 반영함
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
}