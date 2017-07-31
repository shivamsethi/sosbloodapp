package com.shivam.sosblood.others;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

public class DummyTabContent implements TabHost.TabContentFactory {
    private Context context;

    public DummyTabContent(Context context){
        this.context = context;
    }

    @Override
    public View createTabContent(String tag) {
        return new View(context);
    }
}
