
package com.example.ileem.tiiu;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ileem on 2016-12-07.
 */
 public class MarkerItem implements ClusterItem {

    int id;
    double lat;
    double lon;
    LatLng location;
    String spotname;
    String spot;
    int occrrnc_co;
    int dthinj_co;
    int death_co;
    int serinj_co;
    int ordnr_co;
    int inj_co;

    public MarkerItem(int id, LatLng location, String spotname, String spot,
                      int occrrnc_co, int dthinj_co, int death_co, int serinj_co, int ordnr_co, int inj_co) {
        this.id = id;
        this.location = location;
        this.spotname = spotname;
        this.spot = spot;
        this.occrrnc_co = occrrnc_co;
        this.dthinj_co = dthinj_co;
        this.death_co = death_co;
        this.serinj_co = serinj_co;
        this.ordnr_co = ordnr_co;
        this.inj_co = inj_co;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getSpotname() {
        return spotname;
    }

    public void setSpotname(String spotname) {
        this.spotname = spotname;
    }

    public int getId() {
        return id;
    }
    public String getSpot() {
        return spot;
    }
    public int getOccrrnc_co() {
        return occrrnc_co;
    }
    public int getDthinj_co() {
        return dthinj_co;
    }
    public int getDeath_co() {
        return death_co;
    }
    public int getSerinj_co() {
        return serinj_co;
    }
    public int getOrdnr_co() {
        return ordnr_co;
    }
    public int getInj_co() {
        return inj_co;
    }

    @Override
    public LatLng getPosition() {
        return location;
    }
}