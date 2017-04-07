package com.example.sosblood.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.sosblood.R;
import com.example.sosblood.fragments.DonateFragment;
import com.example.sosblood.fragments.HomeFragment;
import com.example.sosblood.fragments.NotificationFragment;
import com.example.sosblood.fragments.RequestGenerateFragment;
import com.example.sosblood.fragments.RequestStatusFragment;
import com.example.sosblood.models.User;
import com.example.sosblood.others.DummyTabContent;
import com.example.sosblood.others.MyConstants;
import com.facebook.login.LoginManager;

public class MainActivity extends AppCompatActivity implements RequestGenerateFragment.RequestGenerateToMainListener,HomeFragment.HomeToMainListener{

    private TabHost tab_host;
    private TabHost.TabSpec tab_spec;
    private User user;
    private boolean request_exists;
    private MenuItem cancel_request_menu_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar tool_bar=(Toolbar)findViewById(R.id.main_toolbar_id);
        setSupportActionBar(tool_bar);
        getSupportActionBar().setTitle("SOS Blood");

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        request_exists=shared_prefs.getBoolean("request_exists",false);

        setupTabs();

        user=new User();

        getUserDataFromSharedPrefs();
    }

    private void getUserDataFromSharedPrefs() {
        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        user.setFirst_name(shared_prefs.getString("first_name","First name"));
        user.setId(shared_prefs.getInt("id",0));
        user.setLongitude(Double.valueOf(shared_prefs.getString("longitude","0.0")));
        user.setPicture_url(shared_prefs.getString("picture_url","Picture"));
        user.setLatitude(Double.valueOf(shared_prefs.getString("latitude","0.0")));
        user.setAccess_token(shared_prefs.getString("access_token","Access Token"));
        user.setAddress(shared_prefs.getString("address","Address"));
        user.setBlood_group(shared_prefs.getInt("blood_group",0));
        user.setEmail(shared_prefs.getString("email","Email"));
        user.setLast_name(shared_prefs.getString("last_name","Last name"));
        user.setAge(shared_prefs.getInt("age",21));
    }

    private void setupTabs()
    {
        tab_host=(TabHost)findViewById(R.id.tab_host_id);
        tab_host.setup();

        tab_spec=tab_host.newTabSpec("home");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Home",getResources().getDrawable(R.drawable.ic_home_black_36dp));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("request");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Request",getResources().getDrawable(R.drawable.request));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("donate");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Donate",getResources().getDrawable(R.drawable.donate));
        tab_host.addTab(tab_spec);

        tab_spec=tab_host.newTabSpec("notification");
        tab_spec.setContent(new DummyTabContent(this));
        tab_spec.setIndicator("Notifications",getResources().getDrawable(R.drawable.ic_notifications_black_36dp));
        tab_host.addTab(tab_spec);

        for(int i=0;i<tab_host.getTabWidget().getChildCount();i++)
        {
            tab_host.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_widget_color));
            tab_host.getTabWidget().getChildAt(i).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
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
        switch(tag)
        {
            case "home":
                title="SOS Blood";
                if(home_frag==null)
                    transaction.add(R.id.tab_content_container_id,new HomeFragment(),"home");
                else
                    transaction.attach(home_frag);
                break;

            case "request":
                title="Request";
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
                title="Donate";
                if(donate_frag==null)
                    transaction.add(R.id.tab_content_container_id,new DonateFragment(),"donate");
                else
                    transaction.attach(donate_frag);
                break;

            case "notification":
                title="Notifications";
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
    public User getCurrentUserinHome() {
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
            case R.id.logout_item_id:
                LoginManager.getInstance().logOut();
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

    private void cancelRequest()
    {
        SharedPreferences shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putBoolean("request_exists",false);
        editor.putString("request_id",null);
        editor.apply();
        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
        startActivity(getIntent());
        finish();
    }
}