package com.hairbyprogress.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hairbyprogress.LoginActivity;
import com.hairbyprogress.MainActivity;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.R;
import com.hairbyprogress.SignupActivity;
import com.hairbyprogress.base.BaseFragment;
import com.hairbyprogress.base.BaseModel;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.appSettingsModel;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.settingsHasLoaded;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.base.Base.APP_SETTINGS;
import static com.hairbyprogress.base.Base.APP_SETTINGS_BASE;
import static com.hairbyprogress.base.Base.EMAIL;
import static com.hairbyprogress.base.Base.IS_ADMIN;
import static com.hairbyprogress.base.Base.MAX_PENDING_AMOUNT;
import static com.hairbyprogress.base.Base.PACKAGE_NAME;
import static com.hairbyprogress.base.Base.PUSH_NOTIFICATION;
import static com.hairbyprogress.base.Base.USER_BASE;
import static com.hairbyprogress.base.Base.USER_ID;
import static com.hairbyprogress.base.Base.WEBSITE;
import static com.hairbyprogress.base.BaseActivity.getBooleanSettings;
import static com.hairbyprogress.base.BaseActivity.putSettings;

public class FragmentProfile extends BaseFragment {

    @BindView(R.id.email_tv)TextView email_tv;
    @BindView(R.id.logout_but)View logout_but;
    @BindView(R.id.login_but)View login_but;
    @BindView(R.id.signup_but)View signup_but;
    @BindView(R.id.login_holder)View login_holder;
    @BindView(R.id.user_holder)View user_holder;
    @BindView(R.id.notify_switch)Switch notify_switch;
    @BindView(R.id.notify_switch_tv)TextView notify_switch_tv;

    MainActivity mainActivity;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser&&email_tv!=null){
            setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setup();
    }

    private void setup(){
        if(userModel!=null){
            email_tv.setText(userModel.getEmail());
        }

        login_holder.setVisibility(userModel==null?View.VISIBLE:View.GONE);
        user_holder.setVisibility(userModel==null?View.GONE:View.VISIBLE);
        logout_but.setVisibility(userModel==null?View.GONE:View.VISIBLE);

        login_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });
        signup_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, SignupActivity.class));
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_profile, container, false);
        ButterKnife.bind(this,rootView);

        user_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!userModel.isAdminItem())return false;

                if(!settingsHasLoaded){
                    Toast("Settings has not loaded");
                    return false;
                }

                new MaterialDialog.Builder(context)
                        .items("Package Name","Email","Website",
                                "Enable Admin","Add Admin User","New Update","Set Max Pending")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int p, CharSequence text) {
                                if(p==0){
                                    updateAppShit(PACKAGE_NAME);
                                }
                                if(p==1){
                                    updateAppShit(EMAIL);
                                }
                                if(p==2){
                                    updateAppShit(WEBSITE);
                                }
                                if(p==3){
                                    isAdmin = true;
                                }
                                if(p==4){
                                    if(!userModel.getEmail().equals("johnebere58@gmail.com")){
                                        Toast("Not Allowed");
                                        return;
                                    }
                                    new MaterialDialog.Builder(context)
                                            .title("Admin User")
                                            .input("Enter Email", "", false, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    addAdminUser(input.toString().trim());
                                                }
                                            })
                                            .show();
                                }
                                if(p==5){
                                    sendUpdate();
                                }
                                if(p==6){
                                    updateAppShitInt(MAX_PENDING_AMOUNT);
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

        boolean canNotify = getBooleanSettings(context,PUSH_NOTIFICATION);
        notify_switch.setChecked(canNotify);
        notify_switch_tv.setText(canNotify?"Enabled":"Disabled");
        notify_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notify_switch_tv.setText(b?"Enabled":"Disabled");
                putSettings(context,PUSH_NOTIFICATION,b);
            }
        });

        return rootView;
    }

    private void sendUpdate(){
        new MaterialDialog.Builder(context)
                .title("Version Code")
                .input("Version Code", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String text = input.toString().trim();
                        final int code = Integer.valueOf(text);
                        new MaterialDialog.Builder(context)
                                .title("Features")
                                .input("Use (-) to separate","", true, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        final String feature = input.toString().trim();
                                        mainActivity.showYesNoDialog("Should the update be compulsory", new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                mainActivity.createUpdate(feature, true, code);
                                            }
                                        }, new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                mainActivity.createUpdate(feature, false, code);
                                            }
                                        });
                                    }
                                })
                                .show();
                    }
                })
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .show();
    }

    private void addAdminUser(String email){
        new BaseModel().getObject(FirebaseFirestore.getInstance().collection(USER_BASE)
                .whereEqualTo(EMAIL, email), new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                if(error!=null){
                    Toast(error);
                    return;
                }
                BaseModel model = (BaseModel) result;
                model.put(IS_ADMIN,true);
                model.updateItem();
                Toast("Updated");
            }
        });
    }

    private void updateAppShitInt(final String key){
        String val = appSettingsModel==null?"":String.valueOf(appSettingsModel.getInt(key));
        new MaterialDialog.Builder(context)
                .input("Enter "+key, val, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        appSettingsModel.put(key,Integer.parseInt(input.toString().trim()));
                        appSettingsModel.updateItem(APP_SETTINGS_BASE,APP_SETTINGS, null);
                    }
                })
                .show();
    }
    private void updateAppShit(final String key){
        String val = appSettingsModel==null?"":appSettingsModel.getString(key);
        new MaterialDialog.Builder(context)
                .input("Enter "+key, val, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        appSettingsModel.put(key,input.toString().trim());
                        appSettingsModel.updateItem(APP_SETTINGS_BASE,APP_SETTINGS,null);
                    }
                })
                .show();
    }


}
