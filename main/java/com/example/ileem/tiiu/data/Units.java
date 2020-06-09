
package com.example.ileem.tiiu.data;

import org.json.JSONObject;

/**
 * Created by ileem on 2016-11-02.
 */
public class Units implements JSONPopulator {
    private String temperature;

    public String getTemperature() {
        return temperature;
    }

    @Override
    public void poupulate(JSONObject data) {
        temperature = data.optString("temperature");
    }
}
