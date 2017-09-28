package com.shivam.sosblood.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.shivam.sosblood.others.MyConstants;

import java.util.List;

public class ParseDeepLinkingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        String access_token=shared_prefs.getString("access_token","Access Token");

        List<String> params_list=getIntent().getData().getPathSegments();
        try{
            switch(params_list.get(0))
            {
                case "request":
                {
                    Uri uri=Uri.parse(getIntent().getData().toString());
                    String request_id=uri.getQueryParameter("r");
                    Bundle bundle=new Bundle();
                    bundle.putString("request_id",request_id);
                    bundle.putSerializable("access_token",access_token);
                    Intent intent=new Intent(this, NeedyDetailActivity.class);
                    intent.putExtra("needy_person",bundle);
                    startActivity(intent);
                    finish();
                    break;
                }

                case "request_generated":
                {
                    Bundle bundle=new Bundle();
                    bundle.putString("request_id",params_list.get(1));
                    bundle.putSerializable("access_token",access_token);
                    Intent intent=new Intent(this, NeedyDetailActivity.class);
                    intent.putExtra("needy_person",bundle);
                    startActivity(intent);
                    finish();
                    break;
                }

                case "request_accepted":
                {
                    Bundle bundle=new Bundle();
                    bundle.putString("donor_id",params_list.get(1));
                    bundle.putString("access_token",access_token);
                    Intent intent=new Intent(this, DonorDetailActivity.class);
                    intent.putExtra("donor",bundle);
                    startActivity(intent);
                    finish();
                    break;
                }
                case "ginapp":
                {
                    Intent intent1=new Intent(this,WebViewActivity.class);
                    intent1.putExtra("url","https://www.goo.gl/"+params_list.get(1));
                    startActivity(intent1);
                    break;
                }
            }
        }catch(IndexOutOfBoundsException e)
        {
            FacebookSdk.sdkInitialize(getApplicationContext());
            if(AccessToken.getCurrentAccessToken()!=null)
            {
                startActivity(new Intent(this,MainActivity.class));
                finish();
            }
            else
            {
                startActivity(new Intent(this,SplashActivity.class));
                finish();
            }
        }
    }
}