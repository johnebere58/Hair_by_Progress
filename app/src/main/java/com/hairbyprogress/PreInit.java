package com.hairbyprogress;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.BackgroundColor;
import com.hairbyprogress.custom.ColorChangingBackgroundView;
import com.hairbyprogress.kenburnsview.KenBurnsView;
import com.hairbyprogress.kenburnsview.Transition;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.hairbyprogress.MyApplication.getCurrentUser;
import static com.hairbyprogress.base.Base.*;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class PreInit extends AppCompatActivity {

    @BindView(R.id.login_but)TextView login_but;
    @BindView(R.id.signup_but)View signup_but;
    @BindView(R.id.shop_but)View shop_but;
    @BindView(R.id.vp)ViewPager vp;
    //@BindView(R.id.dot_holder)LinearLayout dot_holder;
    //@BindView(R.id.back)ColorChangingBackgroundView back;
    Handler handler = new Handler(Looper.getMainLooper());
    int vpSize = 1;

    boolean stop;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long appOpened = getLongSettings(this,APP_OPENED);
        if(appOpened==0){
            putSettings(this,APP_OPENED,System.currentTimeMillis());
        }

        final FirebaseUser currentUser = getCurrentUser();
        if(currentUser!=null){
            DocumentReference docRef = FirebaseFirestore.getInstance().collection(USER_BASE).document(currentUser.getUid());
            docRef.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        if(doc.exists()){
                            MyApplication.userModel = new BaseModel(doc);
                            MyApplication.pausedTime = (System.currentTimeMillis()-10000);
                            startActivity(new Intent(PreInit.this,MainActivity.class));
                            finish();
                        }else{
                            Toast("User Does Not Exist, Try Signing up");
                            startActivity(new Intent(PreInit.this,SignupActivity.class));
                            finish();
                        }
                    }else{
                        finish();
                    }
                }
            });
            return;
        }

        setContentView(R.layout.pre_login);
        ButterKnife.bind(this);

        /*SpannableString text = new SpannableString("Login here");
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreInit.this,LoginActivity.class));
            }
        },0,text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Already have an account? ")
                .append(text);

        login_but.setText(ssb);
        login_but.setMovementMethod(LinkMovementMethod.getInstance());

        BackgroundColor[] colors = {new BackgroundColor(getResources().getColor(R.color.white)),
                                      new BackgroundColor(getResources().getColor(R.color.brown3)),
                                      new BackgroundColor(getResources().getColor(R.color.blue6)),
                                      new BackgroundColor(getResources().getColor(R.color.brown0)),
                                              new BackgroundColor(getResources().getColor(R.color.blue3))
        };
        back.setColors(colors);*/

        shop_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreInit.this,MainActivity.class));
            }
        });

        vp.setOffscreenPageLimit(vpSize);
        vp.setAdapter(new PageAdapter(getSupportFragmentManager()));

        /*for(int i=0;i<vpSize;i++){
            LinearLayout l = (LinearLayout) View.inflate(PreInit.this,R.layout.dot_item,null);
            ImageView v = (ImageView) l.getChildAt(0);

            v.setColorFilter(getResources().getColor(R.color.blue0));
            if(i==0){
                v.setBackgroundResource(R.drawable.circle);
                v.setAlpha(1f);
            }else{
                v.setBackgroundResource(R.drawable.circle1);
                v.setAlpha(.5f);
            }

            dot_holder.addView(l);
        }*/

        //handleVp();

        vp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                stop = true;
                return false;
            }
        });

        signup_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreInit.this,SignupActivity.class));
            }
        });
        login_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreInit.this,LoginActivity.class));
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



   private class PageAdapter extends FragmentPagerAdapter{

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

   public static class ImageFragments extends Fragment{

       int position;
       @Nullable @BindView(R.id.sample)
       KenBurnsView sample;

       int imagePosition = 0;
       int images[] = {R.drawable.image_bell3,
               R.drawable.image_bells4};

       @Override
       public void onCreate(@Nullable Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           Bundle bundle = getArguments();
           position = bundle.getInt(POSITION);
       }

       @Override
       public void setRetainInstance(boolean retain) {
           super.setRetainInstance(true);
       }

       @Nullable
       @Override
       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
           ViewGroup rootView = (ViewGroup) inflater
                   .inflate(position==0?R.layout.pre_item:
                           R.layout.pre_item0, container, false);

           ButterKnife.bind(this,rootView);

           if(sample!=null){
               sample.setTransitionListener(new KenBurnsView.TransitionListener() {
                   @Override
                   public void onTransitionStart(Transition transition) {

                   }

                   @Override
                   public void onTransitionEnd(Transition transition) {
                       sample.setImageResource(images[imagePosition]);
                       imagePosition++;
                       imagePosition=imagePosition>=images.length-1?0:imagePosition;
                   }
               });
           }
           return rootView;
       }
   }

    @Override
    public void onBackPressed() {
        Intent in = new Intent(Intent.ACTION_MAIN);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.addCategory(Intent.CATEGORY_HOME);
        startActivity(in);
    }

    public void Toast(Object text){
        Toast.makeText(this, String.valueOf(text), Toast.LENGTH_SHORT).show();
    }

    public static void putSettings(Context context,String key,Object value){
        SharedPreferences shed = context.getSharedPreferences(SETTINGS_PREF,0);
        SharedPreferences.Editor se = shed.edit();
        if(value instanceof String){
            se.putString(key,value.toString());
        }
        if(value instanceof Long){
            se.putLong(key,Long.valueOf(value.toString()));
        }
        if(value instanceof Boolean){
            se.putBoolean(key, (Boolean) value);
        }
        se.apply();
    }

    public static boolean getBooleanSettings(Context context,String key){
        SharedPreferences shed = context.getSharedPreferences(SETTINGS_PREF,0);
        return shed.getBoolean(key,true);
    }

    public static String getStringSettings(Context context,String key){
        SharedPreferences shed = context.getSharedPreferences(SETTINGS_PREF,0);
        return shed.getString(key,"");
    }

    public static long getLongSettings(Context context,String key){
        SharedPreferences shed = context.getSharedPreferences(SETTINGS_PREF,0);
        return shed.getLong(key,0);
    }
}
