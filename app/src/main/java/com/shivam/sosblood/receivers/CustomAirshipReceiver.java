package com.shivam.sosblood.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.shivam.sosblood.others.MyConstants;
import com.urbanairship.AirshipReceiver;

import static android.content.Context.MODE_PRIVATE;

public class CustomAirshipReceiver extends AirshipReceiver {

    @Override
    protected void onChannelCreated(@NonNull Context context, @NonNull String channelId) {
        super.onChannelCreated(context, channelId);

        Log.v("yo","channel created");

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("ACTION_UPDATE_CHANNEL"));
    }

    @Override
    protected void onChannelUpdated(@NonNull Context context, @NonNull String channelId) {
        super.onChannelUpdated(context, channelId);

        Log.v("yo","channel updated");
    }

    @Override
    protected void onChannelRegistrationFailed(@NonNull Context context) {
        super.onChannelRegistrationFailed(context);

        Log.v("yo","channel reg failed");
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        super.onNotificationPosted(context, notificationInfo);

        String deep_link=notificationInfo.getMessage().getActions().get("^d").getString();
        String path=deep_link.substring(34);
        String[] parts=path.split("/");

        saveNotification(parts[0],parts[1],notificationInfo.getMessage().getAlert(),context);
    }

    private void saveNotification(String type,String id,String title,Context context)
    {
        SharedPreferences shared_prefs=context.getSharedPreferences(MyConstants.SHARED_PREFS_NOTIFICATIONS_KEY,MODE_PRIVATE);
        int notifications_array_length=shared_prefs.getInt("notifications_array_length",0);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putString("notification_type_"+notifications_array_length,type);
        editor.putString("id_"+notifications_array_length,id);
        editor.putString("title_"+notifications_array_length,title);
        editor.putBoolean("notification_clicked_"+notifications_array_length,false);
        editor.putInt("notifications_array_length",notifications_array_length+1);
        editor.apply();
    }
}