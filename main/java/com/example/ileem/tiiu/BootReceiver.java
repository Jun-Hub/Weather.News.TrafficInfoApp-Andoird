package com.example.ileem.tiiu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ileem on 2016-11-17.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && SetActivity.onOff == 1) {
            Intent i = new Intent(context, ScreenService.class);
            context.startService(i);
        }
    }
}