package com.example.sosblood.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sosblood.R;
import com.example.sosblood.widgets.MyTextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private String closed_text;
    private Context context;

    public CustomSpinnerAdapter(Context context, List<String> items, String closed_text) {
        super(context, android.R.layout.simple_spinner_dropdown_item, items);
        this.closed_text=closed_text;
        this.context=context;
    }



    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            return initialSelection(true);
        }
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            return initialSelection(false);
        }
        return getCustomView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return super.getCount() + 1; // Adjust for initial selection item
    }

    private View initialSelection(boolean dropdown) {
        // Just an example using a simple TextView. Create whatever default view
        // to suit your needs, inflating a separate layout if it's cleaner.
        MyTextView view = new MyTextView(getContext());
        view.setText(closed_text);
        int spacing = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_smaller);
        view.setPadding(0, spacing, 0, spacing);

        if (dropdown) { // Hidden when the dropdown is opened
            view.setHeight(0);
        }

        return view;
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        // Distinguish "real" spinner items (that can be reused) from initial selection item
        View row = convertView != null && !(convertView instanceof TextView)
                ? convertView :
                LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        position = position - 1; // Adjust for initial selection item
        String item = getItem(position);

        // ... Resolve views & populate with data ...

        ((TextView)row).setText(item);

        return row;
    }
}