package com.hairbyprogress;

import android.app.NotificationManager;
import android.support.multidex.MultiDexApplication;

import com.hairbyprogress.base.BaseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by John Ebere on 6/6/2018.
 */
public class MyApplication extends MultiDexApplication {

    public static BaseModel dummyAddress;
    public static BaseModel dummyInch;
    public static BaseModel dummyProduct;
    public static BaseModel reuseDummy;
    public static BaseModel dummyModel;
    public static BaseModel userModel;
    public static BaseModel appSettingsModel = new BaseModel();
    //public static BaseModel dummyModel = new BaseModel();
    public static int dummyType;

    public static ArrayList<BaseModel> feedbackList = new ArrayList<>();
    public static ArrayList<BaseModel> order_items = new ArrayList<>();
    public static ArrayList<BaseModel> cart_items = new ArrayList<>();
    public static ArrayList<BaseModel> sections = new ArrayList<>();
    public static ArrayList<BaseModel> prices = new ArrayList<>();
    public static ArrayList<BaseModel> products = new ArrayList<>();
    public static ArrayList<BaseModel> home_display = new ArrayList<>();

    public static ArrayList<String> categories = new ArrayList<>();
    public static ArrayList<String> dummyOrderIds = new ArrayList<>();
    public static ArrayList<String> skills = new ArrayList<>();
    public static ArrayList<String> currentlyUploadingOrDownloading = new ArrayList<>();
    public static boolean isAdmin;
    public static boolean isJohn;
    public static boolean AppIsVisible;
    public static String VISIBLE_CHAT_ID="";

    public static String ID_TO_UPDATE="";

    public static NotificationManager notificationManager;
    public static long pausedTime = 0;

    public static boolean alreadyStarted;
    public static boolean orderHasLoaded;
    public static boolean notificationHasLoaded;
    public static boolean settingsHasLoaded;

    @Override
    public void onCreate() {
        super.onCreate();


        EmojiManager.install(new IosEmojiProvider());
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Nirmala.ttf")
                                    .setFontAttrId(R.attr.fontPath).build());
    }

    public static FirebaseUser getCurrentUser(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser;
    }

    /*public static void logOut(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
    }*/


}
