package com.hairbyprogress;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.adapters.PageAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;
import com.hairbyprogress.services.AppSettingsService;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.vp)public CustomViewPager vp;
    @BindView(R.id.lay)LinearLayout tab_layout;
    @BindView(R.id.cart_but)View cart_but;
    @BindView(R.id.cart_count)TextView cart_count;

    CartReceiver cartReceiver;

    @BindView(R.id.update_layout)View update_layout;
    @BindView(R.id.features_tv)TextView features_tv;
    @BindView(R.id.later_but)View later_but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);

        MyApplication.alreadyStarted=true;

        startService(new Intent(context, AppSettingsService.class));

        cartReceiver = new CartReceiver();
        registerReceiver(cartReceiver,new IntentFilter(BROADCAST_CART_UPDATED));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions((Activity) context,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },34);
            }
        }

        PageAdapter pageAdapter = new PageAdapter(getFragmentManager());
        vp.setAdapter(pageAdapter);
        vp.setOffscreenPageLimit(4);
        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int p) {
                set_selection(p);
                hide_keyboard(vp);

                Intent intent = new Intent(BROADCAST_NOTIFY);
                intent.putExtra(ACTION,ACTION_REFRESH);
                sendBroadcast(intent);

            }

        });

        cart_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyApplication.userModel==null){
                    activity.startActivity(new Intent(context, LoginActivity.class));
                    //Toast("You m");
                    return;
                }
                startActivity(new Intent(context,CartMain.class));
            }
        });

        cart_but.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!MyApplication.isAdmin)return false;

                new MaterialDialog.Builder(context)
                        .items("SECTION MAIN","PRICES MAIN","POST HOME MAIN","POST PRODUCT","POST NEWS","ADD RATING","TESTING","LOGOUT")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                if(position==0){
                                    startActivity(new Intent(context,SectionsMain.class));
                                }
                                if(position==1){
                                    startActivity(new Intent(context,PricesMain.class));
                                }
                                if(position==2){
                                    startActivity(new Intent(context,PostHomeTop.class));
                                }
                                if(position==3){
                                    startActivity(new Intent(context,PostProduct.class));
                                }
                                if(position==4){
                                    startActivity(new Intent(context,PostNews.class));
                                }
                                if(position==5){
                                    startActivity(new Intent(context,RateMain.class).putExtra(IS_ADMIN,true));
                                }
                                if(position==6){
                                    startActivity(new Intent(context,Testing.class));
                                }
                                if(position==7){
                                    logOut(cart_but);
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

        loadCartCount();

        chkVersion();
    }

    public void set_selection(int position) {
        for (int i = 0; i < tab_layout.getChildCount(); i++) {
            View v = tab_layout.getChildAt(i);
            //View back = v.findViewWithTag("back");
            if (i == position) {
                //back.setVisibility(View.VISIBLE);
                v.setAlpha(1f);
                v.setBackgroundResource(R.drawable.curve_brown6);
            } else {
                //back.setVisibility(View.GONE);
                v.setAlpha(.3f);
                v.setBackgroundResource(0);
            }
        }
    }

    private class CartReceiver  extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            loadCartCount();
        }
    }


    private void loadCartCount(){
        if(MyApplication.userModel!=null){
            int count = MyApplication.userModel.getList(MY_CART_ITEMS).size();
            count = count<0?0:count;
            cart_count.setText(String.valueOf(count));
            cart_count.setVisibility(count==0?View.GONE:View.VISIBLE);
        }
    }

    public void setPosition(View v) {
        String s = v.getTag().toString();
        vp.setCurrentItem(Integer.valueOf(s));
    }

    boolean rateShowing;
    @Override
    public void onBackPressed() {
        if(vp.getCurrentItem()!=0){
            vp.setCurrentItem(0);
            return;
        }


        goBack();
    }

    private void goBack(){
        Intent in = new Intent(Intent.ACTION_MAIN);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.addCategory(Intent.CATEGORY_HOME);
        startActivity(in);
        finish();
    }

    private void chkVersion() {


        int version = getVersion();
        HashMap<String, Object> map = (HashMap<String, Object>) MyApplication.appSettingsModel.get(NEW_UPDATE);
        if (map == null) return;


        BaseModel newVersion = new BaseModel(MyApplication.appSettingsModel.getMap(NEW_UPDATE));

        if (version >= newVersion.getInt(VERSION_CODE)) {
            update_layout.setVisibility(View.GONE);
            return;
        }

        final String feature = newVersion.getString(NEW_FEATURE);
        final boolean must = newVersion.getBoolean(MUST_UPDATE);

        StringBuilder sb = new StringBuilder();
        if (feature.isEmpty()) {
            sb.append("*Fixed Bugs");
        } else {
            String[] parts = feature.split("-");
            for (String s : parts) {
                sb.append(String.format("*%s\n", s.trim()));
            }
        }

        features_tv.setText(sb.toString());

        update_layout.setVisibility(View.VISIBLE);
        later_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.isAdmin || !must) {
                    update_layout.setVisibility(View.GONE);
                } else {
                    goBack();
                }
            }
        });
    }


}
