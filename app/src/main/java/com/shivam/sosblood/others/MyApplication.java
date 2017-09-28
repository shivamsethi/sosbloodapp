package com.shivam.sosblood.others;

import android.app.Application;
import android.util.SparseArray;

public class MyApplication extends Application {
    private SparseArray<String> blood_groups;

    public MyApplication() {
        blood_groups=new SparseArray<>();
        blood_groups.put(1,"O+");
        blood_groups.put(2,"O-");
        blood_groups.put(3,"A+");
        blood_groups.put(4,"A-");
        blood_groups.put(5,"B+");
        blood_groups.put(6,"B-");
        blood_groups.put(7,"AB+");
        blood_groups.put(8,"AB-");
    }

    public SparseArray<String> getBloodGroups()
    {
        return blood_groups;
    }
}
