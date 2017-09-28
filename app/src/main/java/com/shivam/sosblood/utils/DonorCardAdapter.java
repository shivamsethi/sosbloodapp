package com.shivam.sosblood.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.shivam.sosblood.R;
import com.shivam.sosblood.models.Donor;
import com.shivam.sosblood.others.MySingleton;

import java.util.List;

public class DonorCardAdapter extends RecyclerView.Adapter<DonorCardAdapter.ViewHolder> {

    private Context context;
    private List<Donor> donors;
    private DonorToHomeListener listener;
    private Activity activity;

    public DonorCardAdapter(List<Donor> donors, Context context) {
        this.donors=donors;
        this.context=context;
        activity=(Activity)context;
    }

    public interface DonorToHomeListener
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

    public void setListener(DonorToHomeListener listener)
    {
        this.listener=listener;
    }

    @Override
    public DonorCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv=(CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.donor_card,parent,false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(DonorCardAdapter.ViewHolder holder, final int position) {
        Donor donor=donors.get(position);
        CardView card=holder.card_view;
        final ImageView pic;
        TextView name,date;
        final ProgressBar progress_bar;
        name=(TextView)card.findViewById(R.id.name_textview_id);
        pic=(ImageView)card.findViewById(R.id.pic_image_view_id);
        progress_bar=(ProgressBar)card.findViewById(R.id.progress_bar_id);
        date=(TextView)card.findViewById(R.id.date_textview_id);

        ImageRequest request=new ImageRequest(donor.getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                progress_bar.setVisibility(View.INVISIBLE);
                pic.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress_bar.setVisibility(View.INVISIBLE);
                pic.setImageBitmap(((BitmapDrawable)(context.getResources().getDrawable(R.drawable.user))).getBitmap());
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(request,"home");

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    CommonTasks.showImageDialog((Activity)context,((BitmapDrawable)(pic.getDrawable())).getBitmap());
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, "Image not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        name.setText(donor.getFirst_name()+" "+donor.getLast_name());
        date.setText(new DateHandler().getSimplifiedDate(donor.getResponse_creation_time()));

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
        return donors.size();
    }
}