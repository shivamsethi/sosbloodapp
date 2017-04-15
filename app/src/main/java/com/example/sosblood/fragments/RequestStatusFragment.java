package com.example.sosblood.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.others.MyApplication;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class RequestStatusFragment extends Fragment {

    private ListView notification_listview;
    private String request_id,access_token;
    private TextView request_info_textview;
    private ProgressBar request_info_progress_bar,responses_progress_bar;

    public RequestStatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_request_status, container, false);

        notification_listview=(ListView)view.findViewById(R.id.request_status_listview_id);
        request_info_textview=(TextView)view.findViewById(R.id.request_info_textview_id);
        request_info_progress_bar=(ProgressBar)view.findViewById(R.id.request_info_progress_bar_id);
        responses_progress_bar=(ProgressBar)view.findViewById(R.id.responses_progress_bar_id);

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        access_token=shared_prefs.getString("access_token",null);

        SharedPreferences shared_prefs_1=getActivity().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        request_id=shared_prefs_1.getString("request_id",null);
        if(request_id!=null && access_token!=null)
        {
            populateRequestInfo(request_id);
            populateResponses();
        }else
        {
            Toast.makeText(getActivity(), "Error while receiving responses to your Blood request.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void populateRequestInfo(String request_id) {
        String url=MyConstants.BASE_URL_API+"blood_requests/"+request_id;

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try
                {
                    JSONObject obj=response.getJSONObject("blood_request");
                    String blood_group,address;
                    blood_group=((MyApplication)getActivity().getApplication()).getBloodGroups().get(obj.getInt("bgroup"));
                    address=obj.getString("address");
                    request_info_textview.setText("You requested for blood group "+blood_group+" in "+address);
                    request_info_progress_bar.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.v("yo",error.getLocalizedMessage()+"\n"+error.getMessage()+"\n"+error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",access_token);
                return headers;
            }
        };
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"response");
    }

    private void populateResponses() {

        String url=MyConstants.BASE_URL_API+"blood_requests/"+request_id+"/responses";

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("yo","responses    "+response.toString());
                Toast.makeText(getActivity(),response.toString(), Toast.LENGTH_LONG).show();
                responses_progress_bar.setVisibility(View.INVISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.v("yo",error.getLocalizedMessage()+"\n"+error.getMessage()+"\n"+error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",access_token);
                return headers;
            }
        };
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"status");
    }
}