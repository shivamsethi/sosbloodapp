package com.shivam.sosblood.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.shivam.sosblood.R;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;

public class MyAutoPilot extends Autopilot {

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(true);
    }

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        AirshipConfigOptions options = new AirshipConfigOptions.Builder()
                //.setDevelopmentAppKey("4lf--cI4Q6G9slKKVFh3sA")
                //.setDevelopmentAppSecret("Dbf77hf3RQSE2bPtemY3Bg")
                .setProductionAppKey("IJEPTG2USsyzSL875VtSnQ")
                .setProductionAppSecret("R3TTzi9WQUKZ6_j3OW8Eqw")
                .setInProduction(true)
                .setGcmSender("644407995092")
                .setNotificationIcon(R.drawable.logo)
                .build();

        return options;
    }
}