
package com.example.ileem.tiiu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Created by ileem on 2016-11-12.
 */
public class AssignActivity extends AppCompatActivity {
    private EditText user_id, user_name, user_passWord, user_passWordAgain,user_Phone, user_email;
    private Button btn_agreeJoin, repetitionBtn;
    private boolean confirm = false; //아이디 중복확인했는지 체크하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assign_activity);
        NetworkUtil.setNetworkPolicy();
        user_id = (EditText)findViewById(R.id.user_id);
        user_name = (EditText)findViewById(R.id.user_name);
        user_passWord = (EditText)findViewById(R.id.user_passWord);
        user_passWordAgain = (EditText)findViewById(R.id.user_passWordAgain);
        user_Phone = (EditText)findViewById(R.id.user_Phone);

        repetitionBtn = (Button)findViewById(R.id.button);

        repetitionBtn.setOnClickListener(new View.OnClickListener() {
            @Override      //아이디 중복확인 검사
            public void onClick(View view) {
                // Get text from id and passord field
                final String userId = user_id.getText().toString();

                // Initialize  AsyncLogin() class with email and password
                new AsyncConfirm().execute(userId);
            }
        });

        btn_agreeJoin = (Button)findViewById(R.id.btn_agreeJoin);
        btn_agreeJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user_id.length() < 1) {
                    Toast.makeText(AssignActivity.this, "아이디가 공백입니다!", Toast.LENGTH_SHORT).show();
                }
                else if (user_id.length() < 4){
                    Toast.makeText(AssignActivity.this, "아이디를 4자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (user_name.length() < 1) {
                    Toast.makeText(AssignActivity.this, "이름이 공백입니다!", Toast.LENGTH_SHORT).show();
                }
                else if (user_name.length() < 2){
                    Toast.makeText(AssignActivity.this, " 이름을 2자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (user_Phone.length() < 1) {
                    Toast.makeText(AssignActivity.this, "폰번호가 공백입니다!", Toast.LENGTH_SHORT).show();
                }
                else if (user_Phone.length() < 10){
                    Toast.makeText(AssignActivity.this, "폰번호를 10자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(user_passWord.length()<1) {
                    Toast.makeText(AssignActivity.this, "비밀번호가 공백입니다!", Toast.LENGTH_SHORT).show();
                }
                else if (user_passWord.length() < 6){
                    Toast.makeText(AssignActivity.this, "비밀번호를 6자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(!user_passWord.getText().toString().equals(user_passWordAgain.getText().toString())) {
                    Toast.makeText(AssignActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }
                else if(!confirm){
                    Toast.makeText(AssignActivity.this, "아이디 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        PHPRequest request = new PHPRequest("http://jun3028.cafe24.com/user_signup/signup_user_information.php");
                        String result = request.PhPtest(String.valueOf(user_id.getText()), String.valueOf(user_name.getText()),
                                String.valueOf(user_passWord.getText()), String.valueOf(user_Phone.getText()));
                        if (result.equals("1 record added")) {
                            Toast.makeText(getApplication(), "회원가입을 축하합니다!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AssignActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplication(), "오류", Toast.LENGTH_SHORT).show();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class AsyncConfirm extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(AssignActivity.this);
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
                url = new URL("http://jun3028.cafe24.com/user_signup/confirmRepetition.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("fuck Error", "Fucking Error11");
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("user_id", params[0]);
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

            if(result.equalsIgnoreCase("false"))
            {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */

                confirm = true;
                Toast.makeText(AssignActivity.this, "사용가능한 아이디 입니다", Toast.LENGTH_LONG).show();

            }else if (result.equalsIgnoreCase("true")){

                confirm = false;
                // If username and password does not match display a error message
                Toast.makeText(AssignActivity.this, "이미 존재하는 아이디입니다.", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(AssignActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }
        }

    }
}
