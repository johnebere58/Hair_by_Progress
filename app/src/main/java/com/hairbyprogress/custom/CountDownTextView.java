package com.hairbyprogress.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;

import com.hairbyprogress.R;
import com.hairbyprogress.base.Base;


import java.util.Locale;

import static com.hairbyprogress.base.Base.oneDay;
import static com.hairbyprogress.base.BaseActivity.spanStringForeground;


/**
 * Created by John Ebere on 11/16/2016.
 * Copyright of Maugost Incorporated
 */
public class CountDownTextView extends android.support.v7.widget.AppCompatTextView{

    long countDownTime;
    long hoursGiven;
    CountDownUtil countDownUtil;
    CountDownChange countDownChange;
    boolean stopCountDown = false;
    private Handler mHandler = new Handler();
    private UpdateTimeRunnable mUpdateTimeTask;
    public boolean timeIsUp;
    String timeUpText;

    public void setCountDownChange(CountDownChange countDownChange) {
        this.countDownChange = countDownChange;
    }

    public void setTimeUpText(String timeUpText) {
        this.timeUpText = timeUpText;
    }

    public CountDownTextView(Context context) {
        super(context);
    }

    public CountDownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCountDownListener(CountDownUtil countDownUtil){
        this.countDownUtil = countDownUtil;
    }

    public void setCountDownTime(long countDownTime,int hoursGiven) {

        stopCountDown=false;
        this.hoursGiven = Base.oneHour * hoursGiven;
        this.countDownTime = countDownTime;
        try {
            this.stopTaskForPeriodicallyUpdatingCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mUpdateTimeTask = new UpdateTimeRunnable();
        try {
            this.startTaskForPeriodicallyUpdatingCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.updateTextDisplay();
    }


    private void updateTextDisplay() {
        this.setText(this.getCountDownTimeDisplayString());

    }

    private CharSequence getCountDownTimeDisplayString() {
        /*long systemTime = 0;
        try {
            systemTime = BaseActivity.getCurrentNetworkTime();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        long systemTime = System.currentTimeMillis();
        long time_rem =  systemTime - countDownTime;

        long time =  hoursGiven;

        long time_left = time - time_rem;

        if(time_left<=0){
            if(countDownUtil!=null)countDownUtil.onTimeUp();
            try {
                stopCountDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            timeIsUp = true;

        }else if(countDownChange!=null){
            long hLeft = time_left/Base.oneHour;
            countDownChange.onTimeChange((int) hLeft);
        }

        return convertTime(time_left);
    }

    public SpannableStringBuilder convertTime(long milli){

        long oneHour = 3600000;
        long oneMin = 60000;
        long oneSec = 1000;

        long day = milli/oneDay;
        long hours = (milli % oneDay)/oneHour;
        long mins = (milli % oneHour) / oneMin;
        long secs = ( (milli % oneHour) % oneMin ) / oneSec;

        String tm;
        if(secs<=0){
            tm = timeUpText==null||timeUpText.isEmpty()?"Time up":timeUpText;
        }
        else if(day==0){
            tm = String.format(Locale.getDefault(),"%01d Hour%s %01d Minute%s", hours,hours>1?"s":"",
                    mins,mins>1?"s":""
                    );
        }else{
            tm = String.format(Locale.getDefault(),"%01d Day%s %01d Hour%s", day,day>1?"s":"",
                    hours,hours>1?"s":"");
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if(!(secs < 0))ssb.append("Time left: ");

        ssb.append(spanStringForeground(tm,this.getResources().getColor(R.color.red0)));

        return ssb;

    }


    public void stopCountDown() {
        try {
            this.stopTaskForPeriodicallyUpdatingCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacks(mUpdateTimeTask);
        stopCountDown=true;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            this.startTaskForPeriodicallyUpdatingCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            this.stopTaskForPeriodicallyUpdatingCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(visibility != View.GONE && visibility != View.INVISIBLE) {
            try {
                this.startTaskForPeriodicallyUpdatingCountDownTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.stopTaskForPeriodicallyUpdatingCountDownTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void startTaskForPeriodicallyUpdatingCountDownTime() throws Exception{
        this.mHandler.post(this.mUpdateTimeTask);
    }

    private void stopTaskForPeriodicallyUpdatingCountDownTime() throws Exception{
        this.mHandler.removeCallbacks(this.mUpdateTimeTask);
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.referenceTime = this.countDownTime;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
        } else {
            SavedState ss = (SavedState)state;
            this.countDownTime = ss.referenceTime;
            super.onRestoreInstanceState(ss.getSuperState());
        }
    }

    private class UpdateTimeRunnable implements Runnable {

        public void run() {
            if(stopCountDown)return;
            long interval = 1000;
            CountDownTextView.this.updateTextDisplay();
            CountDownTextView.this.mHandler.postDelayed(this, interval);
        }
    }

    public static class SavedState extends BaseSavedState {
        private long referenceTime;
        public static final Creator<SavedState> CREATOR = new Creator() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.referenceTime);
        }

        private SavedState(Parcel parcel, Parcel in) {
            super(in);
            this.referenceTime = in.readLong();
        }
    }
}
