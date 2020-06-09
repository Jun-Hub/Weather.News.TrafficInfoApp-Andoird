
package com.example.ileem.tiiu.data;

import org.json.JSONObject;

/**
 * Created by ileem on 2016-11-02.
 */
public class Item implements JSONPopulator {

    private Condition condition;
    private Forecast forecast;

    public Condition getCondition() {
        return condition;
    }

    public Forecast getForecast() {
        return forecast;
    }

    @Override
    public void poupulate(JSONObject data) {
        condition = new Condition();
        condition.poupulate(data.optJSONObject("condition"));

        forecast = new Forecast(toString(), toString(), toString(), toString(), toString(), toString(), toString());
        forecast.poupulate(data.optJSONArray("forecast"));
    }
}
