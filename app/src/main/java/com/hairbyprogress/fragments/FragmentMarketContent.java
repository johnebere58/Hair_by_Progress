package com.hairbyprogress.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hairbyprogress.MainActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.R;
import com.hairbyprogress.adapters.MarketAdapter;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hairbyprogress.base.Base.REMOVED_IDS;
import static com.hairbyprogress.base.Base.TITLE;

public class FragmentMarketContent extends BaseFragment {

        MainActivity mainActivity;
        @BindView(R.id.rsv_market)MyRecyclerView rsv_market;
        MarketAdapter marketAdapter;

        ProductReceiver productReceiver;
        
        ArrayList<BaseModel> mainList = new ArrayList<>();
        String title;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mainActivity = (MainActivity) activity;
            Bundle args = getArguments();
            title = args.getString(TITLE);

            productReceiver = new ProductReceiver();
            activity.registerReceiver(productReceiver,new IntentFilter(Base.BROADCAST_PRODUCTS));

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frag_market_content, container, false);
            ButterKnife.bind(this,rootView);

            marketAdapter = new MarketAdapter(activity,mainList);

            mainActivity.setUpRecycleView(rsv_market,new GridLayoutManager(context,3),null,null);

            rsv_market.setAdapter(marketAdapter);

            loadItem(null);

            return rootView;
        }

        private void loadItem(ArrayList<String> itemsRemoved){
            if(MyApplication.products.isEmpty())return;

            if(itemsRemoved!=null && !itemsRemoved.isEmpty()){
                for(String id:itemsRemoved){
                    mainActivity.removeFromList(mainList,id);
                }
                marketAdapter.notifyDataSetChanged();
                return;
            }

            boolean notify=false;
            for(BaseModel model: MyApplication.products){
                if(model.getString(Base.ITEM_CATEGORY).equalsIgnoreCase(title)) {
                    mainActivity.addOnceToList(mainList, model);
                    notify = true;
                }
            }

            if(notify){
                marketAdapter.notifyDataSetChanged();
            }
        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            activity.unregisterReceiver(productReceiver);
        }catch (Exception ignored){};
    }

    private class ProductReceiver extends BroadcastReceiver{

            @Override
            public void onReceive(Context context, Intent intent) {

                ArrayList<String> removedId = intent.getStringArrayListExtra(REMOVED_IDS);
                loadItem(removedId);
            }
        }
    }