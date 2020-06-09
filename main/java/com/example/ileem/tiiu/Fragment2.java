
package com.example.ileem.tiiu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Created by ileem on 2016-10-27.
 */
public class Fragment2 extends Fragment {

    //뉴스의 title부분을 저장하기 위한 객체 선언
    Vector<String> titlevec = new Vector<String>();
    //뉴스의 descirtion을 저장하기 위한 객체 선언
    Vector<String> pubDatevec = new Vector<String>();
    //뉴스의 originallink을 저장하기 위한 객체 선언
    Vector<String> originLinkvec = new Vector<String>();
    //HTML의 img src 태크를 담는 벡터 선언
    Vector<String> imgSrcVec = new Vector<String>();
    //imgSrcVec중에서 http값만 추출하여 담는 벡터 선언
    Vector<String> imgUrlVec = new Vector<String>();


    EditText searchTxt; //검색어 입력란
    String search; //searchTxt안의 검색어
    String text; //search를 UTF-8형식으로 인코딩

    ListView listview ;
    ListViewAdapter adapter;

    String clientId = "BD24IzYJmjqUJ4fFq3VT";//애플리케이션 클라이언트 아이디값";
    String clientSecret = "O3GJxmPeHH";//애플리케이션 클라이언트 시크릿값";
    StringBuffer response;

    //xml에서 읽어드려서 저장할 변수
    String tagname="",title="",pubDate="", originLink="";
    //제대로 데이터가 읽어졌는지를 판단해주는 변수
    boolean flag=false;

    private MyAsyncTask myAsyncTask;
    InputMethodManager imm;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment2, container, false);

        final Button searchBtn = (Button)v.findViewById(R.id.btnSearch);
        searchTxt = (EditText)v.findViewById(R.id.txtSearch);

        // Adapter 생성
        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) v.findViewById(R.id.listview);
        listview.setAdapter(adapter);

        //GSON 객체생성
        Gson gson = new Gson();

        // 지난번 저장해놨던 사용자 입력값을 꺼내서 보여주기
        SharedPreferences sf = getActivity().getSharedPreferences("listViewInfo", Context.MODE_PRIVATE);
        String info = sf.getString("info", "nullPoint"); // 키값으로 꺼냄, 뒤에것은 디폴트값

        if(!info.equals("nullPoint")) { //info가 빈값이 아니라면

            ArrayListSave arrayListSave = gson.fromJson(info, ArrayListSave.class);  //파싱해놨던 값인 info를 다시 클래스로 변환

            for (ListViewSave listViewSave : arrayListSave.getListViewSaves())     //반복문 이용하여 저장해놨던 info(스트링으로 변환한 클래스)값들을
            {                                                     //그대로 리스트뷰에 추가하며 복귀
                adapter.addItem(listViewSave.getImage(), listViewSave.getTitle(), listViewSave.getDate(), listViewSave.getLink());
            }
        }
        adapter.notifyDataSetChanged();

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override       //검색 버튼 클릭시
            public void onClick(View view) {
                search = searchTxt.getText().toString();

                //뉴스의 데이터들을 뽑아 오는 클래스 선언(생성자로 생성 검색어대입)

                //doInBackground 메소드를 호출해줌.

                imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0); //키보드 감추기

                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute(null, null, null);
                // AsyncTask를 실행시킨다. execute()메서드에 의해 가장 먼저 호출되는 메서드가 onPreExecute()이고
                // 다음으로 자동으로 호출되는 메서드가 doInBackground() 이다.
            }
        });

        LinearLayout mainLayout = (LinearLayout) v.findViewById(R.id.mainLayout);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override    //전체 레이아웃 클릭시 키보드 내려가게하기
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
            }
        });


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override       //리스트뷰 아이템 클릭 시, 해당 오리지날링크로 이동
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String oLink = adapter.getOriginLink(i);
                // 버튼 이벤트 내 Intent를 생성하여 Intent.ACTION_VIEW 액션을 취해준 뒤, url을 넣어줌
                Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse(oLink));
                startActivity(intent);
            }
        });

        searchTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //EditText 엔터키 액션
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search = searchTxt.getText().toString();

                    //뉴스의 데이터들을 뽑아 오는 클래스 선언(생성자로 생성 검색어대입)

                    //doInBackground 메소드를 호출해줌.

                    imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0); //키보드 감추기

                    myAsyncTask = new MyAsyncTask();
                    myAsyncTask.execute(null, null, null);
                    // AsyncTask를 실행시킨다. execute()메서드에 의해 가장 먼저 호출되는 메서드가 onPreExecute()이고
                    // 다음으로 자동으로 호출되는 메서드가 doInBackground() 이다.
                    return true;
                }
                return false;
            }
        });



        return v;
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        ProgressDialog pdLoading = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                text = URLEncoder.encode(search, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/search/news.xml?query=" + text;
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                Log.e("sdf", " " + response.toString());    //파싱해야 할 XML 파일 원본

                //안드로이드에서 xml문서를 읽고 파싱하는 객체를 선언
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                //네임스페이스 사용여부
                factory.setNamespaceAware(true);
                //실제 sax형태로 데이터를 파싱하는 객체 선언
                XmlPullParser xpp = factory.newPullParser();
                //xml문서를 읽고 파싱하는 객체에 넘겨줌
                xpp.setInput(new StringReader(response.toString())); //xml문서의 인코딩 정확히 지정

                //item 태그를 안이라면
                boolean isInItemTag = false;
                //이벤트 타입을 얻어옴
                int eventType = xpp.getEventType();
                //문서의 끝까지 읽어 드리면서 title과 descripton을 추출해냄
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {
                        //태그명을 읽어드림
                        tagname = xpp.getName();

                        if (tagname.equals("item")) {
                            isInItemTag = true;
                        }

                    } else if (eventType == XmlPullParser.TEXT) {
                        //태그명이 title이거나 또는 description일때 읽어옴

                        if (tagname.equals("title") && isInItemTag) {
                            title += xpp.getText(); //text에 해당하는 모든 텍스트를 읽어드림 ( += )
                            title = title.replaceAll("<b>", "");
                            title = title.replaceAll("</b>", "");
                            title = title.replaceAll("&quot;", "\"");
                            title = title.replaceAll("&amp;", "&");
                            title = title.replaceAll("&lt;", "<");
                            title = title.replaceAll("&gt;", ">");

                        } else if (tagname.equals("pubDate") && isInItemTag) {
                            pubDate += xpp.getText();
                            for (int i = 1; i < 32; i++) {
                                for (int j = 2016; j < 2019; j++) {
                                    pubDate = pubDate.replaceAll(" " + i + " Jan " + j, j + "-1-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Feb " + j, j + "-2-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Mar " + j, j + "-3-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Apr " + j, j + "-4-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " May " + j, j + "-5-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Jun " + j, j + "-6-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Jul " + j, j + "-7-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Aug " + j, j + "-8-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Sep " + j, j + "-9-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Oct " + j, j + "-10-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Nov " + j, j + "-11-" + i);
                                    pubDate = pubDate.replaceAll(" " + i + " Dec " + j, j + "-12-" + i);
                                }
                            }
                            pubDate = pubDate.replaceAll(":00 ", "");
                            pubDate = pubDate.replaceAll("0900", "");
                            pubDate = pubDate.replaceAll("\\+", "");
                            pubDate = pubDate.replaceAll("Mon,", "");
                            pubDate = pubDate.replaceAll("Tue,", "");
                            pubDate = pubDate.replaceAll("Wed,", "");      //엿같은 Tue, 22 Nov 2016 10:52:00 +0900을 보기좋게
                            pubDate = pubDate.replaceAll("Thu,", "");      //변환시키는 과정
                            pubDate = pubDate.replaceAll("Fri,", "");
                            pubDate = pubDate.replaceAll("Sat,", "");
                            pubDate = pubDate.replaceAll("Sun,", "");
                        }
                        else if (tagname.equals("originallink") && isInItemTag) {
                            originLink += xpp.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        //태그명을 읽어드림
                        tagname = xpp.getName();

                        //endtag일경우에만 벡터에 저장
                        if (tagname.equals("item")) {
                            //벡터에 저장
                            titlevec.add(title);
                            pubDatevec.add(pubDate);
                            originLinkvec.add(originLink);

                            //변수 초기화
                            title = "";
                            pubDate = "";
                            originLink = "";

                            isInItemTag = false;

                        }//if-------

                    }//if----------
                    //다음 이벤트 다입을 저장
                    eventType = xpp.next();
                }//while---------

                //모든 데이터가 저장되었다면.
                flag = true; //true : 지정된 xml파일을 읽고 필요한 데이터를 추출해서 저장 완료된 상태

            } catch (Exception e) { //인터넷 연결이 원활하지않거나 끊긴 경우

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {   //쓰레드안에서 쓰레드를 생성할경우
                    @Override                           //핸들러를 사용해야함
                    public void run() {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("네트워크 접속 오류")
                                .setMessage("네트워크 접속이 원활하지 않습니다. 다시 시도해주세요")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .create()
                                .show();

                    }
                }, 0);
            }


                String newsLink = "https://m.search.naver.com/search.naver?where=m_news&sm=mtb_jum&query=" + text;

                Connection con = Jsoup.connect(newsLink);

                // 최대 연결 지연시간은 15초로 지정한다. (msec)
                con.timeout(1000 * 15);
                try {
                    // Jsoup의 'get' 메소드를 이용하여 실제 커넥션을 이루고 Document를 취한다.
                    // Document 에는 HTML 코드들이 모두 포함된다.
                    Document document = con.get();

                    // Document에서 Elements 객체를 취한다.
                    // elements는 Jsoup에서 구현해둔것을 이용해야 한다.
                    // 아래 코드에서는 "img" 태그에 해당하는
                    // 모든 Element들을 Elements로 할당하여 반환하게 된다.
                    Elements elements = document.getElementsByTag("img");

                    // Elements에서 Element를 반복하며 순서대로 참조한다.
                    for (Element element : elements) {
                        // Element는 위에서 취한 "img" 태그일 것이므로
                        // 내부 속성중 "src"를 얻어 콘솔로 출력하였다.
                        // 원하는 url에 존재하는 모든 이미지의 실제 url을 볼 수 있다.
                        imgSrcVec.add(element.attr("src").toString());  //벡터에 태그에 해당하는 문자열 담기
                        Log.e("imgSrcVec content", " : " +element.attr("src").toString());
                    }

                    Log.e("imgSrcVec size", " : " +imgSrcVec.size());

                    for (int j = 0; j < imgSrcVec.size(); j++) {    //벡터에 http가 담아져 있다면 새로운 벡터에 담기
                        String str = (String) imgSrcVec.get(j);

                        if(str.contains(".jpg")) {
                            Log.e(".jpg", " : " + str);
                            imgUrlVec.add(str);
                        }
                    }

                    Log.e("size", " : " +imgUrlVec.size());

                    imgSrcVec .clear(); //벡터 초기화

                    if(flag){
                        adapter.removeAll(); //재검색하고나면 리스트를 전부 지워준 후

                        if(titlevec.size() < 1) {   //파싱한 데이터가 없다면 == 검색결과가 없다면
                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.postDelayed(new Runnable() {   //쓰레드안에서 쓰레드를 생성할경우
                                @Override                           //핸들러를 사용해야함
                                public void run() {
                                    Toast.makeText(getContext(), "검색어와 관련된 뉴스가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                                }
                            }, 0);
                        }

                        for (int i = 0; i < titlevec.size(); i++) {    //리스트뷰에 검색결과 대입
                            String str = (String) titlevec.get(i);
                            String date = (String) pubDatevec.get(i);
                            String link = (String) originLinkvec.get(i);
                            String imgUrl = (String)imgUrlVec.get(i);

                            Bitmap urlBitmap = getBitmapFromURL(imgUrl);

                            adapter.addItem(urlBitmap, str, date, link);
                        }
                    }

                } catch (IOException e) { //인터넷 연결이 원활하지않거나 끊긴 경우

                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {   //쓰레드안에서 쓰레드를 생성할경우
                        @Override                           //핸들러를 사용해야함
                        public void run() {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                            dialog.setTitle("네트워크 접속 오류")
                                    .setMessage("네트워크 접속이 원활하지 않습니다. 다시 시도해주세요")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .create()
                                    .show();

                        }
                    }, 0);
                }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            pdLoading.dismiss();
            //this method will be running on UI thread

            adapter.notifyDataSetChanged();  //리스트뷰 갱신하기  필수!!
            listview.smoothScrollToPosition(0); //리스트뷰 스크롤 맨위로 올리기

            //다음 검색시 검색결과가 쌓이지 않게 벡터 초기화
            titlevec.clear();
            pubDatevec.clear();
            originLinkvec.clear();
            imgUrlVec.clear();
        }
    }


    public Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally{
            if(connection!=null)connection.disconnect();
        }
    }

    @Override

    public void onStop() {

        super.onStop();

        Gson gson = new Gson();  //먼저 Gson객체를 생성한다. ​

        List<ListViewSave> listViewSaves = new ArrayList<ListViewSave>();  //리스트뷰 아이템 클래스를 어레이리스트 객체로 형성

        for(int i=0; i<adapter.getCount(); i++) {  //반복문 이용하여 리스트뷰 아이템들을 고스란히 ArrayList객체인 listViewSaves에 저장
            listViewSaves.add(new ListViewSave(adapter.getBitmapImg(i), adapter.getItemTitle(i), adapter.getItemDate(i), adapter.getItemLink(i)));
        }

        String info = gson.toJson(new ArrayListSave(listViewSaves));   //listViewSaves객체를 GSON을 이용하여 String값으로 파싱

        // SharedPreferences 에 (SharedPreferences 이름, 모드)를 저장하기
        SharedPreferences sp = getActivity().getSharedPreferences("listViewInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();//저장하려면 editor가 필요하므로 editor 생성

        editor.putString("info", info);  //info이란 key값으로, 파싱한 listViewSaves객체를 value값으로 넣어주기
        editor.commit(); // 파일에 최종 반영함
    }

}//end Class : MainActivity();
