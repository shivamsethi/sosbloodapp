package com.shivam.sosblood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shivam.sosblood.R;
import com.shivam.sosblood.activities.DonorDetailActivity;
import com.shivam.sosblood.activities.WebViewActivity;
import com.shivam.sosblood.models.SOSNotification;
import com.shivam.sosblood.others.MyConstants;
import com.shivam.sosblood.utils.NotificationCardAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class NotificationFragment extends Fragment {

    private List<SOSNotification> notifications;
    private String access_token;
    private RecyclerView recycler_view;
    private ProgressBar progress_bar;
    private TextView no_notification_textview;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_notification, container, false);

        notifications=new ArrayList<>();

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        access_token=shared_prefs.getString("access_token",null);

        recycler_view=(RecyclerView)view.findViewById(R.id.recycler_view_id);
        progress_bar=(ProgressBar)view.findViewById(R.id.progress_bar_id);
        no_notification_textview=(TextView)view.findViewById(R.id.no_notification_textview_id);

        loadNotifications();

        if(notifications.isEmpty())
            no_notification_textview.setVisibility(View.VISIBLE);
        else
            setupRecycler();

        progress_bar.setVisibility(View.INVISIBLE);

        return view;
    }

    private void setupRecycler() {
        final NotificationCardAdapter adapter=new NotificationCardAdapter(notifications,getActivity(),access_token);
        recycler_view.setAdapter(adapter);

        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(manager);

        adapter.setListener(new NotificationCardAdapter.NotificationToHomeListener() {
            @Override
            public void onClick(int position) {
                switch (notifications.get(position).getType())
                {
                    case "request_accepted":
                    {
                        markNotificationClicked(position);
                        adapter.notifyDataSetChanged();
                        Bundle bundle=new Bundle();
                        bundle.putString("donor_id",notifications.get(position).getId());
                        bundle.putString("access_token",access_token);
                        Intent intent=new Intent(getActivity(), DonorDetailActivity.class);
                        intent.putExtra("donor",bundle);
                        startActivity(intent);
                        break;
                    }
                    case "ginapp":
                    {
                        markNotificationClicked(position);
                        Intent intent1=new Intent(getActivity(),WebViewActivity.class);
                        intent1.putExtra("url","https://www.goo.gl/"+notifications.get(position).getId());
                        startActivity(intent1);
                        break;
                    }
                }
            }
        });
    }

    private void markNotificationClicked(int position) {
        SharedPreferences shared_prefs=getActivity().getSharedPreferences(MyConstants.SHARED_PREFS_NOTIFICATIONS_KEY,MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        editor.putBoolean("notification_clicked_"+position,true);
        editor.apply();
        notifications.get(position).setClicked(true);
    }

    private void loadNotifications() {
        SharedPreferences shared_prefs=getActivity().getSharedPreferences(MyConstants.SHARED_PREFS_NOTIFICATIONS_KEY,MODE_PRIVATE);
        int notifications_array_length=shared_prefs.getInt("notifications_array_length",0);
        SOSNotification notification;
        for(int i=notifications_array_length-1;i>=0;i--)
        {
            notification=new SOSNotification();
            notification.setId(shared_prefs.getString("id_"+i,null));
            notification.setType(shared_prefs.getString("notification_type_"+i,null));
            notification.setClicked(shared_prefs.getBoolean("notification_clicked_"+i,false));
            notification.setTitle(shared_prefs.getString("title_"+i,""));
            notifications.add(notification);
         }
    }
}