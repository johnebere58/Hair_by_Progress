package com.hairbyprogress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.adapters.ProductVpTopAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;
import com.vincent.videocompressor.VideoCompress;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jzvd.JzvdStd;
import id.zelory.compressor.Compressor;

import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.prices;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.utils.StringUtil.getPathFromUri;
import static com.tangxiaolv.telegramgallery.GalleryActivity.GALLERY_CONFIG;

public class ProductMain extends BaseActivity {

    @BindView(R.id.preview)JzvdStd preview;
    @BindView(R.id.title_tv)TextView title_tv;
    @BindView(R.id.top_vp)ViewPager top_vp;
    @BindView(R.id.top_vp_tab)SmartTabLayout top_vp_tab;
    @BindView(R.id.write_up_tv)TextView write_up_tv;
    @BindView(R.id.info_holder)LinearLayout info_holder;
    @BindView(R.id.price_tv)TextView price_tv;
    @BindView(R.id.add_but)View add_but;
    @BindView(R.id.preview_holder)View preview_holder;

    ProductVpTopAdapter vpTopAdapter;

    int itemPrice = 0;
    int defLenght = 0;
    double unitPrice = 0;

    boolean modified;

    private ArrayList<String> main_images = new ArrayList<>();

    private ArrayList<String> images_urls = new ArrayList<>();
    private ArrayList<String> image_paths = new ArrayList<>();
    private ArrayList<String> dummy_paths = new ArrayList<>();

    private ArrayList<String> addedTitles = new ArrayList<>();

    //String selectedLength;
    String priceModel = "";
    ArrayList<HashMap<String,Object>> infos;

    String videoUrl;

    MaterialDialog md;

    boolean gramAvailable;

    String videoFileName;
    DownloadReceiver downloadReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_main);
        ButterKnife.bind(this);

        if(dummyProduct==null){
            Toast("No product");
            finish();
            return;
        }

        videoFileName = String.format("%s%s.mp4",dummyProduct.getObjectId(),dummyProduct.getString(VIDEO_URL).hashCode());

        downloadReceiver = new DownloadReceiver();
        registerReceiver(downloadReceiver,new IntentFilter(BROADCAST_DOWNLOAD_COMPLETED));

        md = getLoadingDialog("Uploading",false);

        main_images = (ArrayList<String>) dummyProduct.getList(IMAGES);
        vpTopAdapter = new ProductVpTopAdapter(context, main_images,TYPE_PRODUCT_MAIN);
        vpTopAdapter.setOnComplete(new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                top_vp_tab.setViewPager(top_vp);
            }
        });
        top_vp.setAdapter(vpTopAdapter);
        top_vp_tab.setViewPager(top_vp);

        title_tv.setText(makeFirstUpper(dummyProduct.getString(ITEM_CATEGORY)));
        write_up_tv.setText(dummyProduct.getString(WRITE_UP));

        itemPrice = dummyProduct.getInt(PRICE);

        price_tv.setText(String.format("N%s",formatNumber(itemPrice)));

        String pre = dummyProduct.getString(VIDEO_URL);
        if(!pre.isEmpty()){
            setupVideo(pre,false);
        }

        infos = (ArrayList<HashMap<String, Object>>) dummyProduct.get(DESCRIPTION);
        if(infos!=null){
        for(HashMap<String,Object> map:infos){
            BaseModel model = new BaseModel(map);
            addDescriptionView(model);
        }}

        getFirstUnitPrice();
        //preview.startPlay("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", MxVideoPlayer.SCREEN_LAYOUT_NORMAL,"");
        add_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });

        add_but.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isAdmin)return false;

                new MaterialDialog.Builder(context)
                        .items("ADD INFO","EDIT PRICE","EDIT WRITEUP","ADD PHOTO","UPDATE VIDEO")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                                if(xp==0){
                                    addDescription();
                                }
                                if(xp==1){
                                    new MaterialDialog.Builder(context)
                                            .input("Price", String.valueOf(dummyProduct.getInt(PRICE)), false, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    int price = Integer.valueOf(input.toString());
                                                    dummyProduct.put(PRICE,price);
                                                    dummyProduct.updateItem();
                                                    price_tv.setText(String.format("N%s",formatNumber(price)));
                                                }
                                            })
                                            .inputType(InputType.TYPE_CLASS_NUMBER)
                                            .positiveText("UPDATE")
                                            .show();
                                }
                                if(xp==2){
                                    new MaterialDialog.Builder(context)
                                            .input("Write up", dummyProduct.getString(WRITE_UP), false, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    dummyProduct.put(WRITE_UP,input.toString().trim());
                                                    dummyProduct.updateItem();
                                                    write_up_tv.setText(input.toString().trim());
                                                }
                                            })
                                            .positiveText("UPDATE")
                                            .show();
                                }
                                if(xp==3){
                                    GalleryConfig config = new GalleryConfig.Build()
                                            .singlePhoto(false)
                                            .hintOfPick("You cannot add more than 6 images")
                                            .limitPickPhoto(6-main_images.size())
                                            .filterMimeTypes(new String[]{"image/*"})
                                            .build();

                                    Intent intent = new Intent(activity, GalleryActivity.class);
                                    intent.putExtra(GALLERY_CONFIG, config);
                                    startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                                }
                                if(xp==4){
                                    new MaterialDialog.Builder(context)
                                            .items("Input Url","Select Video")
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                                                    if(xp==0){
                                                        new MaterialDialog.Builder(context)
                                                                .input("Video Url", "", false, new MaterialDialog.InputCallback() {
                                                                            @Override
                                                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                                                videoUrl = input.toString().trim();
                                                                                Toast("Video Added");
                                                                                dummyProduct.put(VIDEO_URL,videoUrl);
                                                                                dummyProduct.updateItem();

                                                                                setupVideo(videoUrl,false);
                                                                            }
                                                                        }
                                                                )
                                                                .show();
                                                    }
                                                    if(xp==1){
                                                        Intent intent = new Intent();
                                                        intent.setType("video/*");
                                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                                        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_SELECT_VIDEO);
                                                    }
                                                }
                                            })
                                            .show();
                                    }
                            }
                        })
                        .show();

                return true;
            }
        });
    }

    private void tryToCompressVideo(String path){
        MaterialDialog mdComp = getLoadingDialog("Compressing Video",false);


        String destPath = SAVE_FILE_PATH + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
        VideoCompress.compressVideoLow(path, destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                mdComp.show();
            }

            @Override
            public void onSuccess() {
                mdComp.dismiss();
                Toast("Success");
                images_urls.clear();
                ArrayList<String> imageList = new ArrayList<>();
                imageList.add(destPath);
                uploadImages(true, imageList, new OnComplete() {
                    @Override
                    public void onComplete(String error, Object result) {
                        md.dismiss();
                        Toast("Video Added");
                        dummyProduct.put(VIDEO_URL,videoUrl);
                        dummyProduct.updateItem();

                        setupVideo(videoUrl,false);

                    }
                });
            }

            @Override
            public void onFail() {
                mdComp.dismiss();
                Toast("Failed");
            }

            @Override
            public void onProgress(float percent) {
                //tv_progress.setText(String.valueOf(percent) + "%");
            }
        });

    }

    private void setupVideo(final String videoUrl,boolean startPlay){

        preview_holder.setVisibility(View.VISIBLE);
        preview.batteryTimeLayout.setVisibility(View.GONE);
        preview.backButton.setVisibility(View.GONE);
        preview_holder.setOnLongClickListener(view -> {
            if(!isAdmin)return false;

            new MaterialDialog.Builder(context)
                    .items("Copy Url","Remove Video")
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                            if(xp==0){
                                copy(videoUrl);
                            }
                            if(xp==1){
                                showYesNoDialog("Are you sure you want to remove this video?", new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        preview_holder.setVisibility(View.GONE);
                                        Toast("Removed");

                                        dummyProduct.remove(VIDEO_URL);
                                        dummyProduct.updateItem();
                                    }
                                });
                            }
                        }
                    })
                    .show();

            return true;
        });

       if(MyApplication.currentlyUploadingOrDownloading.contains(videoFileName)){
           preview.startButton.setVisibility(View.GONE);
           preview.loadingProgressBar.setVisibility(View.VISIBLE);
           return;
       }

       File file = new File(SAVE_FILE_PATH, videoFileName);

       if(file.exists()){
           preview.setUp(file.getAbsolutePath(), "",JzvdStd.SCREEN_WINDOW_NORMAL);
           if(startPlay){
               new Handler()
                       .postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               preview.startButton.performClick();
                           }
                       },100);
           }
       } else{
            preview.startButton.setVisibility(View.VISIBLE);
           preview.loadingProgressBar.setVisibility(View.GONE);
            preview.startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preview.startButton.setVisibility(View.GONE);
                    preview.loadingProgressBar.setVisibility(View.VISIBLE);
                    downloadFile(context,videoFileName,SAVE_FILE_PATH,videoUrl);
                }
            });
        }
    }

    private void addDescription(){
        ArrayList<String> titles= getTitles();
        titles.add(INPUT);
        new MaterialDialog.Builder(context)
                .items(titles)
                .itemsCallback((dialog, itemView, position, text) -> {
                    final String title = text.toString();
                    if(title.equals(INPUT)){
                        new MaterialDialog.Builder(context)
                                .input("Title: Content", "", false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        String parts[] = input.toString().split(":");
                                        if(parts.length!=2){
                                            Toast("Text Error");
                                            return;
                                        }
                                        BaseModel bm = new BaseModel();
                                        bm.put(TITLE,parts[0].trim());
                                        bm.put(CONTENT,parts[1].trim());
                                        addDescriptionView(bm);

                                        infos.add(bm.items);
                                        dummyProduct.put(DESCRIPTION,infos);
                                        dummyProduct.updateItem();
                                    }
                                })
                                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                                .show();
                        return;
                    }

                    new MaterialDialog.Builder(context)
                            .items(getContent(title))
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    final String content = text.toString();

                                    if(title.equalsIgnoreCase(HAIR_LENGTH)){
//                                        selectedLength = content;
//                                        startActivityForResult(new Intent(context,InchMain.class).putExtra(HAIR_LENGTH,selectedLength),199);

                                        new MaterialDialog.Builder(context)
                                                .items(getPriceModels())
                                                .itemsCallback(new MaterialDialog.ListCallback() {
                                                    @Override
                                                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                                        priceModel=text.toString();
                                                        Toast(priceModel);
                                                        //addDescriptionView(HAIR_LENGTH,content,null,TYPE_HAIR_LENGTH);

                                                        BaseModel bm = new BaseModel();
                                                        bm.put(TITLE,HAIR_LENGTH);
                                                        bm.put(CONTENT,content);

                                                        addDescriptionView(bm);
                                                        infos.add(bm.items);
                                                        dummyProduct.put(DESCRIPTION,infos);
                                                        dummyProduct.put(PRICE_MODEL,priceModel);
                                                        dummyProduct.updateItem();
                                                        MyApplication.dummyInch=null;

                                                    }
                                                })
                                                .show();

                                        return;
                                    }

                                    ArrayList<String> options = new ArrayList<>();
                                    try{
                                        int num = Integer.valueOf(content);
                                        options.add(FIXED);
                                        options.add(FLEXIBLE_NUMBER);
                                    }catch (Exception e){
                                        options.add(FIXED);
                                        options.add(FLEXIBLE_TEXT);
                                    };

                                    new MaterialDialog.Builder(context)
                                            .title("TYPE")
                                            .items(options)
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, final int position, CharSequence text) {
                                                    BaseModel bm = new BaseModel();
                                                    bm.put(TITLE,title);
                                                    bm.put(CONTENT,content);
                                                    bm.put(FLEXIBLE_NUMBER,text.equals(FLEXIBLE_NUMBER));
                                                    bm.put(FLEXIBLE_TEXT,text.equals(FLEXIBLE_TEXT));
                                                    addDescriptionView(bm);

                                                    infos.add(bm.items);
                                                    dummyProduct.put(DESCRIPTION,infos);
                                                    dummyProduct.updateItem();
                                                }
                                            })
                                            .show();
                                }
                            })
                            .show();
                })
                .show();
    }

    private ArrayList<String> getContent(String title){
        for(BaseModel bm:MyApplication.sections){
            if(title.equals(bm.getString(TITLE))){
                ArrayList<String> list = new ArrayList<>(bm.getList(CONTENT));
                Collections.sort(list);
                return list;
            }
        }
        return new ArrayList<String>();
    }

    private ArrayList<String> getTitles(){
        ArrayList<String> titles = new ArrayList<>();
        for(BaseModel bm:MyApplication.sections){
            int sectionType = bm.getInt(SECTION_TYPE);
            if(sectionType!=SECTION_TYPE_LIST)continue;

            String title = bm.getString(TITLE);
            if(addedTitles.contains(title))continue;
            titles.add(title);
        }

        Collections.sort(titles);
        return titles;
    }

    private void addToCart(){
        if(userModel==null){
            activity.startActivity(new Intent(context, LoginActivity.class));
            return;
        }

        if(!isConnectingToInternet()){
            ToastNoInternet();
            return;
        }

        final String id = getRandomId();
        /*if(userModel.getList(MY_CART_ITEMS).contains(id)){
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

                        dummyProduct.put(DESCRIPTION,getNewDescription());
                        dummyProduct.put(PRICE,priceFromTv());
                        dummyProduct.put(USER_ID,userModel.getUserId());
                        dummyProduct.put(QUANTITY,q);
                        dummyProduct.put(MAIN_OBJECT_ID,dummyProduct.getObjectId());
                        dummyProduct.saveItem(CART_BASE,id);

                        userModel.addToList(MY_CART_ITEMS,id,true);
                        userModel.updateItem();
                        sendBroadcast(new Intent(BROADCAST_CART_UPDATED));

                        Toast("Added");
                        finish();
                    }
                })
                .positiveText("ADD")
                .negativeText("CANCEL").show();

    }

    private ArrayList<HashMap<String,Object>> getNewDescription(){
        ArrayList<HashMap<String,Object>> list = new ArrayList<>();
        for(int i=0;i<info_holder.getChildCount();i++){
            final View view = info_holder.getChildAt(i);
            BaseModel map = (BaseModel) view.getTag();
            list.add(map.items);
        }
        return list;
    }

    private void addDescriptionView(final BaseModel model){
        final String title = model.getString(TITLE);
        String content = model.getString(CONTENT);
        boolean flexibleNumber = model.getBoolean(FLEXIBLE_NUMBER);
        boolean flexibleText = model.getBoolean(FLEXIBLE_TEXT);

        if(title.equals(HAIR_GRAM)){
            gramAvailable=true;
        }

        if(title.equalsIgnoreCase(HAIR_LENGTH)){
            defLenght = Integer.valueOf(content);
        }

        final View view = View.inflate(context,R.layout.description_item,null);

        view.setTag(model);

        View main_layout = view.findViewById(R.id.main_layout);
        TextView title_tv = view.findViewById(R.id.title_tv);
        final TextView text_tv = view.findViewById(R.id.text_tv);
        TextView but = view.findViewById(R.id.but);

        main_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View vx) {
                new MaterialDialog.Builder(context)
                        .items(getString(R.string.delete))
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                addedTitles.remove(title);
                                infos.remove(model.items);
                                dummyProduct.put(DESCRIPTION,infos);
                                dummyProduct.updateItem();
                                info_holder.removeView(view);
                            }
                        })
                        .show();
                return true;
            }
        });

        title_tv.setText(title);
        text_tv.setText(content);

        if(flexibleNumber || flexibleText){
            but.setVisibility(View.VISIBLE);
            but.setText("CHANGE");
            but.setTextColor(getResources().getColor(R.color.blue0));
            but.setOnClickListener(v -> new MaterialDialog.Builder(context)
                    .items(getFromSections(title))
                    .itemsCallback((dialog, itemView, position, text) -> {
                        text_tv.setText(text);
                        calPrice();
                        modified = true;
                    })
                    .show());
        }
        else if(title.equalsIgnoreCase(HAIR_LENGTH)){
            but.setVisibility(View.VISIBLE);
            but.setText("CHANGE");
            but.setTextColor(getResources().getColor(R.color.blue0));
            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //final HashMap<String,Object> map = (HashMap<String, Object>) model.getMap(THE_MAP);

                    ArrayList<String> inches = getLenghtList(dummyProduct.getString(PRICE_MODEL));
                    //if(inches.length==0)return;

                    new MaterialDialog.Builder(context)
                            .items(inches)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    text_tv.setText(text);

                                    //final BaseModel bm = new BaseModel(map);
                                    //String theP = bm.getString(text.toString());

                                    int thePrice = getPriceFromPriceModel(dummyProduct.getString(PRICE_MODEL),text.toString());
                                    unitPrice = gramAvailable?
                                            (double)thePrice/100/*Double.valueOf(getContentFromDescription(dummyProduct,HAIR_GRAM))*/
                                            :(double)thePrice;
                                    calPrice();
                                    modified = true;
                                }
                            })
                            .show();
                }
            });
        }
        addedTitles.add(title);

        info_holder.addView(view);
    }

    private int priceFromTv(){
        String p = price_tv.getText().toString();
        p = p.replace(",","");
        p = p.replace("N","");
        return Integer.valueOf(p.trim());
    }

    private void getFirstUnitPrice(){
        double unit = itemPrice;
        for(int i=0;i<info_holder.getChildCount();i++){
            View view = info_holder.getChildAt(i);
            TextView text_tv = view.findViewById(R.id.text_tv);
            BaseModel model = (BaseModel) view.getTag();
            if(model.getString(TITLE).equals(HAIR_BUNDLES)||model.getString(TITLE).equals(HAIR_GRAM)){
                int content = Integer.valueOf(text_tv.getText().toString().trim());
                unit = unit/(double)content;
            }
        }
        unitPrice = unit;
//        Toast(String.valueOf(itemPrice));
//        Toast(String.valueOf(unitPrice));
    }

    private void calPrice(){
        if(unitPrice==0){
            Toast("Unit price is zero");
            return;
        }
        double price = unitPrice;
        for(int i=0;i<info_holder.getChildCount();i++){
            View view = info_holder.getChildAt(i);
            TextView title_tv = view.findViewById(R.id.title_tv);
            TextView text_tv = view.findViewById(R.id.text_tv);
            BaseModel model = (BaseModel) view.getTag();

            if(model.getString(TITLE).equalsIgnoreCase(HAIR_LENGTH))continue;

            if(model.getString(TITLE).equals(HAIR_BUNDLES)||model.getString(TITLE).equals(HAIR_GRAM)){
                int content = Integer.valueOf(text_tv.getText().toString().trim());
                price = price * (double) content;
            }
        }

        price_tv.setText(String.format("N%s",formatNumber((int) price)));
    }

    private Object getValueFromDetails(String key){
        ArrayList<HashMap<String,Object>> infos = (ArrayList<HashMap<String, Object>>) dummyProduct.get(DESCRIPTION);
        for(HashMap<String,Object> map:infos){
            BaseModel model = new BaseModel(map);
            if(model.getString(TITLE).equalsIgnoreCase(key)){
                return model.get(CONTENT);
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)return;

        if(requestCode == 199){
//            BaseModel bm = new BaseModel();
//            bm.put(TITLE,HAIR_LENGTH);
//            bm.put(CONTENT,selectedLength);
//            bm.put(THE_MAP,MyApplication.dummyInch.items);
//
//            addDescriptionView(bm);
//            infos.add(bm.items);
//            dummyProduct.put(DESCRIPTION,infos);
//            dummyProduct.updateItem();
//            MyApplication.dummyInch=null;
        }

        if (requestCode == REQUEST_SELECT_VIDEO) {
            Uri uri = data.getData();
            String path = getPathFromUri(context,uri);

            tryToCompressVideo(path);
        }

        if (requestCode == REQUEST_SELECT_PICTURE) {
            List<String> pics = (List<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
            dummy_paths.clear();
            dummy_paths.addAll(pics);
            String path = dummy_paths.remove(0);
            cropThis(path);
        }

        if (requestCode == UCrop.REQUEST_CROP) {
            try {
                handleCropResult(data);
            } catch (Exception e) {
                e.printStackTrace();
                Toast(e.getMessage());
            }
            if (!dummy_paths.isEmpty()) {
                String path = dummy_paths.remove(0);
                cropThis(path);
            }else{
//                vpTopAdapter.notifyDataSetChanged();
//                vp_tab.setViewPager(vp);
                images_urls.clear();
                ArrayList<String> imageList = new ArrayList<>(image_paths);
                uploadImages(false, imageList, new OnComplete() {
                    @Override
                    public void onComplete(String error, Object result) {
                        md.dismiss();
                        Toast("Added");
                        main_images.addAll(images_urls);
                        dummyProduct.put(IMAGES,main_images);
                        dummyProduct.updateItem();

                        vpTopAdapter.notifyDataSetChanged();
                        top_vp_tab.setViewPager(top_vp);
                    }
                });
            }

        }

    }

    private void handleCropResult(@NonNull Intent result) throws Exception {
        final Uri resultUri = UCrop.getOutput(result);

        if(resultUri==null)return;

        String path = resultUri.toString();
        File file = new File(new URI(path));
        File f1 = new Compressor(context).compressToFile(file);

        image_paths.add(f1.getAbsolutePath());
    }

    private void cropThis(String path){
        String destinationFileName = getRandomIdShort();
        destinationFileName += ".png";

        UCrop uCrop = UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(context.getCacheDir(), destinationFileName)));
        uCrop.start(this);
    }

    private void uploadImages(final boolean isVideo, final ArrayList<String> imageList, final OnComplete onComplete){
        md.show();
        if(imageList.isEmpty()){
            onComplete.onComplete(null,null);
            return;
        }
        String image = imageList.get(0);
        new BaseModel().uploadFile(image, new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                if(error!=null){
                    md.dismiss();
                    Toast(error);
                    showErrorDialog(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            uploadImages(isVideo, imageList, onComplete);
                        }
                    });
                    return;
                }

                String image = result.toString();

                if(isVideo){
                    videoUrl=image;
                }else {
                    images_urls.add(image);
                }
                imageList.remove(0);

                uploadImages(isVideo, imageList, onComplete);
            }
        });
    }

    private class DownloadReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String fileName = intent.getStringExtra(FILE_NAME);
            String error = intent.getStringExtra(ERROR);

            if(fileName.equals(videoFileName)){
                if(error!=null){
                    Toast(error);
                }
                setupVideo(dummyProduct.getString(VIDEO_URL),error==null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeReceivers();
    }

    private void removeReceivers(){
        try{
            if(downloadReceiver!=null){
                unregisterReceiver(downloadReceiver);
            }
        }catch (Exception e){};
    }

    public ArrayList<String> getLenghtList(String title){
        ArrayList<String> list = new ArrayList<>();
        for(BaseModel bm:prices){
          if(bm.getString(TITLE).equals(title)){
              HashMap<String,String> map = (HashMap<String, String>) bm.getMap(CONTENT);
              for(String s:map.keySet())list.add(s);
          }
        }
        Collections.sort(list);
        return list;
    }
}

