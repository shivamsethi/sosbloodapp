package com.shivam.sosblood.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.shivam.sosblood.R;

public class WebViewActivity extends AppCompatActivity {

    private String url;
    private WebView web_view;
    private Toolbar toolbar;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        url=getIntent().getStringExtra("url");

        web_view=(WebView)findViewById(R.id.webview_id);
        toolbar=(Toolbar)findViewById(R.id.toolbar_id);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.cross));

        web_view.setWebViewClient(new MyWebViewClient());
        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.loadUrl(url);

        web_view.getSettings().setSupportZoom(true);
        web_view.getSettings().setBuiltInZoomControls(true);
        web_view.getSettings().setDisplayZoomControls(true);
    }

    private class MyWebViewClient extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if(progress_bar.getVisibility()==View.VISIBLE)
            {
                progress_bar.setVisibility(View.INVISIBLE);
                progress_bar=null;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if(progress_bar==null)
            {
                progress_bar=(ProgressBar)findViewById(R.id.progress_bar_id);
                progress_bar.setVisibility(View.VISIBLE);
            }
        }
    }
}