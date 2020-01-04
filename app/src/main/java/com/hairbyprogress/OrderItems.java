package com.hairbyprogress;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hairbyprogress.adapters.AddressAdapter;
import com.hairbyprogress.adapters.CartAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hairbyprogress.MyApplication.userModel;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class OrderItems extends BaseActivity {

    @BindView(R.id.rsv) MyRecyclerView rsv;
    @BindView(R.id.add_but) View add_but;
    @BindView(R.id.add_tv) TextView add_tv;

    CartAdapter cartAdapter;
    ArrayList<BaseModel> mainList = new ArrayList<>();
    ArrayList<String> selectedIds = new ArrayList<>();

    boolean forRating;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_order);
        ButterKnife.bind(this);

        add_but.setVisibility(View.GONE);

        forRating = getIntent().getBooleanExtra(FOR_RATING,false);

        cartAdapter = new CartAdapter(activity,mainList);
        cartAdapter.setViewMode(true);
        cartAdapter.setForRating(forRating);
        cartAdapter.setSelectionListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseModel bm = (BaseModel) view.getTag();
                selectedIds.add(bm.getObjectId());
                Toast("Added");
                add_tv.setText(String.format("ADD ITEM%s (%s)",selectedIds.size()>1?"S":"",selectedIds.size()));
                add_but.setVisibility(View.VISIBLE);
            }
        });

        rsv.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        rsv.setAdapter(cartAdapter);

        loadItems();

        add_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(forRating){
                    Intent intent = getIntent();
                    intent.putStringArrayListExtra(ITEMS_IN_CART,selectedIds);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
    }

    private void loadItems(){
        ArrayList<BaseModel> models = new ArrayList<>();

        for(BaseModel bm:MyApplication.cart_items){
            if(forRating){
                models.add(bm);
            }else if(MyApplication.dummyOrderIds!=null && MyApplication.dummyOrderIds.contains(bm.getObjectId())){
                models.add(bm);
            }
        }

        mainList.addAll(models);

        if(forRating) Collections.shuffle(mainList);

        cartAdapter.notifyDataSetChanged();

        if(mainList.isEmpty()){
            rsv.showEmpty(R.drawable.ic_cart,"No Item to Display","The items might have been deleted or removed");
        }
    }


}
