package com.example.sosblood.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sosblood.R;
import com.example.sosblood.utils.NeedyCardAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView invite_textview;
    private RecyclerView needy_recycler_view;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_home, container, false);

        invite_textview=(TextView)view.findViewById(R.id.invite_textview_id);
        needy_recycler_view=(RecyclerView)view.findViewById(R.id.home_recycler_view_id);

        final List<String> names=new ArrayList<>(),cities=new ArrayList<>(),blood_groups=new ArrayList<>();
        List<Integer> image_ids=new ArrayList<>();

        for(int i=0;i<10;i++)
        {
            names.add("Shivam Sethi");
            cities.add("Jhansi, UP");
            blood_groups.add("AB+");
            image_ids.add(R.drawable.insta);
        }

        NeedyCardAdapter adapter=new NeedyCardAdapter(image_ids,names,cities,blood_groups);
        needy_recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        needy_recycler_view.setLayoutManager(manager);

        adapter.setListener(new NeedyCardAdapter.NeedyToHomeListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(getActivity(),names.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        invite_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Invite friends", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}