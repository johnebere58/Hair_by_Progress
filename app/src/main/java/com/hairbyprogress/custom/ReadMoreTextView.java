

package com.hairbyprogress.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseActivity;


public class ReadMoreTextView extends android.support.v7.widget.AppCompatTextView {
    BaseActivity baseActivity;
    int minLength = getResources().getInteger(R.integer.min_text);

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public ReadMoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public ReadMoreTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        baseActivity = new BaseActivity();

    }

    public void setReadMoreText(final String text,boolean clickable){
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if(text.length()>minLength) {
            String minText = text.substring(0, minLength);
            ssb.append(minText);
            ssb.append("...");
            ssb.append(readLessSpan(text,minText,null));
            setText(ssb);
        }else{
            setText(text);
        }

        if(clickable)setMovementMethod(LinkMovementMethod.getInstance());
    }
    public void setReadMoreText(final String text,boolean full,boolean clickable,ToggleReadMore onToggle){
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if(text.length()>minLength) {
        String minText = text.substring(0, minLength);
            if(!full) {
                ssb.append(minText);
                ssb.append("...");
                ssb.append(readLessSpan(text, minText,onToggle));
                setText(ssb);
            }else{
                SpannableStringBuilder ssb1 = new SpannableStringBuilder();
                ssb1.append(text);
                ssb1.append("  ");
                ssb1.append(readMoreSpan(text,minText,onToggle));
                setText(ssb1);
            }
        }else{
            setText(text);
        }

        if(clickable)setMovementMethod(LinkMovementMethod.getInstance());
    }


    private SpannableString readLessSpan(final String text, final String lessText, final ToggleReadMore onToggle){
        return BaseActivity.spanStringClickableBoldandForeground("Read More", getResources().getColor(R.color.blue0),
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SpannableStringBuilder ssb1 = new SpannableStringBuilder();
                        ssb1.append(text);
                        ssb1.append("  ");
                        ssb1.append(readMoreSpan(text,lessText,onToggle));
                        setText(ssb1);
                        if(onToggle!=null)onToggle.onToggled(true);
                    }
                });
    }
    private SpannableString readMoreSpan(final String text, final String lessText, final ToggleReadMore onToggle){
        return BaseActivity.spanStringClickableBoldandForeground("Read Less", getResources().getColor(R.color.blue0),
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SpannableStringBuilder ssb1 = new SpannableStringBuilder();
                        ssb1.append(lessText);
                        ssb1.append("...");
                        ssb1.append(readLessSpan(text,lessText,onToggle));
                        setText(ssb1);
                        if(onToggle!=null)onToggle.onToggled(false);
                    }
                });
    }


}
