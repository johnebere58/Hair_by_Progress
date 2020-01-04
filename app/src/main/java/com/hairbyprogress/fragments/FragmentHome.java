package com.hairbyprogress.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.hairbyprogress.CustomActivity;
import com.hairbyprogress.MainActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.R;
import com.hairbyprogress.adapters.FragHomeAdapter;
import com.hairbyprogress.adapters.VpTopAdapter;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.base.BaseModel;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hairbyprogress.base.Base.HOME_DISPLAY_EXCLUSIVE;
import static com.hairbyprogress.base.Base.REMOVED_IDS;
import static com.hairbyprogress.base.Base.TYPE;

public class FragmentHome extends BaseFragment {

    MainActivity mainActivity;
    @BindView(R.id.ex_holder)View ex_holder;

    @BindView(R.id.loading_layout)View loading_layout;
    @BindView(R.id.top_vp)ViewPager top_vp;
    @BindView(R.id.top_vp_tab)SmartTabLayout top_vp_tab;

    @BindView(R.id.colors_rsv)RecyclerView colors_rsv;
    @BindView(R.id.types_rsv)RecyclerView types_rsv;
    @BindView(R.id.tools_rsv)RecyclerView tools_rsv;
    @BindView(R.id.exclusive_rsv)RecyclerView exclusive_rsv;

    @BindView(R.id.custom_but)View custom_but;
    @BindView(R.id.colors_holder)View colors_holder;
    @BindView(R.id.types_holder)View types_holder;
    @BindView(R.id.tools_holder)View tools_holder;

    VpTopAdapter vpTopAdapter;
    FragHomeAdapter fragHomeAdapterColors;
    FragHomeAdapter fragHomeAdapterTypes;
    FragHomeAdapter fragHomeAdapterTools;
    FragHomeAdapter fragHomeAdapterEx;

    ArrayList<BaseModel> topList = new ArrayList<>();
    ArrayList<BaseModel> colorList = new ArrayList<>();
    ArrayList<BaseModel> typesList = new ArrayList<>();
    ArrayList<BaseModel> toolsList = new ArrayList<>();
    ArrayList<BaseModel> ExList = new ArrayList<>();

    DisplayReceiver displayReceiver;
    ExReceiver exReceiver;


    boolean stop;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) activity;

        displayReceiver = new DisplayReceiver();
        exReceiver = new ExReceiver();
        activity.registerReceiver(displayReceiver,new IntentFilter(Base.BROADCAST_HOME_DISPLAY));
        activity.registerReceiver(exReceiver,new IntentFilter(Base.BROADCAST_PRODUCTS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            activity.unregisterReceiver(displayReceiver);
        }catch (Exception ignored){};
        try{
            activity.unregisterReceiver(exReceiver);
        }catch (Exception ignored){};

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_home, container, false);
        ButterKnife.bind(this,rootView);

        custom_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, CustomActivity.class));
            }
        });

        vpTopAdapter = new VpTopAdapter(context,topList);
        fragHomeAdapterColors = new FragHomeAdapter(activity,colorList);
        fragHomeAdapterTypes = new FragHomeAdapter(activity,typesList);
        fragHomeAdapterTools = new FragHomeAdapter(activity,toolsList);
        fragHomeAdapterEx = new FragHomeAdapter(activity,ExList);

        vpTopAdapter.setOnComplete(new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                top_vp_tab.setViewPager(top_vp);
            }
        });

        colors_rsv.setLayoutManager(new GridLayoutManager(context,3));
        colors_rsv.setNestedScrollingEnabled(false);
        colors_rsv.setAdapter(fragHomeAdapterColors);

        types_rsv.setLayoutManager(new GridLayoutManager(context,3));
        types_rsv.setNestedScrollingEnabled(false);
        types_rsv.setAdapter(fragHomeAdapterTypes);

        tools_rsv.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        tools_rsv.setNestedScrollingEnabled(false);
        tools_rsv.setAdapter(fragHomeAdapterTools);

        exclusive_rsv.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        exclusive_rsv.setNestedScrollingEnabled(false);
        exclusive_rsv.setAdapter(fragHomeAdapterEx);



        top_vp.setAdapter(vpTopAdapter);

        top_vp_tab.setViewPager(top_vp);

        refreshItems(null);
        refreshExs(null);

        handleVp();

        top_vp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                stop = true;
                return false;
            }
        });

        return rootView;
    }

    private void refreshItems(ArrayList<String> itemsRemoved){
        if(MyApplication.home_display.isEmpty())return;

        if(itemsRemoved!=null && !itemsRemoved.isEmpty()){
            for(String id:itemsRemoved){
                mainActivity.removeFromList(topList,id);
                mainActivity.removeFromList(colorList,id);
                mainActivity.removeFromList(typesList,id);
                mainActivity.removeFromList(toolsList,id);
            }
            notifyAllAdapters();
            return;
        }

        for(BaseModel model: MyApplication.home_display){
            if(model.getType()==Base.HOME_DISPLAY_TOP) {
                mainActivity.addOnceToList(topList, model);
            }
            if(model.getType()==Base.HOME_DISPLAY_COLORS) {
                mainActivity.addOnceToList(colorList, model);
            }
            if(model.getType()==Base.HOME_DISPLAY_HAIR_TYPES) {
                mainActivity.addOnceToList(typesList, model);
            }
            if(model.getType()==Base.HOME_DISPLAY_HAIR_TOOLS) {
                mainActivity.addOnceToList(toolsList, model);
            }
        }

        notifyAllAdapters();
        loading_layout.setVisibility(View.GONE);

        colors_holder.setVisibility(colorList.isEmpty()?View.GONE:View.VISIBLE);
        types_holder.setVisibility(typesList.isEmpty()?View.GONE:View.VISIBLE);
        tools_holder.setVisibility(toolsList.isEmpty()?View.GONE:View.VISIBLE);
    }

    private void handleVp(){
        if(stop)return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int p = top_vp.getCurrentItem();
                int size = vpTopAdapter.getCount();

                p++;
                p = p>=size?0:p;

                top_vp.setCurrentItem(p);

                //if(p==vpTopAdapter.getCount()-1)stop = true;

                handleVp();

            }
        },5000);
    }

    private void notifyAllAdapters(){
        vpTopAdapter.notifyDataSetChanged();
        top_vp_tab.setViewPager(top_vp);
        fragHomeAdapterColors.notifyDataSetChanged();
        fragHomeAdapterTools.notifyDataSetChanged();
        fragHomeAdapterTypes.notifyDataSetChanged();
        fragHomeAdapterEx.notifyDataSetChanged();
    }

    private void refreshExs(ArrayList<String> itemsRemoved){
        if(MyApplication.products.isEmpty())return;

        if(itemsRemoved!=null && !itemsRemoved.isEmpty()){
            for(String id:itemsRemoved){
                mainActivity.removeFromList(ExList,id);
            }
            fragHomeAdapterEx.notifyDataSetChanged();
            return;
        }

        boolean notifyEx=false;
        for(BaseModel model: MyApplication.products){
            if(model.getString(Base.ITEM_CATEGORY).equalsIgnoreCase(Base.EXCLUSIVE)) {
                model.put(TYPE,HOME_DISPLAY_EXCLUSIVE);
                mainActivity.addOnceToList(ExList, model);
                notifyEx = true;
            }

        }

        if(notifyEx){
            fragHomeAdapterEx.notifyDataSetChanged();
            ex_holder.setVisibility(View.VISIBLE);
        }

    }



    private class DisplayReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> removedId = intent.getStringArrayListExtra(REMOVED_IDS);
            refreshItems(removedId);
        }
    }

    private class ExReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> removedId = intent.getStringArrayListExtra(REMOVED_IDS);
            refreshExs(removedId);
        }
    }



}
