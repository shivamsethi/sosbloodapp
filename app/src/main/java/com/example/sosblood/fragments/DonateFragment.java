package com.example.sosblood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sosblood.R;
import com.example.sosblood.activities.NeedyDetailActivity;
import com.example.sosblood.models.NeedyPerson;
import com.example.sosblood.models.User;
import com.example.sosblood.others.MyConstants;
import com.example.sosblood.others.MySingleton;
import com.example.sosblood.utils.CommonTasks;
import com.example.sosblood.utils.JSONParser;
import com.example.sosblood.utils.NeedyCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonateFragment extends Fragment {

    private RecyclerView recycler_view;
    private TextView no_requests_textview;
    private DonateToMainListener listener;
    private User user;
    private List<NeedyPerson> needy_persons;
    private ProgressBar progress_bar;
    private NeedyCardAdapter adapter;

    public interface DonateToMainListener{
        public User getCurrentUserInDonate();
    }

    public DonateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_donate, container, false);

        recycler_view=(RecyclerView)view.findViewById(R.id.recycler_view_id);
        no_requests_textview=(TextView)view.findViewById(R.id.no_requests_textview_id);
        progress_bar=(ProgressBar)view.findViewById(R.id.progress_bar_id);

        if(listener!=null)
            user=listener.getCurrentUserInDonate();

        setupRecycler();

        fetchNeedyPeople(1);

        return view;
    }

    private void setupRecycler() {
        needy_persons=new ArrayList<>();
        adapter=new NeedyCardAdapter(needy_persons,getActivity());
        recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(manager);

        adapter.setListener(new NeedyCardAdapter.NeedyToHomeListener() {
            @Override
            public void onClick(int position) {
                Bundle bundle=new Bundle();
                bundle.putString("request_id",needy_persons.get(position).getNeedy_id());
                bundle.putString("access_token",user.getAccess_token());
                Intent intent=new Intent(getActivity(), NeedyDetailActivity.class);
                intent.putExtra("needy_person",bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener=(DonateToMainListener)activity;
    }

    private void fetchNeedyPeople(final int page_num) {
        String url= MyConstants.BASE_URL_API+"blood_requests/find_by_latlng";

        JSONObject blood_req_json=new JSONObject();
        JSONObject json=new JSONObject();
        try
        {
            blood_req_json.put("latitude",user.getLatitude());
            blood_req_json.put("longitude",user.getLongitude());
            json.put("blood_request",blood_req_json);
            json.put("num_posts",1);
            json.put("page_num",page_num);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url,json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                List<NeedyPerson> needy_temp;

                try
                {
                    JSONArray array=response.getJSONArray("blood_requests");
                    needy_temp= JSONParser.fetchNeedyPersons(array,getActivity());
                    for(int i=0;i<needy_temp.size();i++)
                    {
                        needy_persons.add(needy_temp.get(i));
                    }
                    refreshListView();
                    if(needy_temp.size()<1) {
                        progress_bar.setVisibility(View.INVISIBLE);
                        if(page_num==1 && needy_temp.size()==0) {
                            no_requests_textview.setVisibility(View.VISIBLE);
                        }
                    }else{
                        fetchNeedyPeople(page_num+1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                headers.put("Authorization",user.getAccess_token());
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(20000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"home");
    }

    private void refreshListView() {
        adapter.notifyDataSetChanged();
        recycler_view.invalidate();
    }
}