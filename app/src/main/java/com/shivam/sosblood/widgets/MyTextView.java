package com.shivam.sosblood.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class MyTextView extends android.support.v7.widget.AppCompatTextView {

    public MyTextView(Context context) {
        super(context);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTypeface(Typeface.createFromAsset(context.getAssets(),"Roboto-Regular.ttf"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
