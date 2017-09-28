package com.shivam.sosblood.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shivam.sosblood.R;
import com.shivam.sosblood.others.MyConstants;

import static android.content.Context.MODE_PRIVATE;

public class CommonTasks {

    public static void showImageDialog(Activity activity, Bitmap bitmap){
        View view=activity.getLayoutInflater().inflate(R.layout.image_dialog,null);
        ImageView image_view=(ImageView)view.findViewById(R.id.image_view_id);
        image_view.setImageBitmap(bitmap);
        AlertDialog.Builder builder=new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    public static void setToolbarFont(Toolbar toolbar, Context context)
    {
        for(int i=0;i<toolbar.getChildCount();i++)
        {
            View view=toolbar.getChildAt(i);
            if(view instanceof TextView)
            {
                ((TextView) view).setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
            }
        }
    }

    public static boolean isNetworkAvailable(Context context)
    {
        try
        {
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isWifi(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static String getMyRequestId(Context context){
        SharedPreferences shared_prefs=context.getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        return shared_prefs.getString("request_id",null);
    }

}
