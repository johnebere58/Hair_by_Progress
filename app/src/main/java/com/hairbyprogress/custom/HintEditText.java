package com.hairbyprogress.custom;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.hairbyprogress.OnComplete;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseActivity;


public class HintEditText extends android.support.v7.widget.AppCompatEditText{


    private Context context;
    private boolean changing;
    private OnComplete doneListener;
    private String codeBuilder ="";
    private final String SPACE = "  ";
    TextWatcher tw;

    public void setDoneListener(OnComplete doneListener) {
        this.doneListener = doneListener;
    }

    public HintEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public HintEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public HintEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void init(){

    if(tw!=null)removeTextChangedListener(tw);

    setTextIsSelectable(false);
    setInputType(InputType.TYPE_CLASS_NUMBER);
    setCursorVisible(false);
    setText(String.format("_%s_%s_%s_",SPACE,SPACE,SPACE));
    setSelection(0);
    codeBuilder="";

    tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable s) {
            if(changing)return;
            changing=true;
            chkText(s.toString());
        }
    };

    addTextChangedListener(tw);

    setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            setCursor();
        }
    });
    }


    private void chkText(String text){
        text = getStringFromCode(text);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for(int i=0;i<text.length();i++){

            String s = String.valueOf(text.charAt(i));
            s = s.trim();

            if(!s.isEmpty()){
                sb.append(s);
                count++;
            }

            if(count==4)break;
        }

        if(count<4){

            int rem = 4-sb.length();
            for(int i=0;i<rem;i++){
                sb.append("_");
            }

        }

        addSpacing(sb.toString());

    }

    private void addSpacing(String text){
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int length = text.length();
        for(int i=0;i<length;i++){

            String s = String.valueOf(text.charAt(i));
            s = s.trim();

            if(!s.isEmpty()){
             ssb.append(s/*.equals("_")?s: BaseActivity.spanStringForeground(s,context.getResources().getColor(R.color.white))*/);
             if(i!=(length-1))ssb.append(SPACE);
            }
        }
        codeBuilder = ssb.toString();
        setText(getCodeFromString(ssb.toString()));
        setCursor();
    }

    private void setCursor(){
        String text = getText()==null?"":getText().toString();
        if(text.isEmpty()){
            setSelection(0);
        }
        else if(!text.contains("_")){
            setSelection(text.length());
        }else if(countString(text,"_")==4){
            setSelection(0);
        }else {
            try {
                setSelection(text.indexOf("_") - SPACE.length());
            }catch (Exception ignored){
                setSelection(text.length());
            };
        }
        changing=false;
        doneListener.onComplete(null,getTheCode());
        setError(null);
    }

    private int countString(String text,String s){
        int count=0;
        for(int i=0;i<text.length();i++){
        String x = String.valueOf(text.charAt(i));
        if(x.equals(s))count++;
        }
        return count;
    }

    private String getTheCode(){
        StringBuilder sb = new StringBuilder();
        String text = getText().toString().trim();
        int length = text.length();
        for(int i=0;i<length;i++){

            String s = String.valueOf(text.charAt(i));
            s = s.trim();

            if(!s.isEmpty()){
                s = s.equals("*")?String.valueOf(codeBuilder.charAt(i)):s;
                sb.append(s);
            }
        }

        return sb.toString().trim();
    }
    private String getCodeFromString(String text){
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        for(int i=0;i<length;i++){

            String s = String.valueOf(text.charAt(i));
            s = !s.equals("_") && !s.trim().isEmpty()?"*":s;
            sb.append(s);
        }

        return sb.toString().trim();
    }
    private String getStringFromCode(String code){
        if(codeBuilder.isEmpty())return code;

        StringBuilder sb = new StringBuilder();
        int length = code.length();
        for(int i=0;i<length;i++){

            String s = String.valueOf(code.charAt(i));
            s = s.equals("*")?String.valueOf(codeBuilder.charAt(i)):s;
            sb.append(s);
        }

        return sb.toString().trim();
    }

}
