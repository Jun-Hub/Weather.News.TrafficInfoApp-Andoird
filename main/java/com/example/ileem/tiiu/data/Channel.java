
package com.example.ileem.tiiu.data;

import org.json.JSONObject;

/**
 * Created by ileem on 2016-11-02.
 */
public class Channel implements JSONPopulator {
    private Units units;
    private Item item;
    private Location location;

    public Units getUnits() {
        return units;
    }

    public Item getItem() {
        return item;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void poupulate(JSONObject data) {

        units = new Units();
        units.poupulate(data.optJSONObject("units"));

        item = new Item();
        item.poupulate(data.optJSONObject("item"));

        location = new Location();
        location.poupulate(data.optJSONObject("location"));
    }
}
