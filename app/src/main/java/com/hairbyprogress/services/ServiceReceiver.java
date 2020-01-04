package com.hairbyprogress.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by John Ebere on 11/19/2016.
 * Copyright of Maugost Incorporated
 */
public class ServiceReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,AppSettingsService.class));
    }


}
