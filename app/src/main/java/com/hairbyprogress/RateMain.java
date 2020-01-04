package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateMain extends BaseActivity {

    @BindView(R.id.submit)View submit;
    @BindView(R.id.rating_bar)MaterialRatingBar rating_bar;
    @BindView(R.id.review_etv)EditText review_etv;

    String objectId;
    String ratingText;
    String rating;
    boolean asAdminUser;

    ArrayList<String> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        asAdminUser = intent.getBooleanExtra(IS_ADMIN,false);

        objectId = intent.getStringExtra(Base.OBJECT_ID);
        ratingText = intent.getStringExtra(Base.RATINGS_TEXT);
        rating = intent.getStringExtra(Base.RATINGS);

        if(rating!=null)rating_bar.setRating(rating.isEmpty()?0:Float.valueOf(rating));
        if(ratingText!=null)review_etv.setText(ratingText);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingText = review_etv.getText().toString().trim();
                rating = String.valueOf(rating_bar.getRating());

                if(rating_bar.getRating()==0){
                    Toast("Click on the stars to review");
                    return;
                }

                if(ratingText.isEmpty()){
                    review_etv.setError("Write a Review");
                    review_etv.requestFocus();
                    return;
                }

                if(!asAdminUser) {

                hideKeyboard(submit);

                Intent in = getIntent();
                in.putExtra(RATINGS_TEXT,ratingText);
                in.putExtra(RATINGS,rating);
                setResult(RESULT_OK,in);
                finish();

                }else{
                    if(items.isEmpty()){
                        startActivityForResult(new Intent(context,OrderItems.class).putExtra(FOR_RATING,true),1);
                        return;
                    }

                    new MaterialDialog.Builder(context)
                            .input("Add a full name", "", false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    String name =input.toString().trim();
                                    BaseModel model = new BaseModel();
                                    model.put(RATINGS_TEXT,ratingText);
                                    model.put(RATINGS,rating);
                                    model.put(NOTIFY_TYPE,NOTIFY_TYPE_FEEDBACK);
                                    model.put(NAME,name);
                                    model.put(ITEMS_IN_CART,items);
                                    model.put(STATUS,APPROVED);
                                    model.saveItem(NOTIFY_BASE);

                                    new MaterialDialog.Builder(context)
                                            .content("Will you like to add again?")
                                            .positiveText("YES")
                                            .negativeText("NO")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    recreate();
                                                    items.clear();
                                                    rating_bar.setRating(0);
                                                    review_etv.setText("");
                                                }
                                            })
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    finish();
                                                }
                                            })
                                            .show();
                                }
                            })
                            .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                            .positiveText("ADD NAME")
                            .show();


                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode !=RESULT_OK) return;

        if(requestCode==1){
            ArrayList<String> itemsList = data.getStringArrayListExtra(ITEMS_IN_CART);
            items.addAll(itemsList);
            Toast("Items Added");
        }
    }
}
