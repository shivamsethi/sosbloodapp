package com.example.sosblood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sosblood.R;
import com.example.sosblood.activities.DonateFilterActivity;
import com.example.sosblood.others.MySpinner;
import com.example.sosblood.utils.CustomSpinnerAdapter;

import java.util.Arrays;
import java.util.List;

public class DonateFragment extends Fragment {

    private LinearLayout filter_lin_lay;
    private MySpinner sort_spinner;

    public DonateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_donate, container, false);

        filter_lin_lay=(LinearLayout)view.findViewById(R.id.filter_lin_lay_id);
        sort_spinner =(MySpinner)view.findViewById(R.id.sort_spinner_id);

        List<String> sort_entries= Arrays.asList(getResources().getStringArray(R.array.sorting_donor_entries));
        CustomSpinnerAdapter adapter=new CustomSpinnerAdapter(getActivity(),sort_entries,"Sort by");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_spinner.setAdapter(adapter);
        sort_spinner.setPrompt("Sort by");

        filter_lin_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DonateFilterActivity.class));
            }
        });

        SpinnerInteractionListener spinner_listener=new SpinnerInteractionListener();
        sort_spinner.setOnItemSelectedListener(spinner_listener);
        sort_spinner.setOnTouchListener(spinner_listener);

        return view;
    }



    public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener,View.OnTouchListener {

        boolean user_selected=false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            user_selected=true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(user_selected)
            {
                Toast.makeText(getActivity(), ""+i, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}