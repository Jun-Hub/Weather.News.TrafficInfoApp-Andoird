
package com.example.ileem.tiiu;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.navdrawer.SimpleSideDrawer;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */


    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SimpleSideDrawer mSlidingMenu;

    static int logined = 0; //자동로그인 인지 아닌지 구별해주는 변수
    private int loginNum;  //자체로그인인지 페북로그인인지 구분해주는 변수

    Bitmap bm;
    String profileId, profileName, profileEmail, pImage, pn, pe;

    //자체로그인 후 전달받을 프로필정보
    ImageView profileImage;
    TextView name, email;

    //서비스에서 getWindow()를 사용하기위해 만든 액티비티 변수
    public static Activity mActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치데이터를 사용하기 위해서는 사용자로부터 permission 사용 여부를 확인 받아야 함
        checkPermission();

        mActivity = this;

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//         Create the adapter that will return a fragment for each of the three
//         primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // 로그인 변수 값과 프로필 정보를 저장한다(자동로그인을 위해) 기억하는 SharedPreferences
        SharedPreferences sf = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        logined = sf.getInt("logined", 0);
        loginNum = sf.getInt("loginNum", 1);
        final String pi = sf.getString("profileId", "null");
        pn = sf.getString("profileName", "null");
        pe = sf.getString("profileEmail", "null");
        pImage = sf.getString("profileImage", "null");

        final Bitmap btmap = StringToBitMap(pImage);


        mSlidingMenu = new SimpleSideDrawer(this); //슬라이드 메뉴 라이브러리 생성
        mSlidingMenu.setLeftBehindContentView(R.layout.slide_menu);

        logined++;

        ImageButton slide_btn = (ImageButton)findViewById(R.id.slide_btn); //슬라이드 메뉴 생성 버튼
        slide_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingMenu.toggleLeftDrawer();

                profileImage = (ImageView)findViewById(R.id.profileImage);
                name = (TextView)findViewById(R.id.name);
                email = (TextView)findViewById(R.id.email);

                Button setBtn = (Button)findViewById(R.id.setBtn);
                //설정 버튼 클릭시
                setBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, SetActivity.class);
                        startActivity(intent);
                    }
                });


                if(logined < 2) {  //자동로그인 상태가 아니라면 = 처음 로그인 한거라면

                    Intent intent = getIntent();  //로그인 액티비티에서 어떤 경로로 로그인 했는지 구분하는 변수
                    loginNum = intent.getExtras().getInt("loginNum");

                    if (loginNum == 1) {//자체로그인 했을 경우
                        profileId = intent.getExtras().getString("ID");
                        profileImage.setImageResource(R.drawable.hell);
                        name.setText(profileId + " 님 로그인하셨습니다.");
                        email.setVisibility(View.GONE);

                        logined++; //로그인 된 상태로 만들기

                    } else if (loginNum == 2) { //페이스북 로그인 했을 경우
                        profileEmail = intent.getExtras().getString("email");
                        profileName = intent.getExtras().getString("name");
                        Bundle b = intent.getExtras();
                        bm = (Bitmap) b.get("profileImage");

                        profileImage.setImageBitmap(bm);
                        name.setText("이름 " + profileName);
                        email.setText("이메일 " + profileEmail);

                        logined++; //로그인 된 상태로 만들기

                    } else {
                        Log.e("로그인 넘버를 받아오는과정에서", "에러");
                    }
                }
                else {  //자동 로그인 상태라면
                    if(loginNum == 1) { //자체로그인 했을 경우
                        profileImage.setImageResource(R.drawable.hell);
                        name.setText(pi + " 님 로그인하셨습니다.");
                        email.setVisibility(View.GONE);

                        logined++; //로그인 된 상태로 만들기
                    }
                    else if(loginNum == 2) {    //페이스북 로그인 했을 경우
                        profileImage.setImageBitmap(btmap);
                        name.setText("이름 " + pn);
                        email.setText("이메일 " + pe);

                        logined++; //로그인 된 상태로 만들기
                    }
                }
            }
        });

        //슬라이드 메뉴 버튼 클릭하지 않아도 자동로그인 상태로 변수 ++ 하기 위해 추가한 코드
        if(logined == 0) {  //자동로그인 상태가 아니라면 = 처음 로그인 한거라면

            Intent intent = getIntent();  //로그인 액티비티에서 어떤 경로로 로그인 했는지 구분하는 변수
            loginNum = intent.getExtras().getInt("loginNum");

            if (loginNum == 1) {//자체로그인 했을 경우
                profileId = intent.getExtras().getString("ID");

                logined++; //로그인 된 상태로 만들기

            } else if (loginNum == 2) { //페이스북 로그인 했을 경우
                profileEmail = intent.getExtras().getString("email");
                profileName = intent.getExtras().getString("name");
                Bundle b = intent.getExtras();
                bm = (Bitmap) b.get("profileImage");

                logined++; //로그인 된 상태로 만들기

            } else {
                Log.e("로그인 넘버를 받아오는과정에서", "에러");
            }
        }
        else {
            logined++; //로그인 된 상태로 만들기
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 26) {
            Log.e("resultCode ==26", "resultCode == 26");
            //접속하자마자 교통사고 프래그먼트가 보여지도록 설정

            getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, new Fragment3()).commit();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * A placeholder fragment containing a simple view.
     */
 //   public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
    /*    private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
    /*    public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
*/
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return new Fragment1();
            else if(position==1)
                return new Fragment2();
            else
                return new Fragment3();
            //return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "날씨";
                case 1:
                    return "뉴스";
                case 2:
                    return "교통사고 지도";
            }
            return null;
        }
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
                int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                    /* ACCESS_FINE_LOCATION의 권한이 없을 때 */
                // 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
                if (permissionResult == PackageManager.PERMISSION_DENIED) {

                        /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                        * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                        */
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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
                                        Toast.makeText(MainActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
            }
            else {
                Toast.makeText(MainActivity.this, "앱 설정에서 권한을 허용해야 위치정보 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }

        }
    }




    @Override
    protected void onStop() {
        super.onStop();

        // Activity가 사라지면 로그인 변수 값과 프로필 정보를 저장한다(자동로그인을 위해)
        SharedPreferences sf = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        editor.putInt("logined", logined);  //로그인 됬는지 안됫는지
        editor.putInt("loginNum", loginNum);    //로그인 뭘로 했는지
        editor.putString("profileId", profileId);   //자체로그인 아디

        if(logined<2) { //처음로그인 상태라면 페북 프로필에서 받아온 비트맵을 스트링으로 변환 후 저장
            String image = BitMapToString(bm);  //페북 이미지를 스트링으로
            editor.putString("profileImage", image);    //페북이미지 저장
            editor.putString("profileName", profileName);   //페북로그인 이름
            editor.putString("profileEmail", profileEmail); //페북로그인 메일
        }
        else if(logined>=2) {  //자동로그인 상태라면 onCreate 쉐어드 불러온 값을 그대로 저장
            editor.putString("profileImage", pImage);    //페북이미지 저장
            editor.putString("profileName", pn);   //페북로그인 이름
            editor.putString("profileEmail", pe); //페북로그인 메일
        }
        editor.commit(); // 파일에 최종 반영함
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
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
