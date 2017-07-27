package com.example.sosblood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView no_requests_textview;
    private RecyclerView needy_recycler_view;
    private HomeToMainListener listener;
    private User user;
    private ProgressBar recycler_progress_bar,image_progress_bar;
    private List<NeedyPerson> needy_persons;
    private ImageView invite_imageview;
    private SwipeRefreshLayout swipe_refresh_layout;
    private String share_message=null;

    public interface HomeToMainListener{
        User getCurrentUserInHome();
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
        image_progress_bar=(ProgressBar)view.findViewById(R.id.image_progress_bar_id);

        if(listener!=null)
            user=listener.getCurrentUserInHome();

        fetchImageAndMessage();

        fetchNearNeedyPeople();

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNearNeedyPeople();
                fetchImageAndMessage();
            }
        });

        return view;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void fetchImageAndMessage()
    {
        String url=MyConstants.BASE_URL_API+"home/banner";
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    fetchImage(response.getString("banner"));
                    share_message=response.getString("sharemessage");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                swipe_refresh_layout.setRefreshing(false);

                if(!CommonTasks.isNetworkAvailable(getActivity()))
                    Toast.makeText(getActivity(), "Network connectivity problem", Toast.LENGTH_SHORT).show();
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
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"home_image");
    }

    private void fetchImage(String url) {
        ImageRequest request=new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                image_progress_bar.setVisibility(View.INVISIBLE);
                invite_imageview.setImageBitmap(response);
                swipe_refresh_layout.setRefreshing(false);
                invite_imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(share_message!=null)
                        {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM,getImageUri(getActivity(),((BitmapDrawable)(invite_imageview.getDrawable())).getBitmap()));
                            intent.putExtra(Intent.EXTRA_TEXT,share_message);
                            intent.setType("*/*");
                            startActivity(Intent.createChooser(intent,"Select app..."));
                        }
                    }
                });
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                swipe_refresh_layout.setRefreshing(false);
                Log.v("yo",error.getLocalizedMessage()+"\n"+error.getMessage()+"\n"+error.toString());
            }
        });
        MySingleton.getInstance(getActivity()).addToRequestQueue(request,"home_image");
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
            json.put("num_posts",10);
            json.put("page_num",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url,json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("yo",response.toString());
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

    private void setupRecycler() {
        NeedyCardAdapter adapter=new NeedyCardAdapter(needy_persons,getActivity());
        needy_recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        needy_recycler_view.setLayoutManager(manager);

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
    public void onStop() {
        super.onStop();
        MySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("home");
    }
}