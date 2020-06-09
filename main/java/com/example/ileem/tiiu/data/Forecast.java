
package com.example.ileem.tiiu.data;

import com.google.gson.JsonIOException;

import org.json.JSONArray;

/**
 * Created by ileem on 2016-11-05.
 */
public class Forecast implements JSONArrayPopulator {

    private String day1;
    private String day2;
    private String day3;
    private String day4;
    private String day5;
    private String day6;
    private String day7;

    public Forecast(String day1, String day2, String day3, String day4, String day5, String day6, String day7) {
        super();

        this.day1 = day1;
        this.day2 = day2;
        this.day3 = day3;
        this.day4 = day4;
        this.day5 = day5;
        this.day6 = day6;
        this.day7 = day7;
    }


    public String getDay1() {
        return day1;
    }

    public String getDay2() {
        return day2;

    }

    public String getDay3() {
        return day3;

    }

    public String getDay4() {
        return day4;

    }

    public String getDay5() {
        return day5;

    }

    public String getDay6() {
        return day6;

    }
    public String getDay7() {
        return day7;

    }

    @Override
    public void poupulate(JSONArray data) {

        day1 = data.optString(0);
        day2 = data.optString(1);
        day3 = data.optString(2);
        day4 = data.optString(3);
        day5 = data.optString(4);
        day6 = data.optString(5);
        day7 = data.optString(6);
    }
}
