package com.shivam.sosblood.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shivam.sosblood.R;
import com.shivam.sosblood.models.SOSNotification;
import com.shivam.sosblood.others.MyConstants;
import com.shivam.sosblood.others.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.ViewHolder> {

    private List<SOSNotification> notifications;
    private Context context;
    private String access_token;
    private NotificationToHomeListener listener;

    public void setListener(NotificationToHomeListener listener)
    {
        this.listener=listener;
    }

    public interface NotificationToHomeListener{
        void onClick(int position);
    }

    public NotificationCardAdapter(List<SOSNotification> notifications, Context context, String access_token) {
        this.notifications=notifications;
        this.context=context;
        this.access_token=access_token;
    }

    @Override
    public NotificationCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv=(CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        SOSNotification notification=notifications.get(position);
        CardView card=holder.card_view;

        ImageView pic;
        TextView content;
        ProgressBar image_progress_bar;
        LinearLayout root_lin_lay;

        pic=(ImageView)card.findViewById(R.id.image_view_id);
        content=(TextView)card.findViewById(R.id.content_textview_id);
        image_progress_bar=(ProgressBar)card.findViewById(R.id.image_progress_bar_id);
        root_lin_lay=(LinearLayout)card.findViewById(R.id.root_lin_lay_id);

        if(!notification.isClicked())
        {
            root_lin_lay.setBackgroundColor(Color.parseColor("#eaa6a6"));
        }

        switch(notification.getType())
        {
            case "request_accepted":
            {
                callResponseDetailApi(position,pic,content,image_progress_bar);
                break;
            }
            case "ginapp":
            {
                populateWithTitle(position,content,pic,image_progress_bar);
                break;
            }
        }

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                    listener.onClick(position);
            }
        });
    }

    private void populateWithTitle(int position,TextView content,ImageView pic,ProgressBar image_progress_bar) {
        content.setText(notifications.get(position).getTitle());
        pic.setImageDrawable(content.getResources().getDrawable(R.drawable.logo));
        image_progress_bar.setVisibility(View.INVISIBLE);
    }

    private void callResponseDetailApi(int position, final ImageView pic, final TextView content, final ProgressBar image_progress_bar) {
        String url= MyConstants.BASE_URL_API+"blood_request_responses/"+notifications.get(position).getId();

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    fetchImage(response.getJSONObject("blood_request_response").getString("picture"),pic,image_progress_bar);
                    content.setText(response.getJSONObject("blood_request_response").getString("first_name")+" accepted your blood request.");
                }catch(JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
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
        MySingleton.getInstance(context).addToRequestQueue(request,"notification_card");
    }

    private void fetchImage(String url, final ImageView pic, final ProgressBar image_progress_bar) {
        ImageRequest request=new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                image_progress_bar.setVisibility(View.INVISIBLE);
                pic.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                image_progress_bar.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "Image not fetched", Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(request,"image_notification");
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        private CardView card_view;
        ViewHolder(CardView itemView) {
            super(itemView);
            card_view=itemView;
        }
    }

}
