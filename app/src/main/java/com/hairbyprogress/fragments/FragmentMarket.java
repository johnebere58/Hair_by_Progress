package com.hairbyprogress.fragments;


import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hairbyprogress.MyApplication;
import com.hairbyprogress.R;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.base.BaseModel;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hairbyprogress.base.Base.BROADCAST_SETUP_MARKET;
import static com.hairbyprogress.base.Base.TITLE;


public class FragmentMarket extends BaseFragment {

    //String[] titles = {"HAIR EXTENSIONS","FRONTAL/CLOSURE","WIG BRAIDS","HAIR TOOLS"};

    private class MarketAdapter extends FragmentPagerAdapter {
        ArrayList<String> titles;

        public MarketAdapter(FragmentManager fm,ArrayList<String> titles) {
            super(fm);
            this.titles = titles;
        }

        @Override
        public android.app.Fragment getItem(int position) {
            FragmentMarketContent frg = new FragmentMarketContent();
            Bundle args = new Bundle();
            args.putString(TITLE,titles.get(position));
            frg.setArguments(args);
            return frg;
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return titles.get(position);
        }
    }

    @BindView(R.id.pager1)ViewPager viewPager;
    @BindView(R.id.viewpagertab)SmartTabLayout viewPagerTab;

    MarketAdapter marketAdapter;
    ArrayList<String> titles = new ArrayList<>();

    SetupReceiver setupReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupReceiver = new SetupReceiver();
        activity.registerReceiver(setupReceiver,new IntentFilter(BROADCAST_SETUP_MARKET));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.frag_market, container, false);
        ButterKnife.bind(this,rootView);

        marketAdapter = new MarketAdapter(getFragmentManager(),titles);
        viewPager.setAdapter(marketAdapter);

        viewPager.setOffscreenPageLimit(titles.size());
        viewPagerTab.setViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setUp();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            if(setupReceiver!=null){
                activity.unregisterReceiver(setupReceiver);
            }
        }catch (Exception ignored){};
    }

    private class SetupReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            setUp();
        }
    }

    private void setUp(){
        if( titles.isEmpty()){
            for(BaseModel bm: MyApplication.sections){
                if(bm.getString(TITLE).equals(Base.ITEM_CATEGORY)){
                    ArrayList<String> title = (ArrayList<String>) bm.getList(Base.CONTENT);
                    titles.addAll(title);
                    marketAdapter.notifyDataSetChanged();
                    viewPagerTab.setViewPager(viewPager);
                    break;
                }
            }
        }
    }
}
