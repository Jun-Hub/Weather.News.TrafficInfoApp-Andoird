
package com.example.ileem.tiiu;

import android.graphics.Bitmap;

/**
 * Created by ileem on 2016-11-24.
 */
public class ListViewItem {

    private Bitmap bitmapImg;
    private String titleStr ;
    private String timeStr ;
    private String linkStr;

    public void setBitmapImg(Bitmap bitmap) {
        bitmapImg = bitmap;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setTime(String time) {
        timeStr = time ;
    }
    public void setLink(String link) {
        linkStr = link ;
    }

    public Bitmap getBitmapImg() {
        return  this.bitmapImg;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getTime() {
        return this.timeStr ;
    }
    public String getLink() {
        return this.linkStr ;
    }
}