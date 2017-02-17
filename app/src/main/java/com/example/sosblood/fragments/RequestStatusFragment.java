package com.example.sosblood.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.sosblood.R;

public class RequestStatusFragment extends Fragment {

    private ListView notification_listview;

    public RequestStatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_request_status, container, false);

        notification_listview=(ListView)view.findViewById(R.id.request_status_listview_id);

        return view;
    }

}
