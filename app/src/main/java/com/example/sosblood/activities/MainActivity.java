package com.example.sosblood.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.example.sosblood.R;
import com.example.sosblood.fragments.DonateFragment;
import com.example.sosblood.fragments.HomeFragment;
import com.example.sosblood.fragments.NotificationFragment;
import com.example.sosblood.fragments.RequestGenerateFragment;
import com.example.sosblood.fragments.RequestStatusFragment;
import com.example.sosblood.models.User;
import com.example.sosblood.others.DummyTabContent;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.utils.CommonTasks;
import com.facebook.login.LoginManager;
import com.urbanairship.UAirship;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements RequestGenerateFragment.RequestGenerateToMainListener,HomeFragment.HomeToMainListener,DonateFragment.DonateToMainListener{

    private TabHost tab_host;
    private TabHost.TabSpec tab_spec;
    private User user;
    private boolean request_exists;
    private MenuItem cancel_request_menu_item;
    private String request_id;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this,new Crashlytics());
        setContentView(R.layout.activity_main);

        Toolbar tool_bar=(Toolbar)findViewById(R.id.main_toolbar_id);
        setSupportActionBar(tool_bar);
        getSupportActionBar().setTitle("SOSBlood");
        CommonTasks.setToolbarFont(tool_bar,this);

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        request_exists=shared_prefs.getBoolean("request_exists",false);
        request_id=shared_prefs.getString("request_id",null);

        setupTabs();

        user=new User();

        getUserDataFromSharedPrefs();

        handler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
                startActivity(getIntent());
                MainActivity.this.finish();
            }
        };

        UAirship.shared().getPushManager().setUserNotificationsEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Use local broadcast manager to receive registration events to update the channel
        IntentFilter channelIdUpdateFilter;
        channelIdUpdateFilter = new IntentFilter();
        channelIdUpdateFilter.addAction("ACTION_UPDATE_CHANNEL");
        locationBroadcastManager.registerReceiver(channelIdUpdateReceiver, channelIdUpdateFilter);

    }

    private BroadcastReceiver channelIdUpdateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("yo",intent.toString());
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(this);
        locationBroadcastManager.unregisterReceiver(channelIdUpdateReceiver);
    }

    private void getUserDataFromSharedPrefs() {
        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        user.setFirst_name(shared_prefs.getString("first_name","First name"));
        user.setId(shared_prefs.getInt("id",0));
        user.setLongitude(Double.valueOf(shared_prefs.getString("longitude","0.0")));
        user.setPicture_url(shared_prefs.getString("picture_url","Picture"));
        user.setLatitude(Double.valueOf(shared_prefs.getString("latitude","0.0")));
        user.setAccess_token(shared_prefs.getString("access_token","Access Token"));
        user.setCity(shared_prefs.getString("city","City"));
        user.setAddress(shared_prefs.getString("address","Address"));
        user.setBlood_group(shared_prefs.getInt("blood_group",0));
        user.setEmail(shared_prefs.getString("email","Email"));
        user.setLast_name(shared_prefs.getString("last_name","Last name"));
    }

    private void setupTabs()
    {
        tab_host=(TabHost)findViewById(R.id.tab_host_id);
        tab_host.setup();

        tab_spec=tab_host.newTabSpec("home");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Home",getResources().getDrawable(R.drawable.home_blue));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("request");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Request",getResources().getDrawable(R.drawable.request_black));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("donate");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Donate",getResources().getDrawable(R.drawable.donate_black));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("notification");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Notifications",getResources().getDrawable(R.drawable.notification_black));
        tab_host.addTab(tab_spec);

        double screen_size_case=getScreenSizeCase();

        for(int i=0;i<tab_host.getTabWidget().getChildCount();i++)
        {
            tab_host.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_widget_color));
            tab_host.getTabWidget().getChildAt(i).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            TextView tv = (TextView) tab_host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));

            switch ((int)(screen_size_case*10))
            {
                case 40:
                {
                    tv.setPadding(0, 0, 0, 7);
                    tv.setTextSize(12);
                    break;
                }
                case 47:
                {
                    tv.setPadding(0, 0, 0, 9);
                    tv.setTextSize(13);
                    break;
                }
                case 55:
                {
                    tv.setPadding(0, 0, 0, 18);
                    tv.setTextSize(14);
                    break;
                }
                case 60:
                {
                    tv.setPadding(0, 0, 0, 25);
                    tv.setTextSize(15);
                    break;
                }
            }

            tv.setShadowLayer(1, 1, 1, Color.LTGRAY);
        }

        initFrag(0);

        TabHost.OnTabChangeListener tab_change_listener=new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tag) {
                changeTab(tag);
            }
        };

        tab_host.setOnTabChangedListener(tab_change_listener);
    }

    private double getScreenSizeCase() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        double wi=(double)width/(double)dm.xdpi;
        double hi=(double)height/(double)dm.ydpi;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        double[] sizes=new double[]{4.0,4.7,5.5,6.0};
        double closest=sizes[0];
        for(int i=1;i<sizes.length;i++)
        {
            if(Math.abs(screenInches-sizes[i])<Math.abs(screenInches-closest))
                closest=sizes[i];
        }
        return closest;
    }

    private void initFrag(int which)
    {
        switch(which)
        {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_container_id,new HomeFragment(),"home").commit();
                break;

            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_container_id,new DonateFragment(),"donate").commit();
                break;

            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_container_id,new NotificationFragment(),"notification").commit();
                break;
        }
    }

    private void changeTab(String tag)
    {
        String title=null;

        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction transaction=fm.beginTransaction();

        HomeFragment home_frag=(HomeFragment) fm.findFragmentByTag("home");
        DonateFragment donate_frag= (DonateFragment) fm.findFragmentByTag("donate");
        NotificationFragment notification_frag= (NotificationFragment) fm.findFragmentByTag("notification");

        RequestStatusFragment req_status_frag=new RequestStatusFragment();
        RequestGenerateFragment req_generate_frag=new RequestGenerateFragment();

        if(request_exists)
        {
            req_status_frag=(RequestStatusFragment)fm.findFragmentByTag("request");
            if(req_status_frag!=null)
                transaction.detach(req_status_frag);
        }
        else
        {
            req_generate_frag=(RequestGenerateFragment)fm.findFragmentByTag("request");
            if(req_generate_frag!=null)
                transaction.detach(req_generate_frag);
        }

        if(home_frag!=null)
            transaction.detach(home_frag);

        if(donate_frag!=null)
            transaction.detach(donate_frag);

        if(notification_frag!=null)
            transaction.detach(notification_frag);

        cancel_request_menu_item.setVisible(false);

        ImageView home_icon=(ImageView) tab_host.getTabWidget().getChildTabViewAt(0).findViewById(android.R.id.icon);
        ImageView request_icon=(ImageView) tab_host.getTabWidget().getChildTabViewAt(1).findViewById(android.R.id.icon);
        ImageView notification_icon=(ImageView) tab_host.getTabWidget().getChildTabViewAt(3).findViewById(android.R.id.icon);
        ImageView donate_icon=(ImageView) tab_host.getTabWidget().getChildTabViewAt(2).findViewById(android.R.id.icon);
        home_icon.setImageDrawable(getResources().getDrawable(R.drawable.home_black));
        request_icon.setImageDrawable(getResources().getDrawable(R.drawable.request_black));
        donate_icon.setImageDrawable(getResources().getDrawable(R.drawable.donate_black));
        notification_icon.setImageDrawable(getResources().getDrawable(R.drawable.notification_black));

        switch(tag)
        {
            case "home":
                home_icon.setImageDrawable(getResources().getDrawable(R.drawable.home_blue));
                title="SOSBlood";
                if(home_frag==null)
                    transaction.add(R.id.tab_content_container_id,new HomeFragment(),"home");
                else
                    transaction.attach(home_frag);
                break;

            case "request":
                request_icon.setImageDrawable(getResources().getDrawable(R.drawable.request_blue));
                title="Request Blood";
                if(request_exists)
                {
                    cancel_request_menu_item.setVisible(true);
                    if(req_status_frag==null)
                        transaction.add(R.id.tab_content_container_id,new RequestStatusFragment(),"request");
                    else
                        transaction.attach(req_status_frag);
                }
                else
                {
                    if(req_generate_frag==null)
                        transaction.add(R.id.tab_content_container_id,new RequestGenerateFragment(),"request");
                    else
                        transaction.attach(req_generate_frag);
                }
                break;

            case "donate":
                donate_icon.setImageDrawable(getResources().getDrawable(R.drawable.donate_blue));
                title="Donate Blood";
                if(donate_frag==null)
                    transaction.add(R.id.tab_content_container_id,new DonateFragment(),"donate");
                else
                    transaction.attach(donate_frag);
                break;

            case "notification":
                notification_icon.setImageDrawable(getResources().getDrawable(R.drawable.notification_blue));
                title="Your Notifications";
                if(notification_frag==null)
                    transaction.add(R.id.tab_content_container_id,new NotificationFragment(),"notification");
                else
                    transaction.attach(notification_frag);
                break;
        }
        transaction.commit();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public User getCurrentUser() {
        return user;
    }

    @Override
    public void restartActivity() {
        startActivity(getIntent());
        finish();
    }

    @Override
    public User getCurrentUserInHome() {
        return user;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        cancel_request_menu_item=menu.findItem(R.id.cancel_request_item_id);
        cancel_request_menu_item.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.feedback_item_id:
                String[] to={"sosbloodapp@gmail.com"};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for SOS BLOOD version 1");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;

            case R.id.logout_item_id:
                LoginManager.getInstance().logOut();
                removeAllSharedPrefs();
                startActivity(new Intent(this,SplashActivity.class));
                finish();
                return true;

            case R.id.cancel_request_item_id:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Cancel Request");
                builder.setMessage("Are you sure you want to cancel your current request?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(request_id!=null)
                            cancelRequest();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeAllSharedPrefs() {
        SharedPreferences shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        shared_prefs.edit().clear().apply();
        shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY,MODE_PRIVATE);
        shared_prefs.edit().clear().apply();
        shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_NOTIFICATIONS_KEY,MODE_PRIVATE);
        shared_prefs.edit().clear().apply();
    }

    private void cancelRequest()
    {
        String url=MyConstants.BASE_URL_API+"blood_requests/"+request_id+"/disable";
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("yo","yaha"+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(MainActivity.this))
                    Toast.makeText(MainActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();

                Log.v("yo",error.getLocalizedMessage()+"\n"+error.getMessage()+"\n"+error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization", user.getAccess_token());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                if(response.statusCode==204)
                {
                    SharedPreferences shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
                    SharedPreferences.Editor editor=shared_prefs.edit();
                    editor.putBoolean("request_exists",false);
                    editor.putString("request_id",null);
                    editor.apply();
                    Message message=handler.obtainMessage();
                    message.sendToTarget();
                }

                return super.parseNetworkResponse(response);
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(request,"request");
    }

    @Override
    public User getCurrentUserInDonate() {
        return user;
    }
}