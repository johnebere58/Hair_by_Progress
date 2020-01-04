package com.hairbyprogress;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.hairbyprogress.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.hairbyprogress.MyApplication;
import com.hairbyprogress.base.BaseModel;


public class AddAddress extends BaseActivity {

    @BindView(R.id.name_etv)EditText name_etv;
    @BindView(R.id.phone_etv)EditText phone_etv;
    @BindView(R.id.state_etv)EditText state_etv;
    @BindView(R.id.city_etv)EditText city_etv;
    @BindView(R.id.street_etv)EditText street_etv;
    @BindView(R.id.add_but)View add_but;

    boolean editting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_address);
        ButterKnife.bind(this);

        editting = MyApplication.dummyAddress!=null;

        if(editting){
            name_etv.setText(MyApplication.dummyAddress.getString(NAME));
            phone_etv.setText(MyApplication.dummyAddress.getString(PHONE_NUMBER));
            street_etv.setText(MyApplication.dummyAddress.getString(STREET));
            state_etv.setText(MyApplication.dummyAddress.getString(STATE));
            city_etv.setText(MyApplication.dummyAddress.getString(CITY));
        }

        add_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAddress();
            }
        });
    }

    private void addAddress(){
        String name = name_etv.getText().toString().trim();
        String phone = phone_etv.getText().toString().trim();
        String street = street_etv.getText().toString().trim();
        String city = city_etv.getText().toString().trim();
        String state = state_etv.getText().toString().trim();

        if(name.length()<4 || !name.contains(" ")){
            name_etv.setError("Enter your full name");
            name_etv.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            phone_etv.setError("Enter your phone number");
            phone_etv.requestFocus();
            return;
        }

        if(street.isEmpty()){
            street_etv.setError("Enter your street address");
            street_etv.requestFocus();
            return;
        }

        if(city.isEmpty()){
            city_etv.setError("Enter your city");
            city_etv.requestFocus();
            return;
        }

        if(state.isEmpty()){
            state_etv.setError("Enter your state");
            state_etv.requestFocus();
            return;
        }

        MyApplication.dummyAddress=new BaseModel();
        MyApplication.dummyAddress.put(NAME,name);
        MyApplication.dummyAddress.put(PHONE_NUMBER,phone);
        MyApplication.dummyAddress.put(STREET,street);
        MyApplication.dummyAddress.put(CITY,city);
        MyApplication.dummyAddress.put(STATE,state);


        hide_keyboard(add_but);

        setResult(RESULT_OK,getIntent());
        finish();

    }

}
