package com.example.sosblood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.models.User;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.others.MySpinner;
import com.example.sosblood.utils.CustomSpinnerAdapter;
import com.example.sosblood.utils.FetchAddressIntentService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class RequestGenerateFragment extends Fragment {

    private MySpinner blood_group_spinner;
    private TextInputLayout region_input_lay,age_input_lay,note_input_lay;
    private EditText region_edittext,age_edittext,note_edittext;
    private TextView same_region_textview;
    private RadioGroup radio_group;
    private Button request_button;
    private RadioButton myself_radio_button;
    private RequestGenerateToMainListener listener;
    private RelativeLayout rel_lay_region,rel_lay_button;
    private LinearLayout parent_lin_lay;
    private User user;
    private double latitude,longitude;
    private ProgressBar region_progress_bar;
    private AddressResultReceiver receiver;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE=1;

    public interface RequestGenerateToMainListener{
        User getCurrentUser();
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

        blood_group_spinner=(MySpinner)view.findViewById(R.id.spinner_blood_group_id);
        radio_group=(RadioGroup)view.findViewById(R.id.request_radio_group_id);
        region_edittext=(EditText)view.findViewById(R.id.region_edittext_id);
        age_edittext=(EditText)view.findViewById(R.id.age_edittext_id);
        region_input_lay=(TextInputLayout)view.findViewById(R.id.region_input_lay_id);
        age_input_lay=(TextInputLayout)view.findViewById(R.id.age_input_lay_id);
        same_region_textview=(TextView)view.findViewById(R.id.same_as_mine_textview_id);
        request_button=(Button)view.findViewById(R.id.request_button_id);
        myself_radio_button=(RadioButton)view.findViewById(R.id.myself_radio_button_id);
        note_input_lay=(TextInputLayout)view.findViewById(R.id.note_input_lay_id);
        note_edittext=(EditText)view.findViewById(R.id.note_edittext_id);
        rel_lay_region=(RelativeLayout)view.findViewById(R.id.rel_lay_region_id);
        rel_lay_button=(RelativeLayout)view.findViewById(R.id.rel_lay_button_id);
        parent_lin_lay=(LinearLayout)view.findViewById(R.id.parent_lin_lay_id);
        region_progress_bar=(ProgressBar)view.findViewById(R.id.region_progress_bar_id);

        if(listener!=null)
            user=listener.getCurrentUser();

        myself_radio_button.setChecked(true);
        hideViews();

        List<String> blood_groups= Arrays.asList(getResources().getStringArray(R.array.blood_group_entries));
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
                        hideViews();
                        break;

                    case R.id.someone_else_radio_button_id:
                        showViews();
                        break;
                }
            }
        });

        region_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Intent intent=new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                    startActivityForResult(intent,PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        same_region_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                region_edittext.setText(user.getAddress());
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
                            generateBloodRequest(user.getAccess_token(),user.getAge(),user.getLatitude(),user.getLongitude(),user.getAddress(),user.getBlood_group(),note_edittext.getText().toString());
                        }
                        break;

                    case R.id.someone_else_radio_button_id:
                        if(user!=null)
                        {
                            int age,blood_group;
                            String address;
                            blood_group=blood_group_spinner.getSelectedItemPosition();
                            address=region_edittext.getText().toString();
                            if(blood_group!=0)
                            {
                                if(!address.isEmpty())
                                {
                                    if(!age_edittext.getText().toString().isEmpty())
                                    {
                                        age=Integer.parseInt(age_edittext.getText().toString());
                                        removeErrorFromInputLayout(age_input_lay);
                                        removeErrorFromInputLayout(region_input_lay);
                                        generateBloodRequest(Integer.toString(user.getId()),age,latitude,longitude,address,blood_group,note_edittext.getText().toString());
                                    }else
                                    {
                                        setErrorOnInputLayout(age_input_lay,"Please enter age.");
                                        removeErrorFromInputLayout(region_input_lay);
                                    }
                                }else
                                {
                                    removeErrorFromInputLayout(age_input_lay);
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

    private void generateBloodRequest(String id,int age,double latitude,double longitude,String address,int bgroup,String note)
    {
        String url=MyConstants.BASE_URL_API+"blood_requests/";
        JSONObject blood_req_json=new JSONObject();
        JSONObject json=new JSONObject();
        try
        {
            blood_req_json.put("id",id);
            blood_req_json.put("age",age);
            blood_req_json.put("latitude",latitude);
            blood_req_json.put("longitude",longitude);
            blood_req_json.put("address",address);
            blood_req_json.put("bgroup",bgroup);
            blood_req_json.put("request_type",bgroup);
            blood_req_json.put("note",note);
            json.put("blood_request",blood_req_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("yo",response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("yo",error.getMessage()+"\n"+error.getLocalizedMessage()+"\n"+error.toString());
                error.printStackTrace();
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
                Log.v("yo","code is "+response.statusCode);
                return super.parseNetworkResponse(response);
            }
        };

        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"blood_request");
        Toast.makeText(getActivity(), "fired", Toast.LENGTH_SHORT).show();

    }

    private void hideViews() {
        blood_group_spinner.setVisibility(View.INVISIBLE);
        region_input_lay.setVisibility(View.INVISIBLE);
        same_region_textview.setVisibility(View.INVISIBLE);
        age_input_lay.setVisibility(View.INVISIBLE);
        ((ViewGroup)blood_group_spinner.getParent()).removeView(blood_group_spinner);
        ((ViewGroup)rel_lay_region.getParent()).removeView(rel_lay_region);
        ((ViewGroup)age_input_lay.getParent()).removeView(age_input_lay);
    }

    private void showViews()
    {
        blood_group_spinner.setVisibility(View.VISIBLE);
        region_input_lay.setVisibility(View.VISIBLE);
        same_region_textview.setVisibility(View.VISIBLE);
        age_input_lay.setVisibility(View.VISIBLE);
        ((ViewGroup)rel_lay_button.getParent()).removeView(rel_lay_button);
        ((ViewGroup)note_input_lay.getParent()).removeView(note_input_lay);
        parent_lin_lay.addView(blood_group_spinner);
        parent_lin_lay.addView(rel_lay_region);
        parent_lin_lay.addView(age_input_lay);
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
            region_edittext.setText(resultData.getString(MyConstants.RESULT_DATA_KEY));
        }
    }

}