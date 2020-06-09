
package com.example.ileem.tiiu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ileem.tiiu.data.Channel;
import com.example.ileem.tiiu.data.Item;
import com.example.ileem.tiiu.service.WeatherServiceCallback;
import com.example.ileem.tiiu.service.YahooWeatherService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by ileem on 2016-11-05.
 */
public class Fragment1_1 extends Fragment implements WeatherServiceCallback {

    private ImageView weatherIconImageView;
    private TextView temperatureTextView;
    private TextView conditionTextView;
    private TextView locationTextView;
    private Drawable weatherIconDrawable;
    public Activity activity;

    private YahooWeatherService service;

    ImageButton updateBtn; // 날씨 업뎃 버튼
    LocationManager lm;

    private boolean updated = false;    //날씨 업뎃이 됬나 안됬나 확인하는 변수

    String con, lo, tem;  // Sharedpreferences에 저장할 날씨와 위치
    String address; //지오코더로부터 얻은 한글주소~

    //weatherIconDrawable의 resourceId
    int resourceId;

    public static Fragment1_1 newInstance() {  //잠금화면 액티비티에 보여줄 프래그먼트 생성자
        Fragment1_1 fragment = new Fragment1_1();
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1_1, container, false);

        weatherIconImageView = (ImageView) v.findViewById(R.id.weatherIconImageView);
        temperatureTextView = (TextView) v.findViewById(R.id.temperatureTextView);
        conditionTextView = (TextView) v.findViewById(R.id.conditionTextView);
        locationTextView = (TextView) v.findViewById(R.id.locationTextView);

        // 지난번 저장해놨던 사용자 입력값을 꺼내서 보여주기
        SharedPreferences sf = this.getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);
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

        conditionTextView.setVisibility(View.GONE);

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

        updateBtn = (ImageButton) v.findViewById(R.id.updateButton);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //위치정보 권한을 거부한뒤, 현재날씨 업뎃버튼을 눌렀을때
                //(GPS가 켜져잇더라도 날씨업뎃이 안되니까) 다시한번 권한 승인받기
                checkPermission();

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

        return v;

    }

    @Override  //YahooWeatherService 오버라이드
    public void serviceSuccess(Channel channel) {

        //dialog.hide();

        Item item = channel.getItem();  //channel 안에 있는 item 생성
        com.example.ileem.tiiu.data.Location location = channel.getLocation();  //channel 안에 있는 location 생성

        //JSON파일 내에서 code값 추출하기
        if(isAdded()) {
           resourceId = getResources().getIdentifier("drawable/icon_" + item.getCondition().getCode()
                    , null, getActivity().getPackageName());

            Log.e("fragment1_1", "2:" + resourceId);

           // @SuppressWarnings("deprecation")  //code값에 맞는 drawable 대입
                   weatherIconDrawable = getResources().getDrawable(resourceId);

            //code값에 맞는(상황에 맞는) 날씨 이미지뷰로 셋
            weatherIconImageView.setImageDrawable(weatherIconDrawable);
        }

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

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            address = getAddress(activity, latitude, longitude);

            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                lm.removeUpdates(mLocationListener);
            } catch (SecurityException ex){
            }

            service = new YahooWeatherService(Fragment1_1.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
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

    /**
     * 퍼미션 체크
     */
    private void checkPermission(){

            /* 사용자의 OS 버전이 마시멜로우 이상인지 체크한다. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    /* 사용자 단말기의 권한 중 "위치사용" 권한이 허용되어 있는지 체크한다.
                    *  int를 쓴 이유? 안드로이드는 C기반이기 때문에, Boolean 이 잘 안쓰인다.
                    */
            int permissionResult = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

                    /* ACCESS_FINE_LOCATION의 권한이 없을 때 */
            // 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
            if (permissionResult == PackageManager.PERMISSION_DENIED) {

                        /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                        * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                        */
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 \"위치정보\" 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                                    }

                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }

                //최초로 권한을 요청할 때
                else {
                    // ACCESS_FINE_LOCATION 권한을 Android OS 에 요청한다.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                }

            }
                    /* ACCESS_FINE_LOCATION의 권한이 있을 때 */
            else {

            }

        }
                /* 사용자의 OS 버전이 마시멜로우 이하일 떄 */
        else {

        }
    }

    /**
     * 사용자가 권한을 허용했는지 거부했는지 체크
     * @param requestCode   1000번
     * @param permissions   개발자가 요청한 권한들
     * @param grantResults  권한에 대한 응답들
     *                    permissions와 grantResults는 인덱스 별로 매칭된다.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {

            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
                내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
            }
            else {
                Toast.makeText(getContext(), "앱 설정에서 권한을 허용해야 위치정보 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onPause() {

        super.onPause();

        Log.e("Fragment1_1", "onPause");

        // Activity 가 종료되기 전에 저장한다
        // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
        SharedPreferences sf = this.getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);
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

    public static String getAddress(Context mContext,double lat, double lng) {  //한글 주소 얻기~
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
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

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = activity;
    }
}