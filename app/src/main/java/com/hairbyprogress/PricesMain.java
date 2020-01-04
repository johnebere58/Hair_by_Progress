package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.hairbyprogress.adapters.PricesAdapter;

import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PricesMain extends BaseActivity {

    @BindView(R.id.recyclerView)MyRecyclerView recyclerView;
    PricesAdapter pricesAdapter;
    ArrayList<BaseModel> mainList = new ArrayList<>();
    ListenerRegistration listen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_prices);
        ButterKnife.bind(this);

        pricesAdapter = new PricesAdapter(activity,mainList);
        setUpRecycleView(recyclerView,new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false),
                null,null);
        recyclerView.setAdapter(pricesAdapter);

        startLoading();

    }

    private void startLoading(){
        recyclerView.showLoading();
        if(!isConnectingToInternet()){
            recyclerView.hideLoading(new MyRecyclerView.RetryListener() {
                @Override
                public void onRetry() {
                    loadItems();
                }
            });
            return;
        }
        loadItems();
    }

    private void loadItems(){
       listen = FirebaseFirestore.getInstance().collection(PRICE_BASE)
               .addSnapshotListener(new EventListener<QuerySnapshot>() {
                   @Override
                   public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                       if(e!=null){
                           Toast(string((R.string.error_try_again)));
                           if(mainList.isEmpty()){
                               recyclerView.hideLoading(new MyRecyclerView.RetryListener() {
                                   @Override
                                   public void onRetry() {
                                       loadItems();
                                   }
                               });
                           }
                           return;
                       }
                       if(queryDocumentSnapshots==null || queryDocumentSnapshots.isEmpty()){
                           Toast(getString(R.string.list_empty));
                           createSections();
                           return;
                       }

                       for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                           BaseModel model = new BaseModel(documentSnapshot);
                           addOnceToListByReplacing(mainList,model);
                       }

                       recyclerView.hideLoading();
                       pricesAdapter.notifyDataSetChanged();
                   }
               });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeListener();
    }

    private void removeListener(){
        if(listen!=null)listen.remove();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
    }

    private void createSections(){
        String[] sections = {HAIR_PRICE_STRAIGHT,
                HAIR_PRICE_CURLY,
                HAIR_PRICE_CLOSURE,
                HAIR_PRICE_180_FRONTAL,
                HAIR_PRICE_360_FRONTAL,
                HAIR_PRICE_STRAIGHT_FRONTAL_LACE_WIG,
                HAIR_PRICE_CURLY_FRONTAL_LACE_WIG,
                HAIR_PRICE_STRAIGHT_FULL_LACE_WIG,
                HAIR_PRICE_CURLY_FULL_LACE_WIG,
                HAIR_PRICE_BOB_FRONTAL_LACE_WIG,
                HAIR_PRICE_BOB_FULL_LACE_WIG};
        for(String s:sections){
            BaseModel model = new BaseModel();
            model.put(TITLE,s);
            model.saveItem(PRICE_BASE);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK)return;
        if(data==null)return;

        if(requestCode == 199){
            boolean editting = data.getBooleanExtra(IS_EDITTING,false);
            if(editting){
                new BaseModel().updateItem(PRICE_BASE,MyApplication.ID_TO_UPDATE,CONTENT,MyApplication.dummyInch.items,null);
                Toast("Updated");
                return;
            }

            BaseModel model = new BaseModel();
            model.put(TITLE,HAIR_PRICE);
            model.put(CONTENT,MyApplication.dummyInch.items);
            model.saveItem(PRICE_BASE);
            Toast(getString(R.string.added));
        }
        MyApplication.dummyInch=null;
    }
}

