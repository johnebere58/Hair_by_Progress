

package com.hairbyprogress.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.hairbyprogress.R;


public class RelativeTimeTextView extends android.support.v7.widget.AppCompatTextView {
    private long mReferenceTime;
    private String mText;
    private String mPrefix;
    private String mSuffix;
    private Handler mHandler = new Handler();
    private UpdateTimeRunnable mUpdateTimeTask;

    public RelativeTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public RelativeTimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RelativeTimeTextView, 0, 0);

        try {
            this.mText = a.getString(R.styleable.RelativeTimeTextView_reference_time);
            this.mPrefix = a.getString(R.styleable.RelativeTimeTextView_relative_time_prefix);
            this.mSuffix = a.getString(R.styleable.RelativeTimeTextView_relative_time_suffix);
            this.mPrefix = this.mPrefix == null?"":this.mPrefix;
            this.mSuffix = this.mSuffix == null?"":this.mSuffix;
        } finally {
            a.recycle();
        }

        try {
            this.mReferenceTime = Long.valueOf(this.mText).longValue();
        } catch (NumberFormatException var7) {
            this.mReferenceTime = -1L;
        }

    }

    public String getPrefix() {
        return this.mPrefix;
    }

    public void setPrefix(String prefix) {
        this.mPrefix = prefix;
        this.updateTextDisplay();
    }

    public String getSuffix() {
        return this.mSuffix;
    }

    public void setSuffix(String suffix) {
        this.mSuffix = suffix;
        this.updateTextDisplay();
    }

    public void setReferenceTime(long referenceTime) {
        this.mReferenceTime = referenceTime;
        try {
            this.stopTaskForPeriodicallyUpdatingRelativeTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mUpdateTimeTask = new UpdateTimeRunnable(this.mReferenceTime);
        try {
            this.startTaskForPeriodicallyUpdatingRelativeTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.updateTextDisplay();
    }

    private void updateTextDisplay() {
        if(this.mReferenceTime != -1L) {
            this.setText(this.mPrefix + this.getRelativeTimeDisplayString() + this.mSuffix);
        }
    }

    private CharSequence getRelativeTimeDisplayString() {
        long now = System.currentTimeMillis();
        long difference = now - this.mReferenceTime;
        CharSequence ch = (CharSequence)(difference >= 0L && difference <= 60000L?"Now":DateUtils.getRelativeTimeSpanString(this.mReferenceTime, now, 60000L, 262144));
        String time = ch.toString();
        /*time = time.replace(" ago","");
        time = time.replace(" min","m");
        time = time.replace(" hour","h");
        time = time.replace(" day","d");

        time = time.replace(" mins","m");
        time = time.replace(" hours","h");
        time = time.replace(" days","d");*/
        return time;

    }

    public void setTimeText(String s){
        this.setText(s);
        try {
            this.stopTaskForPeriodicallyUpdatingRelativeTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            this.startTaskForPeriodicallyUpdatingRelativeTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            this.stopTaskForPeriodicallyUpdatingRelativeTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(visibility != INVISIBLE && visibility != GONE) {
            try {
                this.startTaskForPeriodicallyUpdatingRelativeTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.stopTaskForPeriodicallyUpdatingRelativeTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startTaskForPeriodicallyUpdatingRelativeTime() throws Exception{
        this.mHandler.post(this.mUpdateTimeTask);
    }

    private void stopTaskForPeriodicallyUpdatingRelativeTime() throws Exception{
        this.mHandler.removeCallbacks(this.mUpdateTimeTask);
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.referenceTime = this.mReferenceTime;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
        } else {
            SavedState ss = (SavedState)state;
            this.mReferenceTime = ss.referenceTime;
            super.onRestoreInstanceState(ss.getSuperState());
        }
    }

    private class UpdateTimeRunnable implements Runnable {
        private long mRefTime;

        UpdateTimeRunnable(long refTime) {
            this.mRefTime = refTime;
        }

        public void run() {
            long difference = Math.abs(System.currentTimeMillis() - this.mRefTime);
            long interval = 60000L;
            if(difference > 604800000L) {
                interval = 604800000L;
            } else if(difference > 86400000L) {
                interval = 86400000L;
            } else if(difference > 3600000L) {
                interval = 3600000L;
            }

            RelativeTimeTextView.this.updateTextDisplay();
            RelativeTimeTextView.this.mHandler.postDelayed(this, interval);
        }
    }

    public static class SavedState extends BaseSavedState {
        private long referenceTime;
        public static final Creator<SavedState> CREATOR = new Creator() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
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

        private SavedState(Parcel in) {
            super(in);
            this.referenceTime = in.readLong();
        }
    }
}
