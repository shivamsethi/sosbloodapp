package com.shivam.sosblood.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.shivam.sosblood.R;
import com.shivam.sosblood.models.User;
import com.shivam.sosblood.others.MyConstants;
import com.shivam.sosblood.others.MySingleton;
import com.shivam.sosblood.utils.CommonTasks;
import com.shivam.sosblood.utils.JSONParser;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;
import com.urbanairship.UAirship;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    public static final String USER_PASSING_TAG="fb_user";

    private CarouselView carousel_view;
    private LoginButton fb_login_button;
    private CallbackManager call_back_manager;
    private ProfileTracker profile_tracker;
    private JsonObjectRequest fb_login_request;
    private String profile_pic_url;
    private ProgressDialog dialog;

    String[] carousel_texts={"Blood Donors Community","Pure Blood Repository are humans around you and not central blood banks","Be a blood donor and contribute to save lives","Sign up to request blood or donate to a needy"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        if(AccessToken.getCurrentAccessToken()!=null)
        {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_splash);

        carousel_view=(CarouselView)findViewById(R.id.carousel_view_id);
        fb_login_button=(LoginButton)findViewById(R.id.fb_login_button_id);
        dialog=new ProgressDialog(this);
        dialog.setMessage("Signing in...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        fb_login_button.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));
        fb_login_button.setBackgroundColor(Color.parseColor("#ffffff"));
        fb_login_button.setBackground(getResources().getDrawable(R.drawable.round_cornered_button));

        call_back_manager=CallbackManager.Factory.create();

        profile_tracker=new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if(oldProfile!=null && currentProfile==null)
                {
                    Toast.makeText(SplashActivity.this,oldProfile.getName()+" logged out.", Toast.LENGTH_SHORT).show();
                }
                if(oldProfile==null && currentProfile!=null)
                {
                    profile_pic_url=currentProfile.getProfilePictureUri(150,150).toString();
                }
            }
        };
        profile_tracker.startTracking();

        fb_login_button.setReadPermissions("email");
        fb_login_button.registerCallback(call_back_manager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if(loginResult.getAccessToken()!=null)
                {
                    if(!AccessToken.getCurrentAccessToken().isExpired())
                    {
                        loginToServer();
                    }else
                    {
                        Toast.makeText(SplashActivity.this, "Token expired. Please login again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SplashActivity.this,error.toString()+"\n"+error.getMessage()+"\n"+error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });


//        final MediaController media_controller=new MediaController(this);
//        media_controller.setAnchorView(video_view);
//        video_view.setMediaController(null);
//
//        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blood);
//        video_view.setVideoURI(video);
//
//        video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setVolume(0f,0f);
//            }
//        });
//
//
//        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                video_view.start();
//            }
//        });


        carousel_view.setPageCount(4);
        carousel_view.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View view=getLayoutInflater().inflate(R.layout.custom_carousel_view,null);

                TextView tv=(TextView)view.findViewById(R.id.carousel_textview_id);
                tv.setText(carousel_texts[position]);

                return view;
            }
        });

        /*if(AccessToken.getCurrentAccessToken()!=null)
        {
            loginToServer();
        }*/
    }

    private void loginToServer()
    {
        dialog.show();
        Map<String,String> params=new HashMap<>();
        params.put("fb_access_token",AccessToken.getCurrentAccessToken().getToken());
        params.put("channel_id", UAirship.shared().getPushManager().getChannelId());

        final String url= MyConstants.BASE_URL_API+"fbconnect";

        fb_login_request =new JsonObjectRequest(Request.Method.POST, url,new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                User user= JSONParser.parseFbLogin(response);
                user.setPicture_url(profile_pic_url);

                storeUserLocally(user);
                try
                {
                    storeLastRequestInfo(response.getJSONObject("last_blood_request"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
                    SharedPreferences.Editor editor=shared_prefs.edit();
                    editor.putBoolean("request_exists",false);
                    editor.apply();
                }

                Bundle bundle=new Bundle();
                bundle.putSerializable(USER_PASSING_TAG,user);

                dialog.dismiss();

//                Intent intent=new Intent(SplashActivity.this,AfterSplashActivity.class);
//                intent.putExtra(USER_PASSING_TAG,bundle);
//                startActivity(intent);
//                finish();

                if(user.getBlood_group()==null)
                {
                    Intent intent=new Intent(SplashActivity.this,AfterSplashActivity.class);
                    intent.putExtra(USER_PASSING_TAG,bundle);
                    startActivity(intent);
                    finish();
                }else
                {
                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(SplashActivity.this))
                    Toast.makeText(SplashActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();

                if(CommonTasks.isWifi(SplashActivity.this))
                    Toast.makeText(SplashActivity.this, "Make sure your wifi connection has active internet.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                NetworkResponse response=error.networkResponse;
                if(error instanceof ServerError && response!=null)
                {
                    try
                    {
                        String res=new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        JSONObject obj=new JSONObject(res);
                        if(obj.getString("fb_error_type").equals("OAuthException"))
                        {
                            AlertDialog.Builder builder=new AlertDialog.Builder(SplashActivity.this);
                            builder.setTitle("Error");
                            builder.setMessage("Could not authenticate you. Make sure you gave right username and password for facebook. In case you changed your facebook password or removed SOS Blood from your approved facebook apps, please logout and login again.");
                            builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog dialog2=builder.create();
                            dialog2.show();
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                return headers;
            }
        };

        fb_login_request.setRetryPolicy(new DefaultRetryPolicy(20000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(SplashActivity.this).addToRequestQueue(fb_login_request,"login_tag");
    }

    private void storeLastRequestInfo(JSONObject obj)
    {
        try
        {
            if(obj!=null && (obj.getBoolean("enabled")||obj.get("enabled")==null))
            {
                SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
                SharedPreferences.Editor editor=shared_prefs.edit();
                editor.putBoolean("request_exists",true);
                editor.putString("request_id", obj.getString("id"));
                editor.putString("request_address", obj.getString("address"));
                editor.putInt("request_blood_group", obj.getInt("bgroup"));
                editor.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeUserLocally(User user)
    {
        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putString("email",user.getEmail());
        editor.putString("first_name",user.getFirst_name());
        editor.putString("last_name",user.getLast_name());
        editor.putString("picture_url",user.getPicture_url());
        editor.putInt("id",user.getId());
        editor.putString("access_token",user.getAccess_token());
        if(user.getBlood_group()!=null)
        {
            editor.putInt("blood_group",user.getBlood_group());
            editor.putString("address",user.getAddress());
            editor.putString("city",user.getCity());
        }
        if(user.getLatitude()!=null)
        {
            editor.putString("latitude",user.getLatitude().toString());
        }
        if(user.getLongitude()!=null)
        {
            editor.putString("longitude",user.getLongitude().toString());
        }
        editor.apply();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        //video_view.requestFocus();
        //video_view.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        call_back_manager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profile_tracker.stopTracking();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(dialog.isShowing())
            dialog.dismiss();
    }
}