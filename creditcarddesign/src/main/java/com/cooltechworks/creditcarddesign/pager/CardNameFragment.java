package com.cooltechworks.creditcarddesign.pager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.cooltechworks.creditcarddesign.CardEditActivity;
import com.cooltechworks.creditcarddesign.R;

import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_CVV;
import static com.cooltechworks.creditcarddesign.CreditCardUtils.EXTRA_CARD_HOLDER_NAME;
/**
 * Created by sharish on 9/1/15.
 */
public class CardNameFragment extends CreditCardFragment {


    CardEditActivity cardEditActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardEditActivity = (CardEditActivity)getActivity();
    }


    public CardNameFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {

        View v = inflater.inflate(R.layout.lyt_card_holder_name, group,false);
        cardEditActivity.mCardNameView = (EditText) v.findViewById(R.id.card_name);

        String name = "";
        if(getArguments() != null && getArguments().containsKey(EXTRA_CARD_HOLDER_NAME)) {
            name = getArguments().getString(EXTRA_CARD_HOLDER_NAME);
        }


        if(name == null) {
            name = "";
        }

        cardEditActivity.mCardNameView.setText(name);
        cardEditActivity.mCardNameView.addTextChangedListener(this);

        cardEditActivity.mCardNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE){
                    cardEditActivity.showNext();
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void afterTextChanged(Editable s) {

        onEdit(s.toString());
        if(s.length() == getResources().getInteger(R.integer.card_name_len)) {
            onComplete();
        }
    }

    @Override
    public void focus() {

        if(isAdded()) {
            cardEditActivity.mCardNameView.selectAll();
        }
    }
}
