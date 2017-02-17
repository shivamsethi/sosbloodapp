package com.example.sosblood.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.models.User;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.utils.JSONParser;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    public static final String USER_PASSING_TAG="fb_user";

    private VideoView video_view;
    private CarouselView carousel_view;
    private LoginButton fb_login_button;
    private CallbackManager call_back_manager;
    private ProfileTracker profile_tracker;
    private JsonObjectRequest fb_login_request;
    private String profile_pic_url;
    private ProgressDialog dialog;

    String[] carousel_texts={"Find blood donors with our powerful algorithms","Be a donor and do noble work","Can easily connect with facebook account","Take SOS where ever you go"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        /*if(AccessToken.getCurrentAccessToken()!=null)
            startActivity(new Intent(this,MainActivity.class));*/

        setContentView(R.layout.activity_splash);

        video_view=(VideoView)findViewById(R.id.video_view_id);
        carousel_view=(CarouselView)findViewById(R.id.carousel_view_id);
        fb_login_button=(LoginButton)findViewById(R.id.fb_login_button_id);
        dialog=new ProgressDialog(this);
        dialog.setMessage("Signing in...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        call_back_manager=CallbackManager.Factory.create();


        if(AccessToken.getCurrentAccessToken()!=null)
            loginToServer();


        fb_login_button.setReadPermissions(Arrays.asList("email","user_birthday"));
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
                Toast.makeText(SplashActivity.this,"Error "+error.toString()+error.getMessage()+error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.v("yo","Error "+error.toString()+error.getMessage()+error.getLocalizedMessage());
                error.printStackTrace();

            }
        });


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

        final MediaController media_controller=new MediaController(this);
        media_controller.setAnchorView(video_view);
        video_view.setMediaController(null);

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blood);
        video_view.setVideoURI(video);

        video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(0f,0f);
            }
        });


        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                video_view.start();
            }
        });


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
    }


    private void loginToServer()
    {
        dialog.show();
        Map<String,String> params=new HashMap<>();
        params.put("fb_access_token",AccessToken.getCurrentAccessToken().getToken());

        final String url= MyConstants.BASE_URL_API+"fbconnect";

        fb_login_request =new JsonObjectRequest(Request.Method.POST, url,new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                User user= JSONParser.parseFbLogin(response);

                storeUserLocally(user);

                Bundle bundle=new Bundle();
                bundle.putSerializable(USER_PASSING_TAG,user);

                dialog.dismiss();

                Intent intent=new Intent(SplashActivity.this,AfterSplashActivity.class);
                intent.putExtra(USER_PASSING_TAG,bundle);
                startActivity(intent);

//                if(user.getBlood_group()==null)
//                {
//                    Intent intent=new Intent(SplashActivity.this,AfterSplashActivity.class);
//                    intent.putExtra(USER_PASSING_TAG,bundle);
//                    startActivity(intent);
//                }else
//                {
//                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
//                    intent.putExtra(USER_PASSING_TAG,bundle);
//                    startActivity(intent);
//                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                dialog.dismiss();
                NetworkResponse response=error.networkResponse;
                if(error instanceof ServerError && response!=null)
                {
                    try
                    {
                        String res=new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        Log.v("yo",res);
                        JSONObject obj=new JSONObject(res);
                        Log.v("yo",obj.toString());
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                        Log.v("yo","Again error in Exception");
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

    private void storeUserLocally(User user) {
        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putString("email",user.getEmail());
        editor.putString("first_name",user.getFirst_name());
        editor.putString("last_name",user.getLast_name());
        editor.putString("picture_url",user.getPicture_url());
        editor.putInt("id",user.getId());
        editor.putString("access_token",user.getAccess_token());
        editor.putInt("age",user.getAge());
        if(user.getBlood_group()!=null)
        {
            editor.putInt("blood_group",user.getBlood_group());
            editor.putString("address",user.getAddress());
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
    protected void onResume() {
        super.onResume();
        video_view.requestFocus();
        video_view.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        call_back_manager.onActivityResult(requestCode,resultCode,data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        profile_tracker.stopTracking();
    }


    @Override
    protected void onStop() {
        super.onStop();
        MySingleton.getInstance(this).getRequestQueue().cancelAll("login_tag");
    }
}