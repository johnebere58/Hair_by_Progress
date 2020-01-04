package com.hairbyprogress;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.custom.BackgroundColor;
import com.hairbyprogress.custom.ColorChangingBackgroundView;
import com.hairbyprogress.custom.CustomViewPager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class Intro extends BaseActivity {

    @BindView(R.id.vp)CustomViewPager vp;
    @BindView(R.id.tab)LinearLayout dot_holder;
    @BindView(R.id.back)ColorChangingBackgroundView back;
    Handler handler = new Handler(Looper.getMainLooper());

    boolean stop;

    public ArrayList<String> titles = new ArrayList<>();
    public ArrayList<String> sub_titles = new ArrayList<>();
    public ArrayList<Integer> icons = new ArrayList<>();
    public ArrayList<Integer> colors = new ArrayList<>();

    public int vpSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        titles = intent.getStringArrayListExtra(TITLE);
        sub_titles = intent.getStringArrayListExtra(SUB_TITLE);
        icons = intent.getIntegerArrayListExtra(ICONS);
        colors = intent.getIntegerArrayListExtra(COLORS);

        vpSize = colors.size();

        BackgroundColor[] col = new BackgroundColor[colors.size()];

        for(int i=0;i<colors.size();i++){
            col[i] = new BackgroundColor(colors.get(i));
        }

        back.setColors(col);

        vp.setOffscreenPageLimit(vpSize);
        vp.setAdapter(new PageAdapter(getFragmentManager()));

        for(int i=0;i<vpSize;i++){
            LinearLayout l = (LinearLayout) View.inflate(context,R.layout.dot_item1,null);
            ImageView v = (ImageView) l.getChildAt(0);

            if(i==0){
                v.setBackgroundResource(R.drawable.circle);
                v.setAlpha(1f);
            }else{
                v.setBackgroundResource(R.drawable.circle1);
                v.setAlpha(.3f);
            }

            dot_holder.addView(l);
        }

        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                back.setPosition(position,positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0;i<vpSize;i++){
                    LinearLayout l = (LinearLayout) dot_holder.getChildAt(i);
                    if(l==null)return;
                    ImageView v = (ImageView) l.getChildAt(0);

                    if(i==position){
                        v.setBackgroundResource(R.drawable.circle);
                        v.setAlpha(1f);
                    }else{
                        v.setBackgroundResource(R.drawable.circle1);
                        v.setAlpha(.3f);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        handleVp();

        vp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                stop = true;
                return false;
            }
        });

    }


    private void handleVp(){
        if(stop)return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              int p = vp.getCurrentItem();
              int size = vpSize;

              p++;
              p = p>=size?0:p;

              vp.setCurrentItem(p);

              if(p==vpSize-1)stop = true;

              handleVp();

            }
        },5000);
    }



   private class PageAdapter extends FragmentPagerAdapter {

      public PageAdapter(FragmentManager fm) {
           super(fm);
       }


       @Override
       public Fragment getItem(int position) {
           ImageFragments fragments = new ImageFragments();
           Bundle args = new Bundle();
           args.putInt(POSITION,position);
           fragments.setArguments(args);
           return fragments;
       }

       @Override
       public int getCount() {
           return vpSize;
       }
   }

   public static class ImageFragments extends BaseFragment{

       int position;
       Intro intro;
       @BindView(R.id.imv)ImageView imv;
       @BindView(R.id.title_tv)TextView title_tv;
       @BindView(R.id.title_tv1)TextView title_tv1;
       @BindView(R.id.ok)View ok;
       @BindView(R.id.ok_tv)TextView ok_tv;


       @Override
       public void onCreate(@Nullable Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           Bundle bundle = getArguments();
           position = bundle.getInt(POSITION);
           intro = (Intro)activity;
       }

       @Nullable
       @Override
       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
           ViewGroup rootView = (ViewGroup) inflater
                   .inflate(R.layout.intro_frag, container, false);

           ButterKnife.bind(this,rootView);

           String title = intro.titles.get(position);
           String sub_title = intro.sub_titles.get(position);
           int image = intro.icons.get(position);
           int color = intro.colors.get(position);
           title_tv.setText(title);
           title_tv1.setText(sub_title);
           imv.setImageResource(image);

           ok_tv.setTextColor(color);

           if(title.isEmpty())title_tv.setVisibility(View.GONE);else title_tv.setVisibility(View.VISIBLE);
           if(sub_title.isEmpty())title_tv1.setVisibility(View.GONE);else title_tv1.setVisibility(View.VISIBLE);

           if(image==0){
               imv.setVisibility(View.GONE);
           }else{
               imv.setVisibility(View.VISIBLE);
           }

           if(position==intro.vpSize-1){
               ok.setVisibility(View.VISIBLE);
           }else{
               ok.setVisibility(View.GONE);
           }


           ok.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   activity.finish();
               }
           });

           return rootView;
       }
   }

    @Override
    public void onBackPressed() {
        if(vp.getCurrentItem()==0){
            super.onBackPressed();
            return;
        }
        int p = vp.getCurrentItem();
        p--;
        p = p<0?0:p;
        vp.setCurrentItem(p);
    }
}
