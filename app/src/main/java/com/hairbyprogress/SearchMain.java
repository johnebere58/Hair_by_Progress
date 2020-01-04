package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hairbyprogress.adapters.CartAdapter;
import com.hairbyprogress.adapters.MarketAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class SearchMain extends BaseActivity {

    @BindView(R.id.rsv) MyRecyclerView rsv;
    @BindView(R.id.title_tv)TextView title_tv;

    MarketAdapter marketAdapter;
    ArrayList<BaseModel> mainList = new ArrayList<>();

    String key;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_main);
        ButterKnife.bind(this);
        key = getIntent().getStringExtra(SEARCH_KEY);

        if(MyApplication.products.isEmpty()){
            Toast("No Items Yet");
            finish();
            return;
        }

        if(key==null || key.isEmpty()){
            Toast("No Key Set");
            finish();
            return;
        }

        title_tv.setText(key);

        setUpRecycleView(rsv,new GridLayoutManager(context,3),null,null);

        marketAdapter = new MarketAdapter(activity,mainList);
        rsv.setAdapter(marketAdapter);

        loadItems();

    }

    private void loadItems(){
        for(BaseModel bm:MyApplication.products){
            boolean add = canAdd(bm);
            if(add)mainList.add(bm);
        }


        if(mainList.isEmpty()){
            rsv.showEmpty(R.drawable.ic_cart,"No Items to Display","");
        }else{
            rsv.hideAllView();
        }

        marketAdapter.notifyDataSetChanged();
    }

    private boolean canAdd(BaseModel bm){
        if(bm.getString(ITEM_TAG).equalsIgnoreCase(key)){
            return true;
        }else{
            ArrayList<HashMap<String,Object>> map = (ArrayList<HashMap<String, Object>>) bm.getList(DESCRIPTION);
            for(HashMap<String,Object> m:map){
                if(m.containsValue(key))return true;
            }
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if(!MyApplication.alreadyStarted){
            startActivity(new Intent(context,MainActivity.class));
        }else{
            super.onBackPressed();
        }
    }
}
