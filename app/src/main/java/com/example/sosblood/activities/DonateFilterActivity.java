package com.example.sosblood.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.sosblood.R;

public class DonateFilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_filter);

        setSupportActionBar((Toolbar)findViewById(R.id.filter_toolbar_id));
        getSupportActionBar().setTitle("Filters");
    }

}
