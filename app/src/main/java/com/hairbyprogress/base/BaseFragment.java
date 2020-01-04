package com.hairbyprogress.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class BaseFragment extends Fragment {

    public Context context;
    public Activity activity;
    public BaseActivity baseActivity;
    protected static FirebaseFirestore db;

    @Override
    public void setRetainInstance(boolean retain) {
        super.setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        activity = getActivity();
        baseActivity = (BaseActivity)activity;
        db = FirebaseFirestore.getInstance();
    }

    public void Toast(String s){
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
