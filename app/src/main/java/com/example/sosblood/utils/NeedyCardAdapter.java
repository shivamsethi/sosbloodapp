package com.example.sosblood.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.sosblood.R;
import com.example.sosblood.models.NeedyPerson;
import com.example.sosblood.others.MyApplication;
import com.example.sosblood.others.MySingleton;

import java.util.List;

public class NeedyCardAdapter extends RecyclerView.Adapter<NeedyCardAdapter.ViewHolder> {

    private Context context;
    private List<NeedyPerson> needy_persons;
    private NeedyToHomeListener listener;
    private Activity activity;

    public NeedyCardAdapter(List<NeedyPerson> needy_persons,Context context) {
        this.needy_persons=needy_persons;
        this.context=context;
        activity=(Activity)context;
    }

    public interface NeedyToHomeListener
    {
        public void onClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        private CardView card_view;
        ViewHolder(CardView itemView) {
            super(itemView);
            card_view=itemView;
        }
    }

    public void setListener(NeedyToHomeListener listener)
    {
        this.listener=listener;
    }

    @Override
    public NeedyCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv=(CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.needy_card,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(NeedyCardAdapter.ViewHolder holder, final int position) {
        NeedyPerson needy_person=needy_persons.get(position);
        CardView card=holder.card_view;
        final ImageView pic;
        TextView name,city,blood_group;
        final ProgressBar progress_bar;
        name=(TextView)card.findViewById(R.id.name_textview_id);
        city=(TextView)card.findViewById(R.id.city_textview_id);
        blood_group=(TextView)card.findViewById(R.id.blood_group_textview_id);
        pic=(ImageView)card.findViewById(R.id.pic_image_view_id);
        progress_bar=(ProgressBar)card.findViewById(R.id.progress_bar_id);

        ImageRequest request=new ImageRequest(needy_person.getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                progress_bar.setVisibility(View.INVISIBLE);
                pic.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(context).addToRequestQueue(request,"home");

        name.setText(needy_person.getFirst_name()+" "+needy_person.getLast_name());
        city.setText(needy_person.getAddress());
        blood_group.setText(((MyApplication)activity.getApplication()).getBloodGroups().get(needy_person.getBlood_group()));

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                    listener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return needy_persons.size();
    }
}