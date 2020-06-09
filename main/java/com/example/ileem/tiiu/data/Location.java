
package com.example.ileem.tiiu.data;

import org.json.JSONObject;

/**
 * Created by ileem on 2016-11-04.
 */
public class Location implements JSONPopulator {

    private String city;
    private String region;

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public void poupulate(JSONObject data) {
        city = data.optString("city");
        region = data.optString("region");
    }
}