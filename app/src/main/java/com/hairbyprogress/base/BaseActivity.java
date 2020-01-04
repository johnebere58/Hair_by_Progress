package com.hairbyprogress.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.hairbyprogress.Intro;
import com.hairbyprogress.LoginActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.PreInit;
import com.hairbyprogress.R;
import com.hairbyprogress.SearchMain;
import com.hairbyprogress.ViewImageActivity;
import com.hairbyprogress.recyclerview.MyRecyclerView;
import com.hairbyprogress.recyclerview.loadmore.BaseLoadMoreView;
import com.hairbyprogress.recyclerview.loadmore.DemoLoadMoreView;
import com.hairbyprogress.services.AppSettingsService;
import com.hairbyprogress.utils.FileSizeUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import cn.jzvd.Jzvd;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static com.hairbyprogress.MyApplication.AppIsVisible;
import static com.hairbyprogress.MyApplication.appSettingsModel;
import static com.hairbyprogress.MyApplication.currentlyUploadingOrDownloading;
import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.MyApplication.prices;
import static com.hairbyprogress.MyApplication.userModel;

public class BaseActivity extends Base {

    public Context context;
    public Activity activity;
    protected FirebaseFirestore db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        setSavePhotoPath();
        setSaveFilePath();

        MobileAds.initialize(this, "ca-app-pub-2656853745499155~8033123605");
    }

    private void setSavePhotoPath(){
        File my_folder = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_PATH_SAVE_PHOTO);
        if(!my_folder.exists()) {
            my_folder.mkdirs();
        }
        SAVE_PHOTO_FILE_PATH = my_folder.getAbsolutePath();

    }
    private void setSaveFilePath(){
        File my_folder = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_FILE_PATH);
        if(!my_folder.exists()) {
            my_folder.mkdirs();
        }
        SAVE_FILE_PATH = my_folder.getAbsolutePath();

    }


    public void removeFromHomeList(ArrayList<BaseModel> list,BaseModel bm){
        for(int i=0;i<list.size();i++){
            BaseModel model = home_display.get(i);
            if(model.getObjectId().equals(bm.getObjectId())){
                home_display.remove(i);
                return;
            }
        }
    }

    public void setUpRecycleView(final MyRecyclerView recycleView, final RecyclerView.LayoutManager layoutManager,
                                 MyRecyclerView.PagingableListener pagingableListener,
                                 SwipeRefreshLayout.OnRefreshListener refreshListener){
        final boolean swipable = pagingableListener!=null;
        recycleView.setSwipeEnable(swipable);//open swipe

        DemoLoadMoreView loadMoreView = new DemoLoadMoreView(context, recycleView.getRecyclerView());
        loadMoreView.setLoadMorePadding(100);
        loadMoreView.setLoadmoreString("");
        recycleView.setLayoutManager(layoutManager);
        recycleView.setPagingableListener(pagingableListener);
        recycleView.setOnRefreshListener(refreshListener);

        recycleView.setLoadMoreFooter(loadMoreView);

        recycleView.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!swipable){
                    return;
                }
                if(layoutManager instanceof LinearLayoutManager) {
                    if ((((LinearLayoutManager)layoutManager).findFirstCompletelyVisibleItemPosition() < 1)) {
                        recycleView.setSwipeEnable(true);
                    } else {
                        recycleView.setSwipeEnable(false);
                    }
                }
                if(layoutManager instanceof GridLayoutManager) {
                    if ((((GridLayoutManager)layoutManager).findFirstCompletelyVisibleItemPosition() < 1)) {
                        recycleView.setSwipeEnable(true);
                    } else {
                        recycleView.setSwipeEnable(false);
                    }
                }

            }
        });



        recycleView.getLoadMoreFooter().setOnDrawListener(new BaseLoadMoreView.OnDrawListener() {
            @Override
            public boolean onDrawLoadMore(Canvas c, RecyclerView parent) {
                Log.i("onDrawLoadMore","draw load more");
                return false;
            }
        });



        recycleView.onFinishLoading(true, false);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void ToastError(){
        Toast("An error occurred, try again");
    }

    public void Toast(Object text){
        Toast.makeText(context, String.valueOf(text), Toast.LENGTH_SHORT).show();
    }
    public static void Toast(Context context, Object text){
        Toast.makeText(context, String.valueOf(text), Toast.LENGTH_SHORT).show();
    }

    public static SpannableString spanStringForeground(CharSequence s, int color) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ForegroundColorSpan(color), 0, s.length(), 33);
        return dname;
    }

    public static SpannableString spanStringForegroundandBold(CharSequence s, int color) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ForegroundColorSpan(color), 0, s.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        dname = spanStringTypeface(dname, Typeface.BOLD);
        return dname;
    }

    public static SpannableString spanStringTypeface(CharSequence s, int typeface) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new StyleSpan(typeface), 0, s.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return dname;
    }

    public static SpannableString spanStringClickable(CharSequence s, final View.OnClickListener onClickListener) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if(onClickListener!=null)onClickListener.onClick(view);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, 0, dname.length(), 33);
        return dname;
    }
    public static SpannableString spanStringClickable(CharSequence s, final View.OnClickListener onClickListener, final int linkColor) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(true);
                ds.linkColor = linkColor;
            }
        }, 0, dname.length(), 33);
        return dname;
    }


    public static SpannableString spanStringClickableBold(CharSequence s, final View.OnClickListener onClickListener,final int linkColor,final boolean underline) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(underline);
                ds.linkColor = linkColor;
            }
        }, 0, dname.length(), 33);
        dname = spanStringTypeface(dname, Typeface.BOLD);
        return dname;
    }
    public void visitSite(View v){
        Context context = v==null?this.context:getReqActivity(v);
        if(context==null)return;

        String web = appSettingsModel.getString(WEBSITE);
        Uri uri = Uri.parse(web);
        Intent goToSite = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(goToSite);
        } catch (ActivityNotFoundException e) {
            Toast("Could not launch browser");
        }
    }

    public static SpannableString spanStringClickableBold(CharSequence s, ClickableSpan clickableSpan) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(clickableSpan, 0, dname.length(), 33);
        dname = spanStringTypeface(dname, Typeface.BOLD);
        return dname;
    }


    public static SpannableString spanStringClickableBoldandForeground(CharSequence s,int color, final View.OnClickListener onClickListener) {
        SpannableString dname = new SpannableString(s);
        dname.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, 0, dname.length(), 33);
        dname = spanStringTypeface(dname, Typeface.BOLD);
        dname.setSpan(new ForegroundColorSpan(color), 0, s.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return dname;
    }



    protected static boolean isEmailValid(String email){
        return (!TextUtils.isEmpty(email)&& Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public void copy(String text){
        ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("id",text);
        clip.setPrimaryClip(clipData);

    }

    public MaterialDialog getLoadingDialog(String title, boolean cancelable){
        return new MaterialDialog.Builder(context)
                .title(title)
                .content("Wait a Moment...")
                .progress(true, 0)
                .canceledOnTouchOutside(cancelable)
                .progressIndeterminateStyle(false)
                .build();
    }


    public static boolean isConnectingToInternet(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    public static Activity getReqActivity(View reqView){
        Context context = reqView.getContext();
        while (context instanceof ContextWrapper){
            if(context instanceof Activity){
                return (Activity) context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    public void hideKeyboard(View v){
        Context context = getReqActivity(v);
        InputMethodManager im = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    public static void hideTheKeyboard(View v){
        Context context = getReqActivity(v);
        InputMethodManager im = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public void hide_keyboard(View v){
        Context context = getReqActivity(v);
        InputMethodManager im = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showErrorDialog(MaterialDialog.SingleButtonCallback retry){
        new MaterialDialog.Builder(context)
                .content("An error occurred, Try again")
                .positiveText("RETRY")
                .canceledOnTouchOutside(false)
                .negativeText("CANCEL")
                .onPositive(retry).show();
    }

    public void showErrorDialog(String content,MaterialDialog.SingleButtonCallback retry){
        new MaterialDialog.Builder(context)
                .content(content)
                .positiveText("RETRY")
                .canceledOnTouchOutside(false)
                .negativeText("CANCEL")
                .onPositive(retry).show();
    }
     public void showMessage(String content,MaterialDialog.SingleButtonCallback onclick,boolean cancelable){
        new MaterialDialog.Builder(context)
                .content(content)
                .positiveText("DISMISS")
                .canceledOnTouchOutside(cancelable)
                .cancelable(cancelable)
                .onPositive(onclick).show();
    }



    public void clickBack(View v){
        hide_keyboard(v);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(Jzvd.backPress()){
            return;
        }
        super.onBackPressed();
    }

    public String getDeviceId(){
        return Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void CopyText(Context context,String text){
        if(text==null)return;

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text",text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
        }
        Toast(context,"Copied");
    }

    public ArrayList<String> getLoadedItemsId(ArrayList<BaseModel> baseModels){
        ArrayList<String> list = new ArrayList<>();
        for(int i=0;i<baseModels.size();i++){
            String id = baseModels.get(i).getString(OBJECT_ID);
            list.add(id);
        }
        return list;
    }

    public Date getNewestTimeFromList(ArrayList<BaseModel> baseModels){
        if(baseModels.isEmpty())return new Date();
        return baseModels.get(0).getCreatedAtDate();
    }

    public Date getOldestTimeFromList(ArrayList<BaseModel> baseModels){
        if(baseModels.isEmpty())return new Date();
         return baseModels.get(baseModels.size()-1).getCreatedAtDate();
    }

    public static void addMyDetailsToModel(HashMap<String, Object> model){
        model.put(USER_ID, MyApplication.userModel.getUserId());
        model.put(AVATAR_POSITION,userModel.getInt(AVATAR_POSITION));
        model.put(USERNAME,userModel.getUserName());
        //model.put(MY_SKILLS,userModel.getList(MY_SKILLS));
    }

    public String formatNumber(int amount){
        DecimalFormat format = new DecimalFormat("#,###,###,###,###");
        return format.format(amount);
    }

    public static String getStringFromList(ArrayList<String> list){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<list.size();i++){
            String text = list.get(i);
            sb.append(text);
            if(i!=list.size()-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static String getStringFromListWithSlash(ArrayList<String> list){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<list.size();i++){
            String text = list.get(i);
            sb.append(text);
            if(i!=list.size()-1){
                sb.append("/");
            }
        }
        return sb.toString();
    }



    public static ArrayList<String> getListFromString(String text){
        String[] parts = text.split(",");
        ArrayList<String> list = new ArrayList<>();
        for(String s:parts){
            list.add(s);
        }
        return list;
    }

    public static void startIntro(Context context,BaseModel... model){
        Intent intent = new Intent(context, Intro.class);

        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> sub_titles = new ArrayList<>();
        ArrayList<Integer> icons = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        //ArrayList<BaseModel> items = (ArrayList<BaseModel>) model.getList(ITEMS);

        for(BaseModel m : model){
            titles.add(m.getString(TITLE));
            sub_titles.add(m.getString(SUB_TITLE));
            icons.add(m.getInt(ICONS));
            colors.add(m.getInt(COLORS));
        }

        intent.putExtra(TITLE,titles);
        intent.putExtra(SUB_TITLE,sub_titles);
        intent.putExtra(ICONS,icons);
        intent.putExtra(COLORS,colors);
        context.startActivity(intent);
    }

    public static boolean isInList(BaseModel model,ArrayList<BaseModel> models){
        for(BaseModel bm:models){
            if(bm.getObjectId().equals(model.getObjectId()))return true;
        }
        return false;
    }

    public static String getCamString(){
        String cam = new String(Character.toChars(128247));
        return cam + "Photo";
    }
    public static String getDocString(String name){
        String cam = new String(Character.toChars(128196));
        return String.format("%s %s",cam,name);
    }

    public static String getRandomId(){
        String code = UUID.randomUUID().toString();
//        String sub = code.substring(0,4);
//        Random rand = new Random();
//        int num = rand.nextInt(90)+10;

        return code;
        //return String.format("%s%s",sub,num);
    }
     public static String getRandomIdShort(){
        String code = UUID.randomUUID().toString();
        String sub = code.substring(0,4);
        Random rand = new Random();
        int num = rand.nextInt(90)+10;

        return String.format("%s%s",sub,num);
    }


    public static String getFileExtension(String name){
        return name.substring(name.lastIndexOf(".")+1,name.length());
    }
    public static String getFileNameWithoutExtension(String name){
        return name.substring(0,name.lastIndexOf("."));
    }

    public void pickFromGallery(View view) {
        if(!isConnectingToInternet()){
            Toast("No Internet Connectivity");
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_SELECT_PICTURE);
    }


    public void showYesNoDialog(String s,MaterialDialog.SingleButtonCallback onPositive){
        new MaterialDialog.Builder(context)
                .content(s)
                .onPositive(onPositive)
                .negativeText(R.string.no)
                .positiveText(R.string.yes)
                .show();
    }
    public void showYesNoDialog(String s,MaterialDialog.SingleButtonCallback onPositive,MaterialDialog.SingleButtonCallback onNegative){
        new MaterialDialog.Builder(context)
                .content(s)
                .onPositive(onPositive)
                .onNegative(onNegative)
                .negativeText(R.string.no)
                .positiveText(R.string.yes)
                .show();
    }


    protected final Drawable getVectorDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public Drawable getMessageBackground(@ColorInt int normalColor, @ColorInt int selectedColor,
                                        @ColorInt int pressedColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{selectedColor, pressedColor, normalColor}
                ));
        return drawable;
    }



    public static String getChatDate(long milli){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d 'AT' h:mm a",Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milli);

        long time = calendar.getTime().getTime();
        long now = System.currentTimeMillis();
        long today = getTodayMilli();
        long yesterday = today-86400000;
        if(time>today){
            return "TODAY";
        }
        else if(time>yesterday&&time<today){
            return "YESTERDAY";
        }

        return formatter.format(calendar.getTime());
    }

    public static long getTodayMilli() {
        Calendar calendar = Calendar.getInstance();
        GregorianCalendar cal = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

    public static boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date(time1));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date(time2));
        return isSameDay(cal1, cal2);
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static String getChatDate1(long milli){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d",Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milli);
        return formatter.format(calendar.getTime());
    }


    public static String getChatTime(long milli){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a",Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milli);
        return formatter.format(calendar.getTime());
    }

    public static boolean isVideo(Context context, String ext){
        if(ext.contains("."))ext = ext.replace(".","");
        String[] s = context.getResources().getStringArray(R.array.video_formats);
        List<String> list = Arrays.asList(s);
        return list.contains(ext);
    }
    public static boolean isAudio(Context context, String ext){
        if((ext==null)||ext.isEmpty())return false;

        if(ext.contains("."))ext = ext.replace(".","");
        String[] s = context.getResources().getStringArray(R.array.audio_formats);
        List<String> list = Arrays.asList(s);
        return list.contains(ext);
    }

    public static String getFileNameFromModel(BaseModel model){
        return String.format("%s(%s).%s",model.getString(FILE_NAME),model.getObjectId(),model.getString(FILE_EXTENSION));
    }

    public static boolean fileExist(BaseModel model){
      return getFileFromModel(model).exists();
    }

    public static File getFileFromModel(BaseModel model){
        return new File(SAVE_FILE_PATH,getFileNameFromModel(model));
    }
    public static File getMainFile(BaseModel model){
        return new File(model.getString(FILE_ORIGINAL_PATH));
    }


    public static void openFile(final Context context, File url) {

        if(url==null)return;
        try {

            Uri uri = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName()+
                    ".my.package.name.provider",url);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(uri, "application/x-wav");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append("No application found which can open the file\n")
                    .append(spanStringForeground(url.getAbsolutePath(),context.getResources().getColor(R.color.blue0)));
            Toast(context,e.getMessage());
        }
    }


    public static void downloadFile(Context context,String fileName,String filePath,String fileUrl){
        currentlyUploadingOrDownloading.add(fileName);
        Intent intent = new Intent(BROADCAST_UPLOAD_OR_DOWNLOAD);
        intent.putExtra(ACTION,ACTION_DOWNLOAD);
        intent.putExtra(FILE_URL,fileUrl);
        intent.putExtra(FILE_NAME,fileName);
        intent.putExtra(FILE_PATH,filePath);
        context.sendBroadcast(intent);
    }
    public static void uploadFile(Context context,String objectId,String dBase,String name,String filePath){
        currentlyUploadingOrDownloading.add(objectId);
        Intent intent = new Intent(BROADCAST_UPLOAD_OR_DOWNLOAD);
        intent.putExtra(ACTION,ACTION_UPLOAD);

        ArrayList<String> path = new ArrayList<>();
        path.add(filePath);

        intent.putExtra(FILE_PATH,path);
        intent.putExtra(ISLIST,false);
        intent.putExtra(OBJECT_ID,objectId);
        intent.putExtra(DATABASE_NAME,dBase);
        intent.putExtra(NAME,name);
        context.sendBroadcast(intent);
    }



    public static void setFormatIconAndText(Context context, ImageView icon, String extension){
        extension = extension.toLowerCase();
        if(isVideo(context,extension)){
            icon.setImageResource(R.drawable.icon_file_video);
        }else if(isAudio(context,extension)){
            icon.setImageResource(R.drawable.icon_file_audio);
        }else if(extension.contains("doc")){
            icon.setImageResource(R.drawable.icon_file_doc);
        }else if(extension.contains("pdf")){
            icon.setImageResource(R.drawable.icon_file_pdf);
        }else if(extension.contains("xls")){
            icon.setImageResource(R.drawable.icon_file_xls);
        }else if(extension.contains("ppt")){
            icon.setImageResource(R.drawable.icon_file_ppt);
        }else if(extension.contains("zip")||extension.contains("rar")){
            icon.setImageResource(R.drawable.icon_file_zip);
        }else if(extension.contains("xml")) {
            icon.setImageResource(R.drawable.icon_file_xml);
        }else if(extension.contains("txt")){
            icon.setImageResource(R.drawable.icon_file_text);
        }else if(extension.contains("png")||extension.contains("jpg")||extension.contains("jpeg")){
            icon.setImageResource(R.drawable.icon_file_photo);
        }else{
            icon.setImageResource(R.drawable.icon_file_unknown);
        }
    }


    public static boolean tooLarge(String size){
        if(size.contains("GB")){
            return true;
        }
        if(size.contains("MB")){
            String a = size.substring(0,size.contains(".")?size.indexOf("."):size.length());
            int b = Integer.valueOf(a);
            return b>20;

        }

        return false;
    }

    protected String getFileSize(String filePath){
        File file = new File(filePath.trim());
        long length = file.length();

        final StringBuilder details1Text = new StringBuilder();

        FileSizeUtils.FileSizeDivider divider = FileSizeUtils.getFileSizeDivider(length);

        BigDecimal dividedLength = new BigDecimal(length).divide(divider.div, 2, BigDecimal.ROUND_CEILING);

        NumberFormat numberFormat = NumberFormat.getInstance();
        details1Text.append(numberFormat.format(dividedLength.doubleValue()));
        details1Text.append(divider.unitText);
        return details1Text.toString().trim();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
        AppIsVisible = false;
       }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyApplication.notificationManager!=null){
            MyApplication.notificationManager.cancelAll();
        }
        AppIsVisible=true;
    }


    public void startImageViewer(int position,ArrayList<String> imgs){
        Intent intent = new Intent(context,ViewImageActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(IMAGES, imgs);
        startActivity(intent);
    }

    public AlphaAnimation glowAnimation(){
        AlphaAnimation an = new AlphaAnimation(1,0);
        an.setDuration(3000);
        an.setInterpolator(new LinearInterpolator());
        an.setRepeatCount(Animation.INFINITE);
        an.setRepeatMode(Animation.REVERSE);
        return an;
    }

    public static BaseModel getDummyUser(String username,int gender,int avatar){
        String uId = getRandomId();
        BaseModel model = new BaseModel();
        model.put(USERNAME,username);
        model.put(USER_ID,uId);
        model.put(EMAIL,"");
        model.put(PASSWORD,"");
        model.put(MY_CODE, "");
        model.put(GENDER,gender);
        model.put(AVATAR_POSITION,avatar);
        model.put(DEVICE_ID,"");
        model.put(CODE_ENABLED,false);
        model.put(PUBLIC_STORIES,false);
        model.put(PUSH_NOTIFICATION,false);
        model.put(PRIVATE_CHAT,false);
        model.put(IS_ADMIN,false);
        model.put(IS_DUMMY,true);
        return model;
    }

    public void showInfo(int type){
        String web = appSettingsModel.getString(WEBSITE);
        if(web.isEmpty())return;
        Uri uri = Uri.parse(String.format("%s/%s",web,type==TYPE_POLICY?"privacy.pdf":type==TYPE_ABOUT?"about.pdf":"terms.pdf"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try{
        startActivity(intent);}catch (Exception ignored){}
    }
    public static void showInfo(Context context,int type){
        String web = appSettingsModel.getString(WEBSITE);
        if(web.isEmpty()){
            web = DEFAULT_WEBSITE;
        }
        Uri uri = Uri.parse(String.format("%s/%s",web,type==TYPE_POLICY?"privacy.pdf":type==TYPE_ABOUT?"about.pdf":"terms.pdf"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try{
        context.startActivity(intent);}catch (Exception ignored){}
    }

    public void showInfo(View view){
        String tag = view.getTag().toString();
        int type = Integer.parseInt(tag);
        showInfo(type);
    }

    public void shareApp(View v){
        Context context = v==null?this.context:getReqActivity(v);
        if(context==null)return;

        String pack = appSettingsModel.getString(PACKAGE_NAME);
        String web = appSettingsModel.getString(WEBSITE);
        String packageName = pack.isEmpty()? context.getPackageName() : pack;

        StringBuilder sb = new StringBuilder();
        sb.append("Hair by Progress!\n")
                .append("The best place to buy quality hair online.\n\n")
                .append("Follow this link\n")
                .append(String.format("https://play.google.com/store/apps/details?id=%s",packageName))
                .append("\n\n")
                .append("Or through the website\n")
                .append(web);


        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT,"Hair by Progress");
        share.putExtra(Intent.EXTRA_TEXT,sb.toString());
        startActivity(Intent.createChooser(share,"Share App"));
    }

    public void rateApp(View v){
        Context context = v==null?this.context:getReqActivity(v);
        if(context==null)return;

        String pack = appSettingsModel.getString(PACKAGE_NAME);
        String packageName = pack.isEmpty()? context.getPackageName() : pack;
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                //Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        if(userModel!=null) {
            userModel.put(HAVE_RATED, true);
            userModel.updateItem();
        }
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public void checkUpdate(View v){
        int version = getVersion();
        BaseModel newVersion = new BaseModel(appSettingsModel.getMap(NEW_UPDATE));

        if(version>=newVersion.getInt(VERSION_CODE)){
            Toast("Your App is up to date");
        }else{
            rateApp(v);
        }
    }



    public String chatExists(BaseModel theUser){
        String theUserId = theUser.getUserId();
        ArrayList<String> myChats = (ArrayList<String>) MyApplication.userModel.getList(MY_CHATS);
        ArrayList<String> hisChat = (ArrayList<String>) theUser.getList(MY_CHATS);
        for(String chat:myChats){
            if(chat.contains(theUserId))return chat;
        }
        for(String chat:hisChat){
            if(chat.contains(MyApplication.userModel.getUserId()))return chat;
        }

        return null;
    }

    public int getVersion(){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void sendFeedBack(View v){
        String email = appSettingsModel.getString(EMAIL);
        Intent in = new Intent(Intent.ACTION_SEND);
        in.setType("plain/text");
        //in.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
        in.putExtra(Intent.EXTRA_EMAIL,new String[]{email});
        in.putExtra(Intent.EXTRA_SUBJECT,"My Feedback");
        in.putExtra(Intent.EXTRA_TEXT,"Hi Team HBP,\nYour App is Great and I will like to send you a feedback\n");
        try {
            // startActivity(in);
            startActivity(Intent.createChooser(in,"Send FeedBack"));
        }catch (Exception ignored){

        }
    }
    public void emailUs(View v){
        String email = appSettingsModel.getString(EMAIL);
        Intent in = new Intent(Intent.ACTION_SEND);
        in.setType("plain/text");
        //in.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
        in.putExtra(Intent.EXTRA_EMAIL,new String[]{email});
       // in.putExtra(Intent.EXTRA_SUBJECT,"My Feedback");
        //in.putExtra(Intent.EXTRA_TEXT,"My Feedback");
       // in.putExtra(Intent.EXTRA_TEXT,"Hi Team Maugost,\nYour App is Great and I will like to send you a feedback\n");
        try {
            // startActivity(in);
            startActivity(Intent.createChooser(in,"Email Us"));
        }catch (Exception ignored){

        }
    }

    public void ToastNoInternet(){
        Toast(getString(R.string.no_internet));
    }

    public void logOut(View v){
        if(!isConnectingToInternet()){
            ToastNoInternet();
            return;
        }
        showYesNoDialog("Are you sure you want to log out?", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                final MaterialDialog md = getLoadingDialog("Logging out",false);
                md.show();
                userModel = null;
                stopService(new Intent(context,AppSettingsService.class));
                FirebaseAuth.getInstance().signOut();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        md.dismiss();
                        startActivity(new Intent(context,PreInit.class));
                        finish();
                    }
                },5000);
            }
        });

    }

    public View.OnClickListener addToCartListener(final BaseModel bm){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userModel==null){
                    activity.startActivity(new Intent(context, LoginActivity.class));
                    //Toast("Login First");
                    return;
                }

                if(!isConnectingToInternet()){
                    ToastNoInternet();
                    return;
                }

                /*if(userModel.getList(MY_CART_ITEMS).contains(bm.getObjectId())){
                    Toast("Item already in cart");
                    return;
                }*/

                new MaterialDialog.Builder(context)
                        .title("Quantity")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("What quantity?", "1", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                int q = Integer.valueOf(input.toString().trim());

                                String id = getRandomId();
                                bm.put(USER_ID,userModel.getUserId());
                                bm.put(QUANTITY,q);
                                bm.put(MAIN_OBJECT_ID,bm.getObjectId());
                                bm.saveItem(CART_BASE,id);

                                userModel.addToList(MY_CART_ITEMS,id,true);
                                userModel.updateItem();
                                sendBroadcast(new Intent(BROADCAST_CART_UPDATED));

                                Toast("Added");
                            }
                        })
                        .positiveText("ADD")
                        .negativeText("CANCEL").show();

            }
        };
    }

    public static String encryptCode(String text){

        StringBuilder sb =new StringBuilder();
        for(int i=0;i<text.length();i++){
            char c = text.charAt(i);
            sb.append(getReplacement(String.valueOf(c)));
            sb.append(0);
        }

        return sb.toString();
    }

    public static String decryptCode(String text){

        String parts[] = text.split("0");
        StringBuilder sb = new StringBuilder();

        for(String s :parts){
            sb.append(getReverseReplacement(s));
        }


        return sb.toString();
    }

    public static String getReplacement(String s){
        String letters = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        if(s.equals("7"))return "77";
        if(s.equals("J"))return "J";
        if(s.equals("T"))return "T";
        if(s.equals("d"))return "d";
        if(s.equals("n"))return "n";
        if(s.equals("x"))return "x";
        if(s.equals(" "))return " ";

        int index = letters.indexOf(s);
        if(index==-1)return s;

        return String.valueOf(index);
    }

    public static String getReverseReplacement(String s){
        String letters = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        if(s.equals("77"))return "7";
        if(s.equals("J"))return "J";
        if(s.equals("T"))return "T";
        if(s.equals("d"))return "d";
        if(s.equals("n"))return "n";
        if(s.equals("x"))return "x";
        if(s.equals(" "))return " ";

        String text = "";

        try{
            int index = Integer.parseInt(s);
            char c = letters.charAt(index);
            text = String.valueOf(c);
        }catch(Exception e){
            text = s;
        }

        return text;
    }

    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void showPasswordDialog(final OnComplete onComplete){
        new MaterialDialog.Builder(context)
                .title("Password")
                .input("Enter your current password", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(input.toString().equals(userModel.getString(PASSWORD))){
                            onComplete.onComplete(null,null);
                        }else{
                            Toast("Password Incorrect");
                            onComplete.onComplete("",null);
                        }
                    }
                })
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .positiveText("CONTINUE")
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onComplete.onComplete("",null);
                    }
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    /*public void createNews(String text,String clickText,String clickLink){
        BaseModel model = new BaseModel();
        model.put(TIME,System.currentTimeMillis());
        model.put(OBJECT_ID,getRandomId());
        model.put(MESSAGE,text);
        model.put(NEWS_BUT_TEXT,clickText);
        model.put(NEWS_LINK,clickLink);
        appSettingsModel.put(NEWS,model.items);
        appSettingsModel.updateItem();
        Toast("Created");
    }*/
    /*public void createNotify(String title,String message,String tag,ArrayList<String> images){
        BaseModel model = new BaseModel();
        model.put(TIME,System.currentTimeMillis());
        model.put(OBJECT_ID,getRandomId());
        model.put(MESSAGE,text);
        appSettingsModel.put(NEW_NOTIFICATION,model.items);
        appSettingsModel.updateItem();
        Toast("Created");
    }*/

    public void createUpdate(String feature,boolean must,int code){
        BaseModel model = new BaseModel();
        model.put(NEW_FEATURE,feature);
        model.put(MUST_UPDATE,must);
        model.put(VERSION_CODE,code);
        appSettingsModel.put(NEW_UPDATE,model.items);
        appSettingsModel.updateItem(APP_SETTINGS_BASE,APP_SETTINGS, null);
        Toast("Created");
    }

    public static int getTheColor(Context context,int position){
        int colors[] = {R.color.blue0,
                        R.color.red0,
                        R.color.orange0,
                        R.color.pink0,
                        R.color.m_blue03,
                        R.color.green,
                        R.color.light_green0,
                        R.color.brown0,
                        R.color.blue3,
                        R.color.yellow0,
                        R.color.blue6,
                        R.color.pink03,
                        R.color.dark_green03,
                          };
       int size = colors.length;
       position = position>size-1?position%size : position;
       return context.getResources().getColor(colors[position]);
    }

    public String getContentFromDescription(BaseModel model,String title){
        ArrayList<HashMap<String,Object>> map = (ArrayList<HashMap<String, Object>>) model.getList(DESCRIPTION);
        for(HashMap<String,Object> m: map){
            BaseModel bm = new BaseModel(m);
            if(title.equals(bm.getString(TITLE))){
                return bm.getString(CONTENT);
            }
        }
        return "";
    }

    public void startSearch(String key){
        Intent intent = new Intent(context, SearchMain.class);
        intent.putExtra(SEARCH_KEY,key);
        startActivity(intent);
    }
    public String getOneItemFromDescription(BaseModel model){
        ArrayList<HashMap<String,Object>> map = (ArrayList<HashMap<String, Object>>) model.getList(DESCRIPTION);
        for(HashMap<String,Object> m: map){
            BaseModel bm = new BaseModel(m);
            return bm.getString(CONTENT);
        }
        return "";
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



    public static long getLastSeenStory(Context context){
        SharedPreferences shed = context.getSharedPreferences(SETTINGS_PREF,0);
        return shed.getLong(LAST_SEEN_STORY,0);
    }

    public String string(int res){
        return getString(res);
    }

    public void addOnceToList(ArrayList<BaseModel> list, BaseModel item){
        for(int i=0;i<list.size();i++){
            BaseModel bm = list.get(i);
            if(item.getObjectId().equals(bm.getObjectId())){
                    list.set(i, item);
                return;
            }
        }
        list.add(getPositionToInsert(list,item),item);
    }

    public int getPositionToInsert(ArrayList<BaseModel> list,BaseModel item){
        int position = list.size();

        for (int i = 0; i < list.size(); i++) {
            long time0 = list.get(i).getTime();
            long time1 = item.getTime();
            if(time1>time0){
                position = i;
                break;
            }
        }

        return position;
    }

    public void removeFromList(ArrayList<BaseModel> list, String id){
        for(int i=0;i<list.size();i++){
            BaseModel bm = list.get(i);
            if(id.equals(bm.getObjectId())){
                    list.remove(i);
                return;
            }
        }
    }


    /*public boolean addOnceToList(ArrayList<BaseModel> list, BaseModel item, int position){
        for(BaseModel bm:list){
            if(item.getObjectId().equals(bm.getObjectId()))return false;
        }
        list.add(position,item);
        return true;
    }*/

    public boolean addOnceToListByReplacing(ArrayList<BaseModel> list, BaseModel item){
        for(int i=0;i<list.size();i++){
            BaseModel bm = list.get(i);
            if(item.getObjectId().equals(bm.getObjectId())){
                list.set(i,item);
                return true;
            }
        }
        list.add(item);
        return true;
    }


    public String convertListToString(String divider,ArrayList<String> list){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<list.size();i++){
            String s = list.get(i);
            sb.append(s);
            if(i!=list.size()-1)sb.append(divider);
            sb.append(" ");
        }

        return sb.toString().trim();

    }

    public ArrayList<String> convertStringToList(String divider,String text){
        ArrayList<String> list = new ArrayList<>();
        String parts[] = text.split(divider);
        for(String s:parts){
            list.add(s.trim());
        }
        return list;
    }

    public ArrayList<String> getFromSections(String key){
        ArrayList<String> contents = new ArrayList<>();
        if(!MyApplication.sections.isEmpty()){
            for(BaseModel bm: MyApplication.sections){
                if(bm.getString(Base.TITLE).equalsIgnoreCase(key)){
                    ArrayList<String> title = (ArrayList<String>) bm.getList(Base.CONTENT);
                    contents.addAll(title);
                    break;
                }
                //5492057010
            }
        }
        return contents;
    }

    public String getFromTextSections(String key){
        if(!MyApplication.sections.isEmpty()){
            for(BaseModel bm: MyApplication.sections){
                if(bm.getString(Base.TITLE).equalsIgnoreCase(key)){
                    return bm.getString(CONTENT);
                }
            }
        }
        return "";
    }

    public String getFromMapSections(String key,String subKey){
        if(!MyApplication.sections.isEmpty()){
            for(BaseModel bm: MyApplication.sections){
                if(bm.getString(Base.TITLE).equalsIgnoreCase(key)){
                    HashMap<String,Object> map = (HashMap<String, Object>) bm.getMap(CONTENT);
                    BaseModel model = new BaseModel(map);
                    return model.getString(subKey);
                }
            }
        }
        return "";
    }



    public String makeFirstUpper(String text){
        return text.substring(0,1).toUpperCase()+text.substring(1);
    }

    public void callPhone(String num){
        String url = String.format("tel:%s", num);
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast("No permission set");
            return;
        }
        startActivity(in);
    }

    public Locale getLocale() {
        Configuration config = getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }

    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config){
        return config.getLocales().get(0);
    }

    public ArrayList<String> getPriceModels(){
        ArrayList<String> list = new ArrayList<>();
        String[] sections = {HAIR_PRICE_STRAIGHT,
                HAIR_PRICE_CURLY,
                HAIR_PRICE_CLOSURE,
                HAIR_PRICE_180_FRONTAL,
                HAIR_PRICE_360_FRONTAL,
                HAIR_PRICE_STRAIGHT_FRONTAL_LACE_WIG,
                HAIR_PRICE_CURLY_FRONTAL_LACE_WIG,
                HAIR_PRICE_STRAIGHT_FULL_LACE_WIG,
                HAIR_PRICE_CURLY_FULL_LACE_WIG,
                HAIR_PRICE_BOB_FRONTAL_LACE_WIG,
                HAIR_PRICE_BOB_FULL_LACE_WIG};
        /*for(BaseModel bm:prices){
            list.add(bm.getString(TITLE));
        }*/
        list.addAll(Arrays.asList(sections));
        return list;
    }

    public int getPriceFromPriceModel(String key,String content){
        for(BaseModel bm:MyApplication.prices){
            if(bm.getString(TITLE).equals(key)){
                String p = new BaseModel(bm.getMap(CONTENT)).getString(content);
                return p.isEmpty()?0:Integer.valueOf(p);
            }
        }
        return 0;
    }
}
