package com.shivam.sosblood.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.shivam.sosblood.R;
import com.shivam.sosblood.others.MyApplication;
import com.shivam.sosblood.utils.CommonTasks;
import com.shivam.sosblood.utils.CustomSpinnerAdapter;
import com.shivam.sosblood.widgets.MySpinner;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class SignUpActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private MySpinner blood_group_spinner;
    private EditText region_edit;
    private TextInputLayout region_input_lay;
    private ImageView image_view;
    private GoogleApiClient google_api_client;
    private LocationRequest location_request;
    private ProgressBar progress_bar_auto_detect;

    private final int PICK_IMAGE=1;
    private final int REQUEST_LOCATION_SETTINGS=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar=(Toolbar)findViewById(R.id.signup_toolbar_id);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SOS Blood");
        CommonTasks.setToolbarFont(toolbar,this);

        blood_group_spinner=(MySpinner) findViewById(R.id.spinner_blood_group_id);
        image_view=(ImageView)findViewById(R.id.image_view_id);
        region_edit=(EditText)findViewById(R.id.region_edittext_id);
        region_input_lay=(TextInputLayout)findViewById(R.id.region_input_lay_id);
        progress_bar_auto_detect=(ProgressBar)findViewById(R.id.region_progress_bar_id);

        progress_bar_auto_detect.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

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

        google_api_client=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        location_request=LocationRequest.create()
                .setFastestInterval(2000)
                .setInterval(4000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void actFabChangePic(View view) {
        CharSequence options[]=new CharSequence[]{""};
        MultiImageSelector.create()
                .single()
                .start(this,PICK_IMAGE);
    }

    public void actTextViewAutoDetect(View view)
    {
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(location_request);
        PendingResult<LocationSettingsResult> result=LocationServices.SettingsApi.checkLocationSettings(google_api_client,builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status=locationSettingsResult.getStatus();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        fetchLocation();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(SignUpActivity.this,REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(SignUpActivity.this, "Sorry, auto detect not working. Please enter region manually", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }


    private void fetchLocation()
    {
        progress_bar_auto_detect.setVisibility(View.VISIBLE);
        Toast.makeText(SignUpActivity.this, "Getting your location...", Toast.LENGTH_SHORT).show();
        region_edit.setText("");

        try
        {
            Location last_location=LocationServices.FusedLocationApi.getLastLocation(google_api_client);
            if(last_location==null)
                LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client,location_request,SignUpActivity.this);
            else
            {
                region_edit.setText(last_location.getLatitude()+", "+last_location.getLongitude());
                progress_bar_auto_detect.setVisibility(View.INVISIBLE);
            }
        }catch(SecurityException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show();
            progress_bar_auto_detect.setVisibility(View.INVISIBLE);
        }
    }


    public void actTextViewChooseLoc(View view) {
        Toast.makeText(this, "choose loc", Toast.LENGTH_SHORT).show();
    }

    public void actTextViewToS(View view) {
        Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show();
    }

    public void actButtonSignUp(View view) {
        Toast.makeText(this, "Sign up", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            switch (requestCode)
            {
                case PICK_IMAGE:
                    List<String> path=data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    Uri uri=Uri.fromFile(new File(path.get(0)));
                    CropImage.activity(uri)
                            .setAspectRatio(1,1)
                            .setActivityTitle("Crop Image")
                            .setMultiTouchEnabled(true)
                            .start(this);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    Uri uri1=CropImage.getActivityResult(data).getUri();
                    image_view.setImageURI(uri1);
                    break;

                case REQUEST_LOCATION_SETTINGS:
                    fetchLocation();
                    break;
            }
        }
        else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
        {
            Toast.makeText(this, "Error in cropping image", Toast.LENGTH_SHORT).show();
        }
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
    protected void onStart() {
        google_api_client.connect();
        super.onStart();
    }


    @Override
    protected void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client,this);
        google_api_client.disconnect();
        super.onStop();
    }


    @Override
    public void onLocationChanged(Location location) {
        region_edit.setText(location.getLatitude()+", "+location.getLongitude());
        progress_bar_auto_detect.setVisibility(View.INVISIBLE);
    }
}