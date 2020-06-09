
package com.example.ileem.tiiu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ileem on 2016-10-27.
 */
public class Fragment1 extends Fragment{

    private TabLayout tabLayout;
    int color1 = Color.rgb(255,100,135); //핫핑크 RGB를 Int값으로 변환
    int normalColor = Color.rgb(255,140,140);
    int selectedColor = Color.rgb(190,0,0);


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment1, container, false);
        
        tabLayout = (TabLayout) v.findViewById(R.id.tabs); //TabLayout 객체 생성

        tabLayout.addTab(tabLayout.newTab().setText("현재날씨"), true); //TabLayout의 탭들 추가
        tabLayout.addTab(tabLayout.newTab().setText("주간날씨"));

        tabLayout.setSelectedTabIndicatorColor(color1); //네비게이션 selectedTabBar의 색깔 지정
        tabLayout.setSelectedTabIndicatorHeight(15);    //네비게이션 selectedTabBar의 높이 지정
        tabLayout.setTabTextColors(normalColor, selectedColor); //선택된 탭의 글씨 색깔 지정

        //접속하자마자 현재날씨 프래그먼트가 보여지도록 설정
        getChildFragmentManager().beginTransaction().replace(R.id.fragContainer, new Fragment1_1()).commit();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override  //TabLayout의 각 탭 클릭 리스너
            public void onTabSelected(TabLayout.Tab tab) {
                //        pager.setCurrentItem(tab.getPosition());

                if(tab.getPosition() == 0) {
                    getChildFragmentManager().beginTransaction().replace(R.id.fragContainer, new Fragment1_1()).commit();
                }
                else if(tab.getPosition() == 1){
                    getChildFragmentManager().beginTransaction().replace(R.id.fragContainer, new Fragment1_2()).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                if(tab.getPosition() == 0 && tab.getPosition() == 1){
                    getChildFragmentManager().beginTransaction().replace(R.id.fragContainer, new Fragment1_1()).commit();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return v;

    }
}