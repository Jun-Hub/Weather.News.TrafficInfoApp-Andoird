
package com.example.ileem.tiiu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by ileem on 2016-11-12.
 */
public class SetActivity extends AppCompatActivity {

    //스위치 on/off 판단용 변수
    static int onOff, onOff2, onOff3;
    Switch sw, sw2, sw3;  //스위치
    int hour, minute;   //타임피커에서 받아올 시간 변수
    static Button timeBtn;
    static String ff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity);

        timeBtn = (Button) findViewById(R.id.timeBtn);

        sw = (Switch)findViewById(R.id.switch1);
        sw2 = (Switch)findViewById(R.id.switch2);
        sw3 = (Switch)findViewById(R.id.switch3);

        // 지난번 저장해놨던 스위치 상태값을 꺼내서 보여주기
        SharedPreferences sf = this.getSharedPreferences("switchState", Context.MODE_PRIVATE);
        onOff = sf.getInt("switch", 2); // 키값으로 꺼냄
        onOff2 = sf.getInt("switch2", 2);
        onOff3 = sf.getInt("switch3", 2);

        if(onOff == 1) {
            sw.setChecked(true);
        }
        else {
            sw.setChecked(false);
        }

        if(onOff2 == 1) {
            sw2.setChecked(true);
        }
        else {
            sw2.setChecked(false);
        }

        if(onOff3 == 1) {
            sw3.setChecked(true);
        }
        else {
            sw3.setChecked(false);
        }

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b)
                {
                    onOff = 1;
                    Intent intent = new Intent(SetActivity.this, ScreenService.class);
                    startService(intent);

                }
                else
                {
                    onOff = 0;
                    Intent intent = new Intent(SetActivity.this, ScreenService.class);
                    stopService(intent);
                }

            }
        });

        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                // LocationManager 객체를 얻어온다
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                if (b) {

                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {   //설정에서 위치사용이 off되어있다면
                        alertCheckGPS();    //설정에서 위치사용을 On하시겠습니까?
                    }
                    else {  //설정에서 GPS가 켜져있다면

                        onOff2 = 1;
                        Intent intent = new Intent(SetActivity.this, LocationService.class);
                        startService(intent);
                    }

                } else {
                    onOff2 = 0;
                    Intent intent = new Intent(SetActivity.this, LocationService.class);
                    stopService(intent);
                }
            }
        });

        sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                // LocationManager 객체를 얻어온다
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                if (b) {

                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {   //설정에서 위치사용이 off되어있다면
                        alertCheckGPS();    //설정에서 위치사용을 On하시겠습니까?
                    }
                    else {  //설정에서 GPS가 켜져있다면

                        onOff3 = 1;
                        Intent intent = new Intent(SetActivity.this, FineDustService.class);
                        startService(intent);
                    }

                } else {
                    onOff3 = 0;
                    Intent intent = new Intent(SetActivity.this, FineDustService.class);
                    stopService(intent);
                }
            }
        });

        //시간 설정 버튼 클릭 구현
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetActivity.this, DialogActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==1) //시간 설정 인텐트 받기
        {
            timeBtn = (Button) findViewById(R.id.timeBtn);

            hour = data.getIntExtra("시", 2);
            minute = data.getIntExtra("분", 3);

            if(hour<12)
            {//0시부터 ~ 11시라면 앞에 "오전"을 나타낸다
                if(minute==0)
                {//0분이라는 표기값 없애주기 위한 설정
                    timeBtn.setText("오전 "+hour+"시");
                }
                else
                    timeBtn.setText("오전 "+hour+"시 "+minute+"분");
            }
            else if(hour>11)
            {//12시부터 23시라면 앞에 "오후"를 나타낸다.
                if(hour==12)
                {
                    if(minute==0)
                    {//0분이라는 표기값 없애주기 위한 설정
                        timeBtn.setText("오후 12시");
                    }
                    else
                        timeBtn.setText("오후 12시 " + minute + "분");
                }
                else {
                    for (int i = 13; i < 24; i++) {//24시간제를 12시간제로 표시하기 위해 반복문을 돌려서 13시부터 24시까지 바꿔준다
                        if (hour == i) {
                            if(minute==0)
                            {
                                timeBtn.setText("오후 " + (i - 12) + "시");
                            }
                            else
                                timeBtn.setText("오후 " + (i - 12) + "시 " + minute + "분");
                        }
                    }
                }
            }

            ff = timeBtn.getText().toString();

            ff = ff.replaceAll("오전 1시", "01 :");
            ff = ff.replaceAll("오전 2시", "02 :");
            ff = ff.replaceAll("오전 3시", "03 :");
            ff = ff.replaceAll("오전 4시", "04 :");
            ff = ff.replaceAll("오전 5시", "05 :");
            ff = ff.replaceAll("오전 6시", "06 :");
            ff = ff.replaceAll("오전 7시", "07 :");
            ff = ff.replaceAll("오전 8시", "08 :");
            ff = ff.replaceAll("오전 9시", "09 :");
            ff = ff.replaceAll("오전 10시", "10 :");
            ff = ff.replaceAll("오전 11시", "11 :");
            ff = ff.replaceAll("오전 12시", "00 :");
            ff = ff.replaceAll("오후 1시", "13 :");
            ff = ff.replaceAll("오후 2시", "14 :");
            ff = ff.replaceAll("오후 3시", "15 :");
            ff = ff.replaceAll("오후 4시", "16 :");
            ff = ff.replaceAll("오후 5시", "17 :");
            ff = ff.replaceAll("오후 6시", "18 :");
            ff = ff.replaceAll("오후 7시", "19 :");
            ff = ff.replaceAll("오후 8시", "20 :");
            ff = ff.replaceAll("오후 9시", "21 :");
            ff = ff.replaceAll("오후 10시", "22 :");
            ff = ff.replaceAll("오후 11시", "23 :");
            ff = ff.replaceAll("오후 12시", "12 :");
            ff = ff.replaceAll("오전 0시", "00 :");
            ff = ff.replaceAll(" 1분", " 01 : 03");
            ff = ff.replaceAll(" 2분", " 02 : 03");
            ff = ff.replaceAll(" 3분", " 03 : 03");
            ff = ff.replaceAll(" 4분", " 04 : 03");
            ff = ff.replaceAll(" 5분", " 05 : 03");
            ff = ff.replaceAll(" 6분", " 06 : 03");
            ff = ff.replaceAll(" 7분", " 07 : 03");
            ff = ff.replaceAll(" 8분", " 08 : 03");
            ff = ff.replaceAll(" 9분", " 09 : 03");
            ff = ff.replaceAll("분", " : 03");
        }

    }

    private void alertCheckGPS() {   //설정에서 위치사용을 On하시겠습니까?
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("내 위치 정보를 사용하려면, 단말기의 설정에서 '위치 서비스' 사용을 허용해주세요.")
                .setCancelable(false)
                .setPositiveButton("설정하기",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                                sw2.setChecked(false);
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                sw2.setChecked(false);
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity 가 종료되기 전에 저장한다
        // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
        SharedPreferences sf = this.getSharedPreferences("switchState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요
        editor.putInt("switch", onOff); // 입력
        editor.putInt("switch2", onOff2);
        editor.putInt("switch3", onOff3);
        editor.commit(); // 파일에 최종 반영함
    }
}
