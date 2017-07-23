package com.example.sosblood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.activities.DonorDetailActivity;
import com.example.sosblood.models.Donor;
import com.example.sosblood.others.MyApplication;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.utils.DonorCardAdapter;
import com.example.sosblood.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class RequestStatusFragment extends Fragment {

    private RecyclerView donor_recycler_view;
    private String request_id,access_token;
    private TextView request_info_textview,no_response_textview;
    private ProgressBar request_info_progress_bar,responses_progress_bar;
    private List<Donor> donors;

    public RequestStatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_request_status, container, false);

        donor_recycler_view =(RecyclerView) view.findViewById(R.id.request_status_recycler_view_id);
        request_info_textview=(TextView)view.findViewById(R.id.request_info_textview_id);
        request_info_progress_bar=(ProgressBar)view.findViewById(R.id.request_info_progress_bar_id);
        responses_progress_bar=(ProgressBar)view.findViewById(R.id.responses_progress_bar_id);
        no_response_textview=(TextView)view.findViewById(R.id.no_response_textview_id);

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        access_token=shared_prefs.getString("access_token",null);

        SharedPreferences shared_prefs_1=getActivity().getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        request_id=shared_prefs_1.getString("request_id",null);
        if(request_id!=null && access_token!=null)
        {
            populateRequestInfo();
            populateResponses();
        }else
        {
            Toast.makeText(getActivity(), "Error while receiving responses to your Blood request.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void populateRequestInfo()
    {
        String url=MyConstants.BASE_URL_API+"blood_requests/"+request_id;

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try
                {
                    JSONObject obj=response.getJSONObject("blood_request");
                    String blood_group,city;
                    blood_group=((MyApplication)getActivity().getApplication()).getBloodGroups().get(obj.getInt("bgroup"));
                    city=obj.getString("city");
                    request_info_textview.setText("You requested for blood group "+blood_group+" in "+city);
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

                try
                {
                    JSONArray array=response.getJSONArray("blood_requests");
                    donors= JSONParser.fetchDonors(array);
                    responses_progress_bar.setVisibility(View.INVISIBLE);

                    if(donors.isEmpty())
                        no_response_textview.setVisibility(View.VISIBLE);
                    else
                        setupRecycler();
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
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"request_status");
    }

    private void setupRecycler() {
        DonorCardAdapter adapter=new DonorCardAdapter(donors,getActivity());
        donor_recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        donor_recycler_view.setLayoutManager(manager);

        adapter.setListener(new DonorCardAdapter.DonorToHomeListener() {
            @Override
            public void onClick(int position) {
                openDonorDetailsActivity(position);
            }
        });
    }

    private void openDonorDetailsActivity(int position) {
        Bundle bundle=new Bundle();
        bundle.putString("donor_id",donors.get(position).getId());
        bundle.putString("access_token",access_token);
        Intent intent=new Intent(getActivity(), DonorDetailActivity.class);
        intent.putExtra("donor",bundle);
        startActivity(intent);
    }

    private void buildDonorDetailDialog(final int position) {
        View view=getActivity().getLayoutInflater().inflate(R.layout.donor_detail_dialog,null,false);
        Button call_button=(Button)view.findViewById(R.id.call_button_id);
        TextView name,phone,note;
        name=(TextView)view.findViewById(R.id.name_textview_id);
        phone=(TextView)view.findViewById(R.id.phone_textview_id);
        note=(TextView)view.findViewById(R.id.note_textview_id);
        final ImageView pic=(ImageView)view.findViewById(R.id.image_view_id);
        final ProgressBar dialog_progress_bar=(ProgressBar)view.findViewById(R.id.progress_bar_id);

        ImageRequest request=new ImageRequest(donors.get(position).getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                dialog_progress_bar.setVisibility(View.INVISIBLE);
                pic.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"request");

        name.setText(donors.get(position).getFirst_name()+" "+donors.get(position).getLast_name());
        phone.setText(donors.get(position).getPhone());
        note.setText(donors.get(position).getNote());

        call_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+donors.get(position).getPhone()));
                startActivity(intent);
            }
        });
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    @Override
    public void onStop() {
        super.onStop();
        MySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("request_status");
    }
}