package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InchMain extends BaseActivity {

    @BindView(R.id.proceed)View proceed;
    @BindView(R.id.lay)LinearLayout lay;

    boolean isEditting;
//    String hairLength;
//    String hairPrice;
    String selectedLength;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inch_main);
        ButterKnife.bind(this);

        if(MyApplication.sections.isEmpty()){
            Toast(getString(R.string.setting_up));
            finish();
            return;
        }

        Intent intent = getIntent();
        selectedLength = intent.getStringExtra(HAIR_LENGTH);
//        hairLength = intent.getStringExtra(HAIR_LENGTH);
//        hairPrice = intent.getStringExtra(PRICE);

        ArrayList<String> inches = getFromSections(HAIR_LENGTH);
        for(String length:inches){
            addLengthView(length);
        }

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> map = getInches();
                if(map.keySet().isEmpty()){
                    Toast("No price entered");
                    return;
                }
                if(selectedLength!=null){
                    if(map.get(selectedLength)==null){
                        Toast(String.format("Enter the price for %s",selectedLength));
                        return;
                    }
                }

                MyApplication.dummyInch = new BaseModel(map);
                Intent intent = getIntent();
                intent.putExtra(IS_EDITTING,isEditting);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    private HashMap<String,Object> getInches(){
        HashMap<String,Object> map = new HashMap<>();
        for(int i=0;i<lay.getChildCount();i++){
            View view = lay.getChildAt(i);
            TextView length_tv = view.findViewById(R.id.length_tv);
            EditText price_etv = view.findViewById(R.id.price_etv);

            String price = price_etv.getText().toString().trim();
            if(price.isEmpty())continue;

            map.put(length_tv.getText().toString(),Integer.valueOf(price));
        }
        return map;
    }

    private void addLengthView(String length){
        View view = View.inflate(context,R.layout.inch_main_item,null);
        TextView length_tv = view.findViewById(R.id.length_tv);
        EditText price_etv = view.findViewById(R.id.price_etv);
        length_tv.setText(length);

        if(MyApplication.dummyInch!=null){
            price_etv.setText(MyApplication.dummyInch.getString(length));
            isEditting=true;
        }

        lay.addView(view);
    }

}
