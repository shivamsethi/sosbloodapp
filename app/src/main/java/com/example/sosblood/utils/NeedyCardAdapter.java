package com.example.sosblood.utils;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sosblood.R;

import java.util.List;

public class NeedyCardAdapter extends RecyclerView.Adapter<NeedyCardAdapter.ViewHolder> {

    private List<Integer> image_ids;
    private List<String> names,cities,blood_groups;
    private NeedyToHomeListener listener;

    public NeedyCardAdapter(List<Integer> image_ids, List<String> names, List<String> cities, List<String> blood_groups) {
        this.image_ids = image_ids;
        this.names = names;
        this.cities = cities;
        this.blood_groups = blood_groups;
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
        CardView card=holder.card_view;
        ImageView pic;
        TextView name,city,blood_group;
        pic=(ImageView)card.findViewById(R.id.pic_image_view_id);
        name=(TextView)card.findViewById(R.id.name_textview_id);
        city=(TextView)card.findViewById(R.id.city_textview_id);
        blood_group=(TextView)card.findViewById(R.id.blood_group_textview_id);
        Drawable drawable=card.getResources().getDrawable(image_ids.get(position));
        pic.setImageDrawable(drawable);
        name.setText(names.get(position));
        city.setText(cities.get(position));
        blood_group.setText(blood_groups.get(position));

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
        return names.size();
    }
}