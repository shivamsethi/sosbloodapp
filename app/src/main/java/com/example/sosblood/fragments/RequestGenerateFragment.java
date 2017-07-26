package com.example.sosblood.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.models.User;
import com.example.sosblood.others.MyApplication;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.utils.CommonTasks;
import com.example.sosblood.utils.CustomSpinnerAdapter;
import com.example.sosblood.utils.FetchAddressIntentService;
import com.example.sosblood.widgets.MySpinner;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class RequestGenerateFragment extends Fragment {

    private MySpinner blood_group_spinner;
    private TextInputLayout region_input_lay,note_input_lay;
    private EditText region_edittext,note_edittext;
    private TextView same_region_textview;
    private RadioGroup radio_group;
    private Button request_button;
    private RadioButton myself_radio_button,someone_else_radio_button;
    private RequestGenerateToMainListener listener;
    private RelativeLayout rel_lay_region,rel_lay_button;
    private LinearLayout parent_lin_lay;
    private User user;
    private double latitude,longitude;
    private ProgressBar region_progress_bar;
    private AddressResultReceiver receiver;
    private ProgressDialog progress_dialog;
    private boolean can_type_city=false,someone_else=false;
    private SparseArray<String> blood_groups;
    private String address;
    private View line_view;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE=1;

    public interface RequestGenerateToMainListener{
        User getCurrentUser();
        void restartActivity();
    }

    public RequestGenerateFragment() {

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener=(RequestGenerateToMainListener)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.fragment_request_generate, container, false);

        receiver=new AddressResultReceiver(new Handler());

        blood_groups=((MyApplication)getActivity().getApplication()).getBloodGroups();

        blood_group_spinner=(MySpinner)view.findViewById(R.id.spinner_blood_group_id);
        radio_group=(RadioGroup)view.findViewById(R.id.request_radio_group_id);
        region_edittext=(EditText)view.findViewById(R.id.region_edittext_id);
        region_input_lay=(TextInputLayout)view.findViewById(R.id.region_input_lay_id);
        same_region_textview=(TextView)view.findViewById(R.id.same_as_mine_textview_id);
        request_button=(Button)view.findViewById(R.id.request_button_id);
        myself_radio_button=(RadioButton)view.findViewById(R.id.myself_radio_button_id);
        someone_else_radio_button=(RadioButton)view.findViewById(R.id.someone_else_radio_button_id);
        note_input_lay=(TextInputLayout)view.findViewById(R.id.note_input_lay_id);
        note_edittext=(EditText)view.findViewById(R.id.note_edittext_id);
        rel_lay_region=(RelativeLayout)view.findViewById(R.id.rel_lay_region_id);
        rel_lay_button=(RelativeLayout)view.findViewById(R.id.rel_lay_button_id);
        parent_lin_lay=(LinearLayout)view.findViewById(R.id.parent_lin_lay_id);
        region_progress_bar=(ProgressBar)view.findViewById(R.id.region_progress_bar_id);
        line_view=view.findViewById(R.id.line_view_id);

        progress_dialog=new ProgressDialog(getActivity());
        progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress_dialog.setMessage("Requesting...");
        progress_dialog.setCancelable(false);

        myself_radio_button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        someone_else_radio_button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        note_input_lay.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        region_input_lay.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));

        if(listener!=null)
            user=listener.getCurrentUser();

        myself_radio_button.setChecked(true);
        hideViews();

        List<String> blood_groups=new ArrayList<>();
        SparseArray<String> bg_array=((MyApplication)getActivity().getApplication()).getBloodGroups();
        for(int i=0;i<bg_array.size();i++)
        {
            blood_groups.add(bg_array.get(i+1));
        }
        CustomSpinnerAdapter adapter=new CustomSpinnerAdapter(getActivity(),blood_groups,"Select Blood Group");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blood_group_spinner.setAdapter(adapter);
        blood_group_spinner.setPrompt("Select blood group");

        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(radioGroup.getCheckedRadioButtonId())
                {
                    case R.id.myself_radio_button_id:
                        someone_else=false;
                        hideViews();
                        break;

                    case R.id.someone_else_radio_button_id:
                        someone_else=true;
                        showViews();
                        break;
                }
            }
        });

        region_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!can_type_city)
                {
                    try
                    {
                        Intent intent=new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                        startActivityForResult(intent,PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        same_region_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                region_edittext.setText(user.getCity());
                latitude=user.getLatitude();
                longitude=user.getLongitude();
            }
        });

        request_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(radio_group.getCheckedRadioButtonId())
                {
                    case R.id.myself_radio_button_id:
                        if(user!=null)
                        {
                            generateBloodRequest(Integer.toString(user.getId()),user.getLatitude(),user.getLongitude(),user.getAddress(),user.getCity(),user.getBlood_group(),note_edittext.getText().toString());
                        }
                        break;

                    case R.id.someone_else_radio_button_id:
                        if(user!=null)
                        {
                            int blood_group;
                            String city;
                            blood_group=blood_group_spinner.getSelectedItemPosition();
                            city=region_edittext.getText().toString();
                            if(blood_group!=0)
                            {
                                if(!city.isEmpty())
                                {
                                    removeErrorFromInputLayout(region_input_lay);
                                    generateBloodRequest(Integer.toString(user.getId()),latitude,longitude,address,city,blood_group,note_edittext.getText().toString());
                                }else
                                {
                                    setErrorOnInputLayout(region_input_lay,"Please select region.");
                                }
                            }else
                            {
                                Toast.makeText(getActivity(), "Please select blood group", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }
            }
        });

        return view;
    }

    private void setErrorOnInputLayout(TextInputLayout layout,String error)
    {
        layout.setErrorEnabled(true);
        layout.setError(error);
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(layout.getEditText(), InputMethodManager.SHOW_IMPLICIT);
    }


    private void removeErrorFromInputLayout(TextInputLayout layout)
    {
        layout.setErrorEnabled(false);
    }

    private void generateBloodRequest(String id, double latitude, double longitude, String address,String city, final int bgroup, String note)
    {
        progress_dialog.show();

        String url=MyConstants.BASE_URL_API+"blood_requests/";
        JSONObject blood_req_json=new JSONObject();
        JSONObject json=new JSONObject();
        try
        {
            blood_req_json.put("latitude",latitude);
            blood_req_json.put("longitude",longitude);
            blood_req_json.put("address",address);
            blood_req_json.put("city",city);
            blood_req_json.put("bgroup",bgroup);
            blood_req_json.put("request_type",1);
            blood_req_json.put("note",note);
            blood_req_json.put("someone_else",someone_else);
            json.put("blood_request",blood_req_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("yo","sending generate req \n"+blood_req_json.toString());

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json_response) {
                JSONObject response=new JSONObject();
                Log.v("yo","req generated \n"+json_response.toString());
                View view=getActivity().getLayoutInflater().inflate(R.layout.request_acknowledge,null);
                TextView bgroup_tv,region_tv,age_tv;
                Button okay_button=(Button)view.findViewById(R.id.okay_button_id);
                bgroup_tv=(TextView)view.findViewById(R.id.blood_group_textview_id);
                region_tv=(TextView)view.findViewById(R.id.region_textview_id);
                try
                {
                    response=json_response.getJSONObject("blood_request");
                    bgroup_tv.setText(blood_groups.get(response.getInt("bgroup")));
                    region_tv.setText(response.getString("city"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                        .setView(view);

                final AlertDialog dialog=builder.create();
                progress_dialog.dismiss();
                dialog.show();
                okay_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(dialog.isShowing())
                            dialog.dismiss();

                        listener.restartActivity();
                    }
                });

                SharedPreferences shared_prefs=getActivity().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
                SharedPreferences.Editor editor=shared_prefs.edit();
                editor.putBoolean("request_exists",true);
                try
                {
                    editor.putString("request_id",response.getString("id"));
                    editor.putInt("request_blood_group",response.getInt("bgroup"));
                    editor.putString("request_city",response.getString("city"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                editor.apply();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progress_dialog.isShowing())
                    progress_dialog.dismiss();
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(getActivity()))
                    Toast.makeText(getActivity(), "Network connectivity problem", Toast.LENGTH_SHORT).show();

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
        };

        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"blood_request");
    }

    private void hideViews() {

        blood_group_spinner.setVisibility(View.INVISIBLE);
        region_input_lay.setVisibility(View.INVISIBLE);
        same_region_textview.setVisibility(View.INVISIBLE);
        line_view.setVisibility(View.INVISIBLE);
        try
        {
            ((ViewGroup)blood_group_spinner.getParent()).removeView(blood_group_spinner);
            ((ViewGroup)line_view.getParent()).removeView(line_view);
            ((ViewGroup)rel_lay_region.getParent()).removeView(rel_lay_region);
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void showViews()
    {
        blood_group_spinner.setVisibility(View.VISIBLE);
        line_view.setVisibility(View.VISIBLE);
        region_input_lay.setVisibility(View.VISIBLE);
        same_region_textview.setVisibility(View.VISIBLE);
        ((ViewGroup)rel_lay_button.getParent()).removeView(rel_lay_button);
        ((ViewGroup)note_input_lay.getParent()).removeView(note_input_lay);
        parent_lin_lay.addView(blood_group_spinner);
        parent_lin_lay.addView(line_view);
        parent_lin_lay.addView(rel_lay_region);
        parent_lin_lay.addView(note_input_lay);
        parent_lin_lay.addView(rel_lay_button);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            switch (requestCode)
            {
                case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                    Place place=PlaceAutocomplete.getPlace(getActivity(),data);
                    if(place!=null)
                    {
                        region_progress_bar.setVisibility(View.VISIBLE);
                        Location location=new Location("");
                        location.setLatitude(place.getLatLng().latitude);
                        location.setLongitude(place.getLatLng().longitude);
                        startIntentService(location);
                    }
                    break;
            }
        }
    }


    private void startIntentService(Location location)
    {
        latitude=location.getLatitude();
        longitude=location.getLongitude();

        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(MyConstants.RECEIVER, receiver);
        intent.putExtra(MyConstants.LOCATION_DATA_EXTRA, location);
        getActivity().startService(intent);
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
            {
                region_edittext.setText(resultData.getString(MyConstants.RESULT_DATA_KEY));
                address=resultData.getString(MyConstants.ADDRESS_DATA_KEY);
            }
            else
            {
                if(!resultData.getString(MyConstants.RESULT_DATA_KEY).equals(getResources().getString(R.string.invalid_lat_lng)))
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setTitle("Oooops!!");
                    builder.setMessage("Seems that Location service didn't work as expected. This is a temporary problem, no worries. You can still type city manually. Go on.");
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
                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage("Problem getting location. Please request again. If problem persists, try after an hour. We value your each second.");
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