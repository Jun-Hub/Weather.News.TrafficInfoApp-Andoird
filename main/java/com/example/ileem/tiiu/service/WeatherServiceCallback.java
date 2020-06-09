
package com.example.ileem.tiiu.service;

import com.example.ileem.tiiu.data.Channel;

/**
 * Created by ileem on 2016-11-02.
 */
public interface WeatherServiceCallback {
    //성공시
    void serviceSuccess(Channel channel);
    //실패시
    void serviceFailure(Exception exception);
}
