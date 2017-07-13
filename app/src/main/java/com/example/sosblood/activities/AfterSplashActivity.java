package com.example.sosblood.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.models.User;
import com.example.sosblood.others.MyApplication;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.others.MySpinner;
import com.example.sosblood.utils.CustomSpinnerAdapter;
import com.example.sosblood.utils.FetchAddressIntentService;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfterSplashActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private ProgressDialog dialog;
    private ImageView image_view;
    private TextView name_textview,email_textview;
    private MySpinner blood_group_spinner;
    private TextInputLayout region_input_lay;
    private EditText region_edittext;
    private ProgressBar dp_progress_bar,region_progress_bar;
    private User user;
    private GoogleApiClient google_api_client;
    private LocationRequest location_request;
    private AddressResultReceiver receiver;
    private double latitude,longitude;
    private boolean can_type_city=false;

    private static final int REQUEST_LOCATION_SETTINGS=1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE=2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_splash);

        receiver=new AddressResultReceiver(new Handler());

        setupLocationPermission();

        user=(User)(getIntent().getBundleExtra(SplashActivity.USER_PASSING_TAG).getSerializable(SplashActivity.USER_PASSING_TAG));

        image_view=(ImageView)findViewById(R.id.dp_image_view_id);
        dp_progress_bar=(ProgressBar)findViewById(R.id.dp_progress_bar_id);
        name_textview=(TextView)findViewById(R.id.name_textview_id);
        email_textview=(TextView)findViewById(R.id.email_textview_id);
        blood_group_spinner=(MySpinner)findViewById(R.id.spinner_blood_group_id);
        region_input_lay=(TextInputLayout)findViewById(R.id.region_input_lay_id);
        region_edittext=(EditText)findViewById(R.id.region_edittext_id);
        region_progress_bar=(ProgressBar)findViewById(R.id.region_progress_bar_id);
        dialog=new ProgressDialog(this);

        dialog.setMessage("Just a moment...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        region_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!can_type_city)
                {
                    try
                    {
                        Intent intent=new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(AfterSplashActivity.this);
                        startActivityForResult(intent,PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        List<String> blood_groups=new ArrayList<>();
        SparseArray<String> bg_array=((MyApplication)this.getApplication()).getBloodGroups();
        for(int i=0;i<bg_array.size();i++)
        {
            blood_groups.add(bg_array.get(i+1));
        }
        CustomSpinnerAdapter adapter=new CustomSpinnerAdapter(this,blood_groups,"Select Blood Group");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blood_group_spinner.setAdapter(adapter);
        blood_group_spinner.setPrompt("Select blood group");

        name_textview.setText(user.getFirst_name()+" "+user.getLast_name());
        email_textview.setText(user.getEmail());

        ImageRequest request=new ImageRequest(user.getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                dp_progress_bar.setVisibility(View.INVISIBLE);
                image_view.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dp_progress_bar.setVisibility(View.INVISIBLE);
                Toast.makeText(AfterSplashActivity.this, "Can't load Photo", Toast.LENGTH_SHORT).show();
                Log.v("yo",""+error.getLocalizedMessage());
                Log.v("yo",""+error.getMessage());
                Log.v("yo",""+error.toString());
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(request,"image_tag");
    }

    private void setupLocationPermission() {
        int permission= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission== PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(AfterSplashActivity.this);
                builder.setTitle("Alert");
                builder.setMessage("SOS Blood can automatically detect your location for storing your city information. For this,location permission is required. Kindly click on Allow in the next step.");
                builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(AfterSplashActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else if(permission==PackageManager.PERMISSION_GRANTED)
            setupLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
                setupLocation();
                break;
        }
    }

    private void setupLocation() {

        google_api_client=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        location_request=LocationRequest.create()
                .setFastestInterval(2000)
                .setInterval(4000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(location_request);
        PendingResult<LocationSettingsResult> result=LocationServices.SettingsApi.checkLocationSettings(google_api_client,builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status=locationSettingsResult.getStatus();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        fetchLocation();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try
                        {
                            status.startResolutionForResult(AfterSplashActivity.this,REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

        google_api_client.connect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            switch(requestCode)
            {
                case REQUEST_LOCATION_SETTINGS:
                    fetchLocation();
                    break;

                case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                    Place place=PlaceAutocomplete.getPlace(AfterSplashActivity.this,data);
                    if(place!=null)
                    {
                        region_progress_bar.setVisibility(View.VISIBLE);
                        Location location=new Location("");
                        location.setLatitude(place.getLatLng().latitude);
                        location.setLongitude(place.getLatLng().longitude);
                        startIntentService(location);
                    }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        try
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client,this);
            google_api_client.disconnect();
        }catch(IllegalStateException e)
        {
            e.printStackTrace();
        }
        super.onStop();
    }


    private void startIntentService(Location location)
    {
        latitude=location.getLatitude();
        longitude=location.getLongitude();

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(MyConstants.RECEIVER, receiver);
        intent.putExtra(MyConstants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }


    private void fetchLocation() {
        region_progress_bar.setVisibility(View.VISIBLE);
        try
        {
            Location last_location=LocationServices.FusedLocationApi.getLastLocation(google_api_client);
            if(last_location==null)
                LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client,location_request,AfterSplashActivity.this);
            else
            {
                startIntentService(last_location);
            }
        }catch(SecurityException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Enter region manually. Auto detect not working.", Toast.LENGTH_SHORT).show();
            region_progress_bar.setVisibility(View.INVISIBLE);
        }
    }

    public void actButtonBack(View view) {
        super.onBackPressed();
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginManager.getInstance().logOut();
    }

    public void actButtonNext(View view) {
        if(blood_group_spinner.getSelectedItemPosition()!=0)
        {
            if(!region_edittext.getText().toString().isEmpty())
            {
                int blood_group=blood_group_spinner.getSelectedItemPosition();
                String address=region_edittext.getText().toString();
                hitSecondApi(blood_group,address);
            }
            else
            {
                Toast.makeText(this, "Please select your city.", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Select your blood group.", Toast.LENGTH_SHORT).show();
        }
    }


    private void hitSecondApi(final int blood_group, final String address)
    {
        dialog.show();
        String url=MyConstants.BASE_URL_API+"users/"+user.getId();
        JSONObject user_json=new JSONObject();
        JSONObject json=new JSONObject();
        try
        {
            user_json.put("bgroup",blood_group);
            user_json.put("latitude",latitude);
            user_json.put("longitude",longitude);
            user_json.put("address",address);
            json.put("user",user_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.PUT, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",user.getAccess_token());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if(response.statusCode==204)
                {
                    dialog.dismiss();
                    appendUserLocally(blood_group,address);
                    startActivity(new Intent(AfterSplashActivity.this,MainActivity.class));
                    finish();
                }
                return super.parseNetworkResponse(response);
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(request,"second_tag");
    }

    private void appendUserLocally(int blood_group, String address) {
        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY,MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putInt("blood_group",blood_group);
        editor.putString("address",address);
        editor.putString("latitude",Double.valueOf(latitude).toString());
        editor.putString("longitude",Double.valueOf(longitude).toString());
        editor.apply();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        startIntentService(location);
        LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dialog.isShowing())
            dialog.dismiss();
    }

    class AddressResultReceiver extends ResultReceiver
    {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            region_progress_bar.setVisibility(View.INVISIBLE);
            if(resultCode==MyConstants.SUCCESS_RESULT)
                region_edittext.setText(resultData.getString(MyConstants.RESULT_DATA_KEY));
            else
            {
                if(!resultData.getString(MyConstants.RESULT_DATA_KEY).equals(getResources().getString(R.string.invalid_lat_lng)))
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(AfterSplashActivity.this);
                    builder.setTitle("Oooops!!");
                    builder.setMessage("Seems that Location service didn't work as expected. This is a temporary problem, no worries. You can still type your city manually. Go on.");
                    builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog dialog1=builder.create();
                    dialog1.show();
                    can_type_city=true;
                    region_edittext.setFocusableInTouchMode(true);
                    region_edittext.setFocusable(true);
                }else
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(AfterSplashActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Problem getting location. Please go back and login again. If problem persists, try after an hour. We value your each second.");
                    builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog dialog1=builder.create();
                    dialog1.show();
                }
            }
        }
    }
}