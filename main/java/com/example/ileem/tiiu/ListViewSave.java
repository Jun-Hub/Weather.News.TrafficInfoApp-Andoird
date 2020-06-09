
package com.example.ileem.tiiu;

import android.graphics.Bitmap;

/**
 * Created by ileem on 2016-11-24.
 */
public class ListViewSave {

    Bitmap image;
    String title;
    String date;
    String link;

    public ListViewSave(Bitmap image, String title, String date, String link) {
        this.image = image;
        this.title = title;
        this.date = date;
        this.link = link;
    }

    public Bitmap getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }
    public String getDate() {
        return date;
    }
    public String getLink() {
        return link;
    }
}
