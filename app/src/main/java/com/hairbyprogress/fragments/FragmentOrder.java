package com.hairbyprogress.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.MainActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.R;
import com.hairbyprogress.RateMain;
import com.hairbyprogress.adapters.FragNotifyAdapter;
import com.hairbyprogress.adapters.OrderAdapter;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.base.Base.ADDRESS;
import static com.hairbyprogress.base.Base.APPROVED;
import static com.hairbyprogress.base.Base.BROADCAST_NOTIFY;
import static com.hairbyprogress.base.Base.BROADCAST_ORDER_UPDATED;
import static com.hairbyprogress.base.Base.ITEMS_IN_CART;
import static com.hairbyprogress.base.Base.HAVE_RATED;
import static com.hairbyprogress.base.Base.NAME;
import static com.hairbyprogress.base.Base.NOTIFY_BASE;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE_FEEDBACK;
import static com.hairbyprogress.base.Base.ORDER_DELIVERED;
import static com.hairbyprogress.base.Base.ORDER_PENDING;
import static com.hairbyprogress.base.Base.PENDING;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.RATINGS;
import static com.hairbyprogress.base.Base.RATINGS_TEXT;
import static com.hairbyprogress.base.Base.STATUS;


public class FragmentOrder extends BaseFragment {

    MainActivity mainActivity;
    @BindView(R.id.vp_order)CustomViewPager vp_order;
    @BindView(R.id.lay)LinearLayout tab_layout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_order, container, false);
        ButterKnife.bind(this,rootView);

        for (int i = 0; i < tab_layout.getChildCount(); i++) {
            View view = tab_layout.getChildAt(i);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPosition(view);
                }
            });
        }

        vp_order.setOffscreenPageLimit(2);
        vp_order.setAdapter(new PageAdapter(mainActivity.getFragmentManager()));
        vp_order.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int p) {
                set_selection(p);
            }

        });



        return rootView;
    }




    public void set_selection(int position) {
        //position = position==0?0:position==1?2:4;
        for (int i = 0; i < tab_layout.getChildCount(); i++) {
            View v = tab_layout.getChildAt(i);
            if(i==position){
                v.setAlpha(1f);
            }else{
                v.setAlpha(.4f);
            }

        }
    }

    public void setPosition(View v) {
        String s = v.getTag().toString();
        vp_order.setCurrentItem(Integer.valueOf(s));
    }


    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            FragmentOrderContent fragments = new FragmentOrderContent();
            Bundle args = new Bundle();
            args.putInt(POSITION,position);
            fragments.setArguments(args);
            return fragments;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class FragmentOrderContent extends BaseFragment{

        MainActivity mainActivity;
        int position;
        @BindView(R.id.rsv_home)MyRecyclerView rsv_home;
        MyReceiver myReceiver;
        OrderAdapter orderAdapter;
        FragNotifyAdapter notifyAdapter;
        private BaseModel dummyRateModel;

        ArrayList<BaseModel> mainList = new ArrayList<>();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mainActivity = (MainActivity) activity;
            Bundle args = getArguments();
            position = args.getInt(POSITION);

            myReceiver = new MyReceiver();
            activity.registerReceiver(myReceiver,new IntentFilter(position==0?BROADCAST_ORDER_UPDATED:BROADCAST_NOTIFY));

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frag_home_content, container, false);
            ButterKnife.bind(this,rootView);

            mainActivity.setUpRecycleView(rsv_home, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false), null, null);

            if(position==0){
            orderAdapter = new OrderAdapter(activity, mainList);
            orderAdapter.setClickRate(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseModel model = (BaseModel) view.getTag();
                    dummyRateModel = model;
                    startActivityForResult(new Intent(context, RateMain.class),22);
                }
            });
            rsv_home.setAdapter(orderAdapter);

            loadItems();
            }

            if(position==1){
             notifyAdapter = new FragNotifyAdapter(activity,mainList);
             rsv_home.setAdapter(notifyAdapter);
             loadNotifications();
            }

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode!=RESULT_OK)return;

            if(requestCode==22){
                for(int i=0;i<mainList.size();i++){
                    BaseModel bm = mainList.get(i);
                    if(bm.getObjectId().equals(dummyRateModel.getObjectId())){
                        bm.put(HAVE_RATED,true);
                        bm.updateItem();
                        orderAdapter.notifyItemChanged(i);
                        break;
                    }
                }

                String ratingText = data.getStringExtra(RATINGS_TEXT);
                String ratings = data.getStringExtra(RATINGS);

                HashMap<String,Object> dInfo = (HashMap<String, Object>) dummyRateModel.getMap(ADDRESS);

                BaseModel model = new BaseModel();
                model.put(RATINGS_TEXT,ratingText);
                model.put(RATINGS,ratings);
                model.put(NOTIFY_TYPE,NOTIFY_TYPE_FEEDBACK);
                model.put(NAME,dInfo.get(NAME).toString());
                model.put(ITEMS_IN_CART,dummyRateModel.getList(ITEMS_IN_CART));
                model.put(STATUS,PENDING);
                model.saveItem(NOTIFY_BASE);

                new MaterialDialog.Builder(context)
                        .content("Thank you, your review has been submitted")
                        .positiveText("OK")
                        .show();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            try{
                if(myReceiver!=null){
                    activity.unregisterReceiver(myReceiver);
                }
            }catch (Exception ignored){};
        }

        private void loadItems(){

            if(MyApplication.userModel==null)return;

            for(BaseModel bm: MyApplication.order_items){
                if(isAdmin || bm.getUserId().equals(MyApplication.userModel.getUserId())){
                    mainActivity.addOnceToList(mainList,bm);
                }
            }

            orderAdapter.notifyDataSetChanged();

            if(!MyApplication.orderHasLoaded){
                rsv_home.showLoading();
            }
            else if (mainList.isEmpty()) {
                rsv_home.showEmpty(R.drawable.ic_deliver,"No Order Yet" , "You have not made any order");
            } else {
                rsv_home.hideAllView();
            }
        }

        private void loadNotifications(){

            //if(MyApplication.userModel==null)return;

            for(BaseModel bm: MyApplication.feedbackList){
                if(isAdmin || bm.getInt(STATUS)==APPROVED){
                    mainActivity.addOnceToList(mainList,bm);
                }
            }

            notifyAdapter.notifyDataSetChanged();

            if(!MyApplication.notificationHasLoaded){
                rsv_home.showLoading();
            }
            else if (mainList.isEmpty()) {
                rsv_home.showEmpty(R.drawable.ic_feeds,"No Feedback Yet" , "");
            } else {
                rsv_home.hideAllView();
            }
        }



        private class MyReceiver extends BroadcastReceiver{

            @Override
            public void onReceive(Context context, Intent intent) {
                if(position==0)loadItems();
                if(position==1)loadNotifications();
            }
        }

    }



}
