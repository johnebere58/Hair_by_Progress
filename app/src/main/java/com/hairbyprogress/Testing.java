package com.hairbyprogress;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hairbyprogress.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Testing extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_notify_item);
        ButterKnife.bind(this);

    }

}
