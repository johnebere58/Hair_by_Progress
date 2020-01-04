package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.hairbyprogress.adapters.SectionAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionsMain extends BaseActivity {

    @BindView(R.id.post_section)View post_section;
    @BindView(R.id.recyclerView)MyRecyclerView recyclerView;
    SectionAdapter sectionAdapter;
    ArrayList<BaseModel> mainList = new ArrayList<>();
    ListenerRegistration listen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_sections);
        ButterKnife.bind(this);

        sectionAdapter = new SectionAdapter(activity,mainList);
        setUpRecycleView(recyclerView,new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false),
                null,null);
        recyclerView.setAdapter(sectionAdapter);

        startLoading();

        post_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSection();
            }
        });

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
       listen = FirebaseFirestore.getInstance().collection(SECTIONS_BASE)
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
                       sectionAdapter.notifyDataSetChanged();
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
        String[] sections = {ITEM_CATEGORY,HAIR_COLOR,HAIR_GRAM,HAIR_LENGTH,HAIR_TYPE};
        for(String s:sections){
            BaseModel model = new BaseModel();
            model.put(TITLE,s);
            model.put(SECTION_TYPE,SECTION_TYPE_LIST);
            model.saveItem(SECTIONS_BASE);
        }

        BaseModel model = new BaseModel();
        model.put(TITLE,CUSTOMIZING_FEE);
        model.put(SECTION_TYPE,SECTION_TYPE_TEXT);
        model.saveItem(SECTIONS_BASE);


    }

    private void addSection(){
        new MaterialDialog.Builder(context)
                .title(R.string.title)
                .input(getString(R.string.enter_title), "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        final String title = input.toString().trim();

                        if(title.equalsIgnoreCase(HAIR_PRICE)){
                            startActivityForResult(new Intent(context,InchMain.class),199);
                            return;
                        }
                        new MaterialDialog.Builder(context)
                                .items("AS LIST",/*"AS MAP",*/"AS PLAIN TEXT")
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                        final int sectionType = position;
                                        new MaterialDialog.Builder(context)
                                                .title(R.string.content)
                                                .input(getString(R.string.use_comma), "", false, new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                        String content = input.toString().trim();

                                                        BaseModel model = new BaseModel();
                                                        model.put(TITLE,title);
                                                        model.put(SECTION_TYPE,sectionType);
                                                        model.put(CONTENT,sectionType==SECTION_TYPE_LIST?
                                                                  convertStringToList(",",content)
                                                                  :content);
                                                        model.saveItem(SECTIONS_BASE);
                                                        Toast(getString(R.string.added));

                                                    }
                                                })
                                                .show();

                                    }
                                })
                                .show();

                    }
                })
                .positiveText("CREATE")
                .negativeText("CANCEL")
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK)return;
        if(data==null)return;

        if(requestCode == 199){
            /*boolean editting = data.getBooleanExtra(IS_EDITTING,false);
            if(editting){
                new BaseModel().updateItem(SECTIONS_BASE,MyApplication.ID_TO_UPDATE,CONTENT,MyApplication.dummyInch.items,null);
                Toast("Updated");
                return;
            }

            BaseModel model = new BaseModel();
            model.put(TITLE,HAIR_PRICE);
            model.put(CONTENT,MyApplication.dummyInch.items);
            model.put(SECTION_TYPE,SECTION_TYPE_MAP);
            model.saveItem(SECTIONS_BASE);
            Toast(getString(R.string.added));*/
        }
        MyApplication.dummyInch=null;
    }
}

