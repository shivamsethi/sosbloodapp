package com.example.sosblood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.example.sosblood.utils.JSONParser;
import com.example.sosblood.utils.NeedyCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView no_requests_textview;
    private RecyclerView needy_recycler_view;
    private HomeToMainListener listener;
    private User user;
    private ProgressBar recycler_progress_bar;
    private List<NeedyPerson> needy_persons;
    private ImageView invite_imageview;
    private SwipeRefreshLayout swipe_refresh_layout;

    public interface HomeToMainListener{
        User getCurrentUserinHome();
    }

    public HomeFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener=(HomeToMainListener)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_home, container, false);

        no_requests_textview=(TextView)view.findViewById(R.id.no_requests_textview_id);
        needy_recycler_view=(RecyclerView)view.findViewById(R.id.home_recycler_view_id);
        recycler_progress_bar=(ProgressBar)view.findViewById(R.id.recycler_progress_bar_id);
        invite_imageview=(ImageView)view.findViewById(R.id.image_view_id);
        swipe_refresh_layout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout_id);

        if(listener!=null)
            user=listener.getCurrentUserinHome();

        fetchNearNeedyPeople();

        invite_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Invite friends", Toast.LENGTH_SHORT).show();
            }
        });

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNearNeedyPeople();
            }
        });

        return view;
    }

    private void fetchNearNeedyPeople() {

        String url= MyConstants.BASE_URL_API+"blood_requests/find_by_latlng";

        JSONObject blood_req_json=new JSONObject();
        JSONObject json=new JSONObject();
        try
        {
            blood_req_json.put("latitude",user.getLatitude());
            blood_req_json.put("longitude",user.getLongitude());
            json.put("blood_request",blood_req_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url,json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try
                {
                    JSONArray array=response.getJSONArray("blood_requests");
                    needy_persons=JSONParser.fetchNeedyPersons(array,getActivity());
                    recycler_progress_bar.setVisibility(View.INVISIBLE);

                    if(needy_persons.isEmpty())
                        no_requests_textview.setVisibility(View.VISIBLE);
                    else
                        setupRecycler();

                    swipe_refresh_layout.setRefreshing(false);
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
                headers.put("Authorization",user.getAccess_token());
                return headers;
            }
        };

        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"home");
    }

    private void setupRecycler() {
        NeedyCardAdapter adapter=new NeedyCardAdapter(needy_persons,getActivity());
        needy_recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        needy_recycler_view.setLayoutManager(manager);

        adapter.setListener(new NeedyCardAdapter.NeedyToHomeListener() {
            @Override
            public void onClick(int position) {
                Bundle bundle=new Bundle();
                bundle.putSerializable("needy_person",needy_persons.get(position));
                bundle.putSerializable("user",user);
                Intent intent=new Intent(getActivity(), NeedyDetailActivity.class);
                intent.putExtra("needy_person",bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        MySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("home");
    }
}