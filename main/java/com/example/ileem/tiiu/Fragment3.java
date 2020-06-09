
package com.example.ileem.tiiu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by ileem on 2016-10-27.
 */
public class Fragment3 extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    Marker selectedMarker;
    View marker_root_view;
    TextView tv_marker;
    private GoogleMap mMap;
    ScrollView scrollView;
    ArrayList<MarkerItem> sampleList;
    ClusterManager<MarkerItem> clusterManager;

    LocationManager lm;

    String dangerSpot;

    static View v;   // 프래그먼트의 뷰 인스턴스

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            v = inflater.inflate(R.layout.fragment3, container, false);
        }catch (InflateException e) {
        // 구글맵 View가 이미 inflate되어 있는 상태이므로, 에러를 무시합니다.
        }

        CheckTypesTask task = new CheckTypesTask();
        task.execute(); //프로그래스 다이얼로그 에이씽크태스크

        scrollView = (ScrollView)v.findViewById(R.id.scroll);

        scrollView.setVisibility(View.GONE);    //처음 액티비티 생성당시 스크롤뷰를 안보여줌

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment)getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkPermission();

        //mMap = mapFragment.getMap;
        //mMap.setOnMyLocationChangeListener(this);

        lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        return v;

    }

    private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {

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

                if(distance < 16000) {
                    dangerSpot = sampleList.get(i).getSpotname();
                    alertEx();
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        clusterManager = new ClusterManager<>(getActivity(), mMap);

        mMap.clear();
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.537523, 126.96558), 8));
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnMapClickListener(this);

        setCustomMarkerView();
        getSampleMarkerItems();

        mMap.setOnCameraChangeListener(clusterManager);

        clusterManager.addItem(new MarkerItem(0, new LatLng(37.5567290716902, 126.948737606357), "마포구 대흥동\n(21세기토탈웨딩 부근)", "서울 서울마포1", 8, 9, 1, 5, 3, 0));
        clusterManager.addItem(new MarkerItem(1,new LatLng(37.5443245536362, 126.949863977542), "마포구 공덕동\n(국민서관앞삼거리 부근)", "서울 서울마포2", 6, 6, 2, 2, 2, 0));
        clusterManager.addItem(new MarkerItem(2,new LatLng(37.516454716313, 126.907011927759), "영등포구 영등포동\n(영등포역 부근)", "서울 서울영등포1", 14, 14, 1, 12, 1, 0));
        clusterManager.addItem(new MarkerItem(3,new LatLng(37.4801001891733, 126.904641414671), "금천구 독산동\n(구로전화국_남부순환로_진출 부근)", "서울 서울금천1", 15, 18, 0, 15, 2, 1));
        clusterManager.addItem(new MarkerItem(4,new LatLng(37.4644617935778, 126.902888320341), "금천구 독산동\n(시흥4동사거리 부근)", "서울 서울금천2", 9, 12, 0, 6, 5, 1));
        clusterManager.addItem(new MarkerItem(5,new LatLng(37.4487336618299, 126.902487179541), "금천구 시흥동\n(박미삼거리 부근)", "서울 서울금천3", 6, 7, 0, 4, 3, 0));
        clusterManager.addItem(new MarkerItem(6,new LatLng(37.4588175899607, 126.898998326213), "금천구 독산동\n(금천구청입구 부근)", "서울 서울금천4", 5, 5, 1, 2, 2, 0));
        clusterManager.addItem(new MarkerItem(7,new LatLng(37.5036985354599, 126.937295475307), "동작구 상도동\n(교보증권 부근)", "서울 서울동작1", 9, 9, 0, 7, 1, 1));
        clusterManager.addItem(new MarkerItem(8,new LatLng(37.4997400336765, 126.928220805047), "동작구 대방동\n(신대방삼거리역 부근)", "서울 서울동작2", 7, 9, 0, 4, 5, 0));
        clusterManager.addItem(new MarkerItem(9,new LatLng(37.4807071686018, 126.952318397377), "관악구 봉천동\n(서울대입구역_관악로_진입_2 부근)", "서울 서울관악1", 14, 14, 1, 8, 4, 1));
        clusterManager.addItem(new MarkerItem(10,new LatLng(37.4776140547457, 126.9157308496), "관악구 신림동\n(난곡우체국앞 부근)", "서울 서울관악2", 6, 6, 0, 3, 3, 0));
        clusterManager.addItem(new MarkerItem(11,new LatLng(37.5615252119199, 126.809667924536), "강서구 공항동\n(신한은행앞 부근)", "서울 서울강서1", 6, 7, 2, 2, 3, 0));
        clusterManager.addItem(new MarkerItem(12,new LatLng(37.547160598084, 126.87299116651), "강서구 염창동\n(국일빌딩앞 부근)", "서울 서울강서2", 4, 4, 1, 1, 2, 0));
        clusterManager.addItem(new MarkerItem(13,new LatLng(37.6115701113724, 127.056202936917), "성북구 장위동\n(돌곶이역 부근)", "서울 서울종암1", 10, 10, 1, 6, 3, 0));
        clusterManager.addItem(new MarkerItem(14,new LatLng(37.6147482894029, 127.059084728978), "성북구 장위동\n(한전빌라 부근)", "서울 서울종암2", 5, 5, 1, 4, 0, 0));
        clusterManager.addItem(new MarkerItem(15,new LatLng(37.4806150807223, 126.89154676767), "구로구 가리봉동\n(만민중앙교회 부근)",  "서울 서울구로1", 22, 25, 1, 15, 6, 3));
        clusterManager.addItem(new MarkerItem(16,new LatLng(37.5034714368582, 126.888423832295), "구로구 구로동\n(미래초교_거리공원길_진입 부근)", "서울 서울구로2", 5, 5, 1, 2, 2, 0));
        clusterManager.addItem(new MarkerItem(17,new LatLng(37.484465283447, 127.032645467515), "서초구 서초동\n(양재역_남부순환로_진입_2 부근)", "서울 서울서초1", 7, 7, 1, 2, 2, 2));
        clusterManager.addItem(new MarkerItem(18,new LatLng(37.5360514913841, 126.82801488568), "양천구 신월동\n(신월사거리 부근)", "서울 서울양천1", 8, 9, 0, 5, 4, 0));
        clusterManager.addItem(new MarkerItem(19,new LatLng(37.5331847206992, 126.863738734456), "양천구 목동\n(목동사거리 부근)", "서울 서울양천2", 6, 6, 0, 3, 2, 1));
        clusterManager.addItem(new MarkerItem(20,new LatLng(37.5710861132345, 127.000661781317), "중구 방산동\n(종로5가 부근)", "서울 서울중부1", 15, 15, 0, 11, 2, 2));
        clusterManager.addItem(new MarkerItem(21,new LatLng(37.6401119643965, 127.06641515064), "노원구 하계동\n(중계근린공원 부근)", "서울 서울노원1", 10, 10, 0, 8, 1, 1));
        clusterManager.addItem(new MarkerItem(22,new LatLng(37.6455601257001, 127.070713238226), "노원구 하계동\n(중계1공단 부근)",  "서울 서울노원2", 5, 6, 0, 2, 3, 1));
        clusterManager.addItem(new MarkerItem(23,new LatLng(37.5653821253857, 127.017102273252), "중구 신당동\n(신당역 부근)",  "서울 서울중부2", 11, 12, 0, 8, 3, 1));
        clusterManager.addItem(new MarkerItem(24,new LatLng(37.5639175389642, 126.979604903844), "중구 소공동\n(소공로지하상가 부근)", "서울 서울남대문1", 8, 8, 2, 4, 2, 0));
        clusterManager.addItem(new MarkerItem(25,new LatLng(37.6522051443528, 127.036907937452), "도봉구 창동\n(서영실업 부근)", "서울 서울도봉1", 9, 10, 0, 9, 1, 0));
        clusterManager.addItem(new MarkerItem(26,new LatLng(37.6140334875563, 126.926927574549), "은평구 대조동\n(동명여고 부근)", "서울 서울은평1", 4, 4, 1, 3, 0, 0));
        clusterManager.addItem(new MarkerItem(27,new LatLng(37.5736580987432, 127.017047953301), "종로구 창신동\n(아람보습학원 부근)","서울 서울혜화1", 11, 11, 1, 6, 3, 1));
        clusterManager.addItem(new MarkerItem(28,new LatLng(37.5778711680209, 127.015325712013), "종로구 창신동\n(창신쌍용아파트 부근)", "서울 서울혜화2", 5, 6, 0, 2, 3, 1));
        clusterManager.addItem(new MarkerItem(29,new LatLng(37.4923031151975, 126.989035299141), "서초구 방배동\n(함지박사거리 부근)", "서울 서울방배1", 4, 4, 0, 2, 1, 1));
        clusterManager.addItem(new MarkerItem(30,new LatLng(37.4990143320467, 127.060901044748), "강남구 대치동\n(은마아파트입구 부근)", "서울 서울수서1", 6, 6, 1, 4, 1, 0));
        clusterManager.addItem(new MarkerItem(31,new LatLng(37.5663327402647, 126.965977940031), "서대문구 냉천동\n(서대문아트홀_의주로_진출_2 부근)", "서울 서울서대문1", 7, 7, 2, 1, 3, 1));
        clusterManager.addItem(new MarkerItem(32,new LatLng(37.5826068126552, 126.913137498898), "서대문구 북가좌동\n(북가좌사거리 부근)", "서울 서울서대문2", 4, 4, 0, 2, 2, 0));
        clusterManager.addItem(new MarkerItem(33,new LatLng(37.5883638551087, 126.912772717688), "은평구 신사동\n(증산손칼국수 부근)", "서울 서울서부1", 5, 5, 2, 2, 1, 0));
        clusterManager.addItem(new MarkerItem(34,new LatLng(37.6427130330809, 127.030773726527), "강북구 수유동\n(강북구-도봉구경계(4) 부근)","서울 서울강북1", 6, 6, 1, 5, 0, 0));
        clusterManager.addItem(new MarkerItem(35,new LatLng(37.5405178079513, 127.071002718302), "광진구 화양동\n(건대입구역 부근)","서울 서울광진1", 5, 6, 0, 3, 2, 1));
        clusterManager.addItem(new MarkerItem(36,new LatLng(37.556840342109, 127.079921097182), "광진구 능동\n(군자역 부근)","서울 서울광진2", 4, 5, 0, 5, 0, 0));

        clusterManager.setRenderer(new MyClusterRenderer(getActivity(), mMap, clusterManager));

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerItem>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerItem> cluster) {

                return false;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
            @Override
            public boolean onClusterItemClick(MarkerItem marker) {

                Log.e("some", "onClusterItemClick");

                CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
                mMap.animateCamera(center);

                int Id = marker.getId();   //선택된 마커의 아이디값 가져오기

                MarkerItem markerItem = sampleList.get(Id); //클릭한 마커의 ID와 매치되는 markerItem 가져오기

                TextView spotName = (TextView)getView().findViewById(R.id.spotname);
                TextView spot = (TextView)getView().findViewById(R.id.spot);
                TextView occrrnc_co = (TextView)getView().findViewById(R.id.occr);
                TextView dthinj_co = (TextView)getView().findViewById(R.id.dthi);
                TextView death_co = (TextView)getView().findViewById(R.id.death);
                TextView serinj_co = (TextView)getView().findViewById(R.id.seri);
                TextView ordnr_co = (TextView)getView().findViewById(R.id.ordn);
                TextView inj_co = (TextView)getView().findViewById(R.id.inj);

                String Occrrnc = String.valueOf(markerItem.getOccrrnc_co());
                String Dthinj = String.valueOf(markerItem.getDthinj_co());
                String Death = String.valueOf(markerItem.getDeath_co());
                String Serinj = String.valueOf(markerItem.getSerinj_co());
                String Ordnr = String.valueOf(markerItem.getOrdnr_co());
                String Inj = String.valueOf(markerItem.getInj_co());

                spotName.setText(markerItem.getSpotname());
                spot.setText(markerItem.getSpot());
                occrrnc_co.setText(Occrrnc);
                dthinj_co.setText(Dthinj);
                death_co.setText(Death);
                serinj_co.setText(Serinj);
                ordnr_co.setText(Ordnr);
                inj_co.setText(Inj);

                scrollView.setVisibility(View.VISIBLE); //마커가 클릭되면 스크롤뷰 보여주기

                return false;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                //위치정보 권한을 거부한뒤, 현재날씨 업뎃버튼을 눌렀을때
                //(GPS가 켜져잇더라도 날씨업뎃이 안되니까) 다시한번 권한 승인받기
                checkPermission();

                //추가한 라인
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                FirebaseInstanceId.getInstance().getToken();

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

                return false;
            }
        });

        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 500, null);
    }

    public class MyClusterRenderer extends DefaultClusterRenderer<MarkerItem> {

        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());

        public MyClusterRenderer(Context context, GoogleMap map, ClusterManager<MarkerItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MarkerItem markerItem, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(markerItem, markerOptions);

            LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());
            String spotname = markerItem.getSpotname();

            tv_marker.setText(spotname);
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone);
            tv_marker.setTextColor(Color.BLACK);

            markerOptions.title(spotname);
            markerOptions.position(position);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), marker_root_view)));

        }

        @Override
        protected void onClusterItemRendered(MarkerItem markerItem, Marker marker) {
            super.onClusterItemRendered(markerItem, marker);

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MarkerItem> cluster, MarkerOptions markerOptions){

            final Drawable clusterIcon = getResources().getDrawable(R.drawable.circle_icon);
            clusterIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);

            mClusterIconGenerator.setBackground(clusterIcon);

            //    modify padding for one or two digit numbers
            mClusterIconGenerator.setContentPadding(100, 70, 0, 0);

            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }
    }


    private void setCustomMarkerView() {

        marker_root_view = LayoutInflater.from(getActivity()).inflate(R.layout.marker_layout, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }


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


//        for (MarkerItem markerItem : sampleList) {
//            addMarker(markerItem, false);
//        }

    }


    private Marker addMarker(MarkerItem markerItem, boolean isSelectedMarker) {


        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());
        String spotname = markerItem.getSpotname();

        tv_marker.setText(spotname);

        if (isSelectedMarker) {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone_blue);
            tv_marker.setTextColor(Color.WHITE);
        } else {
            tv_marker.setBackgroundResource(R.drawable.ic_marker_phone);
            tv_marker.setTextColor(Color.BLACK);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(spotname);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), marker_root_view)));

        return mMap.addMarker(markerOptions);
    }



    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


//    private Marker addMarker(Marker marker, boolean isSelectedMarker) {
//        double lat = marker.getPosition().latitude;
//        double lon = marker.getPosition().longitude;
//        String spotname = marker.getTitle();
//        MarkerItem temp = new MarkerItem(lat, lon, spotname, "temp", 00, 00, 00, 00, 00, 00);
//        return addMarker(temp, isSelectedMarker);
//
//    }


    @Override
    public boolean onMarkerClick(Marker marker) {

//        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
//        mMap.animateCamera(center);
//
//        String selectedMarkerId = marker.getId();   //선택된 마커의 아이디값 가져오기
//
//        String replacedId = selectedMarkerId.replace("m", "");  //아이디값중 m만 지우기
//        Log.e("replacedId", " : "+ replacedId);
//        int Id = Integer.parseInt(replacedId);    //스트링인 아이디값을 int로 변환
//
//        MarkerItem markerItem = sampleList.get(Id); //클릭한 마커의 ID와 매치되는 markerItem 가져오기
//
//
//        TextView spotName = (TextView)findViewById(R.id.spotname);
//        TextView spot = (TextView)findViewById(R.id.spot);
//        TextView occrrnc_co = (TextView)findViewById(R.id.occr);
//        TextView dthinj_co = (TextView)findViewById(R.id.dthi);
//        TextView death_co = (TextView)findViewById(R.id.death);
//        TextView serinj_co = (TextView)findViewById(R.id.seri);
//        TextView ordnr_co = (TextView)findViewById(R.id.ordn);
//        TextView inj_co = (TextView)findViewById(R.id.inj);
//
//        String Occrrnc = String.valueOf(markerItem.getOccrrnc_co());
//        String Dthinj = String.valueOf(markerItem.getDthinj_co());
//        String Death = String.valueOf(markerItem.getDeath_co());
//        String Serinj = String.valueOf(markerItem.getSerinj_co());
//        String Ordnr = String.valueOf(markerItem.getOrdnr_co());
//        String Inj = String.valueOf(markerItem.getInj_co());
//
//        spotName.setText(markerItem.getSpotname());
//        spot.setText(markerItem.getSpot());
//        occrrnc_co.setText(Occrrnc);
//        dthinj_co.setText(Dthinj);
//        death_co.setText(Death);
//        serinj_co.setText(Serinj);
//        ordnr_co.setText(Ordnr);
//        inj_co.setText(Inj);
//
//        scrollView.setVisibility(View.VISIBLE); //마커가 클릭되면 스크롤뷰 보여주기

//        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.afdadfs);
//        Bitmap bitmap = drawable.getBitmap();
//
//        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));

        //changeSelectedMarker(marker);

        return true;
    }



    private void changeSelectedMarker(Marker marker) {
        // 선택했던 마커 되돌리기
        if (selectedMarker != null) {
            //    addMarker(selectedMarker, false);
            selectedMarker.remove();
        }

        // 선택한 마커 표시
        if (marker != null) {
            //    selectedMarker = addMarker(marker, true);
            marker.remove();
        }


    }

    @Override
    public void onMapClick(LatLng latLng) {
        changeSelectedMarker(null);

        scrollView.setVisibility(View.GONE);    //맵화면 클릭시 스크롤뷰를 안보여줌
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
            int permissionResult = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                    /* ACCESS_FINE_LOCATION의 권한이 없을 때 */
            // 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
            if (permissionResult == PackageManager.PERMISSION_DENIED) {

                        /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                        * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                        */
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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
                                    Toast.makeText(getActivity(), "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
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
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
            }
            else {
                Toast.makeText(getActivity(), "앱 설정에서 권한을 허용해야 위치정보 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void alertCheckGPS() {   //설정에서 위치사용을 On하시겠습니까?
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void alertEx() {   //설정에서 위치사용을 On하시겠습니까?
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("1800m내에 보행자 무단횡단사고 다발지역이 있습니다.\n" + dangerSpot + "\n안전운행 바랍니다.")
                .setCancelable(false)
                .setNegativeButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(v!=null){
            ViewGroup parent = (ViewGroup)v.getParent();
            if(parent!=null){
                parent.removeView(v);
            }
        }
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(getContext());

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("Loading...");
            asyncDialog.setCancelable(false);
            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                    Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }

}