package com.example.sosblood.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.sosblood.R;
import com.example.sosblood.utils.CommonTasks;

public class DonateFilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_filter);


        Toolbar toolbar=(Toolbar)findViewById(R.id.filter_toolbar_id);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Filters");
        CommonTasks.setToolbarFont(toolbar,this);
    }

}
