package com.shivam.sosblood.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class MyButton extends android.support.v7.widget.AppCompatButton {
    public MyButton(Context context) {
        super(context);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }
}
