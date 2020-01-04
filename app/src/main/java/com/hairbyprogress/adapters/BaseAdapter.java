package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;


public class BaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    protected ArrayList<BaseModel> baseModels;
    protected Activity activity;
    protected BaseActivity mainActivity;
    protected LayoutInflater mInflater;
    private int mCount = 0;
    protected Context mContext = null;

    protected BaseAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        mContext = activity;
        this.baseModels = baseModels;
        this.activity= activity;
        mainActivity = (BaseActivity) activity;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }


    @Override
    public void onBindViewHolder(VH holder, int position) {

    }

    public void setCount(int count){
        mCount = count;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    public Object getItem(int position){
        return null;
    }

    public boolean isEmpty(){
        return mCount==0;
    }

    protected void Toast(Object s){
        Toast.makeText(activity, String.valueOf(s), Toast.LENGTH_SHORT).show();
    }
}


