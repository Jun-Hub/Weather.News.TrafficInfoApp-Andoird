
package com.example.ileem.tiiu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;


public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    //ImageView myImage;
    String userId;
    Bitmap bm;

    //자체 로그인 변수들
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText etId;
    private EditText etPassword;
    private Button assignBtn;

    //자체 로그인인지, 페이스북 로그인인지 구분하는 변수값
    int loginNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.login_activity);

        // 자체로그인 뷰들
        etId = (EditText) findViewById(R.id.id);
        etPassword = (EditText) findViewById(R.id.password);
        assignBtn = (Button)findViewById(R.id.assignBtn);


        loginNum = 0;  //로그아웃을 한다면 로그인 액티비티로 오니까 로그인변수 초기화!

        // 로그인을 했었더라면 바로 다음화면으로 넘어가기위해 로그인했던 방법을 기억하는 SharedPreferences
        SharedPreferences sf = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        int loginNumber = sf.getInt("loginNum", 0);

        if(loginNumber == 1){ //자체로그인으로 로그인했엇더라면
            Intent intent2 = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent2);
            LoginActivity.this.finish();
        }
        else if(loginNumber == 2){ //페이스북 로그인으로 로그인 했었더라면
            Intent intent2 = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent2);
            LoginActivity.this.finish();
        }

        assignBtn.setOnClickListener(new View.OnClickListener() {
            @Override     //회원가입 버튼 클릭시 구현
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, AssignActivity.class);
                startActivity(intent);
            }
        });


        callbackManager = CallbackManager.Factory.create();

        //페이스북 로그인 버튼
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {  //로그인이 성공한다면

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("result",object.toString());

                        try {
                            userId = object.getString("id");
                            final String email = object.getString("email");       // 이메일
                            final String name = object.getString("name");         // 이름
                            String gender = object.getString("gender");     // 성별

                            new Thread () {
                                public void run() {
                                    // 에러낸 코드를 여기로 옮겨 줘서 실행시켜준다.
                                    try{
//                                        URL url = new URL("https://graph.facebook.com/"+userId+"/picture");
//                                        URLConnection conn = url.openConnection();
//                                        conn.connect();
//                                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
//                                        Bitmap bm = BitmapFactory.decodeStream(bis);
//                                        bis.close();

                                        //페이스북으로 로그인했을시 로그인 변수
                                        loginNum = 2;

                                        bm = getBitmap();

                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("profileImage", bm); //번들에 프로필 이미지 담기
                                        Message msg = handler.obtainMessage();
                                        msg.setData(bundle);
                                        handler.sendMessage(msg);

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("loginNum", 2);
                                        intent.putExtras(bundle);
                                        intent.putExtra("email", email);   //메인 화면으로 번들(프로필이미지)
                                        intent.putExtra("name", name);     //이메일주소, 이름 보내기
                                        startActivity(intent);
                                        LoginActivity.this.finish();


                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

//                            Bitmap bm = loadBitmap("https://graph.facebook.com/"+userId+"/picture");
//                            myImage.setImageBitmap(bm);

                            Log.d("TAG","페이스북 이메일 -> " + email);
                            Log.d("TAG","페이스북 이름 -> " + name);
                            Log.d("TAG","페이스북 성별 -> " + gender);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginErr",error.toString());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap getBitmap()
    {
        Bitmap bm = null;

        try{    //메인 스레드가 아닌 다른 스레드에서 네트워크 접속을 해야함
            URL url = new URL("https://graph.facebook.com/"+userId+"/picture");
            URLConnection conn = url.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return bm;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            Bitmap bm = bundle.getParcelable("profileImage");
            //myImage.setImageBitmap(bm);
        }
    };


    public void checkLogin(View arg0) { //자체로그인 접속 버튼 클릭시

        // Get text from email and passord field
        final String email = etId.getText().toString();
        final String password = etPassword.getText().toString();

        // Initialize  AsyncLogin() class with email and password
        new AsyncLogin().execute(email,password);
    }

    //에이씽크 태스크를 이용하여 로그인 접속 버튼 클릭시 서버와 접속 구현
    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://jun3028.cafe24.com/user_signup/signIn.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("fuck Error", "Fucking Error11");
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user_id", params[0])
                        .appendQueryParameter("user_password", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                Log.e("fuck Error", "Fucking Error22");
                return "exception";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }
                else{
                    Log.e("fuck Error", "Fucking Error33");
                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("fuck Error", "Fucking Error44");
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */

                loginNum = 1; //자체로그인 성공시 로그인 변수
                Intent intent2 = new Intent(LoginActivity.this,MainActivity.class);
                intent2.putExtra("loginNum", 1);
                intent2.putExtra("ID", etId.getText().toString());
                startActivity(intent2);
                LoginActivity.this.finish();

            }else if (result.equalsIgnoreCase("false")){

                // If username and password does not match display a error message
                Toast.makeText(LoginActivity.this, "아이디나 비밀번호를 확인하세요", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(LoginActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Activity가 사라지면 로그인 변수 값을 저장한다(재접시 자동로그인을 위해)
        SharedPreferences sf = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요

        editor.putInt("loginNum", loginNum);
        editor.commit(); // 파일에 최종 반영함
    }
}