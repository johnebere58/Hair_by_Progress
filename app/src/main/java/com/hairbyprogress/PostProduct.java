package com.hairbyprogress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import id.zelory.compressor.Compressor;

import static com.hairbyprogress.utils.StringUtil.getPathFromUri;
import static com.tangxiaolv.telegramgallery.GalleryActivity.GALLERY_CONFIG;

public class PostProduct extends BaseActivity {

    @BindView(R.id.display_section)TextView display_section;
    @BindView(R.id.vp)ViewPager vp;
    @BindView(R.id.vp_tab)SmartTabLayout vp_tab;

    @BindView(R.id.description_layout)LinearLayout description_layout;
    @BindView(R.id.add_description)View add_description;
    @BindView(R.id.select_photo)View select_photo;

    @BindView(R.id.select_preview)View select_preview;
    @BindView(R.id.preview_holder)View preview_holder;
    @BindView(R.id.preview)JzvdStd preview;

    @BindView(R.id.desc_etv)EditText desc_etv;

    @BindView(R.id.price_etv)EditText price_etv;

    @BindView(R.id.pm_tv)TextView pm_tv;
    @BindView(R.id.publish)View publish;

    private ArrayList<String> images_urls = new ArrayList<>();
    private ArrayList<String> image_paths = new ArrayList<>();
    private ArrayList<String> dummy_paths = new ArrayList<>();

    private ArrayList<String> addedTitles = new ArrayList<>();

    private String videoPath;
    private String videoUrl;
    private String write_up;
    private String price;

    private ProductVpTopAdapter vpTopAdapter;

    private MaterialDialog md;
    private BaseModel model = new BaseModel();

    private String itemCategory="";

    String priceModel = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_product);
        ButterKnife.bind(this);

        select_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        vpTopAdapter = new ProductVpTopAdapter(context,image_paths,TYPE_POST_PRODUCT);
        vpTopAdapter.setOnComplete(new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                vp_tab.setViewPager(vp);
            }
        });
        vp.setAdapter(vpTopAdapter);
        vp_tab.setViewPager(vp);

        md = getLoadingDialog(getString(R.string.uploading),false);

        if(MyApplication.sections.isEmpty()){
            Toast(getString(R.string.setting_up));
            finish();
            return;
        }

        final ArrayList<String> sections = getFromSections(ITEM_CATEGORY);
        sections.add(EXCLUSIVE);

        display_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .items(sections)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                itemCategory = text.toString().toLowerCase();
                                display_section.setBackgroundColor(getResources().getColor(R.color.brown0));
                                display_section.setTextColor(getResources().getColor(R.color.white));
                                display_section.setText(text);
                            }
                        })
                        .show();
            }
        });

        add_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDescription();
            }
        });

        select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryConfig config = new GalleryConfig.Build()
                        .singlePhoto(false)
                        .hintOfPick("You cannot add more than 6 images")
                        .limitPickPhoto(6-image_paths.size())
                        .filterMimeTypes(new String[]{"image/*"})
                        .build();
                //GalleryActivity.openActivity(activity,1,config);

                Intent intent = new Intent(activity, GalleryActivity.class);
                intent.putExtra(GALLERY_CONFIG, config);
                startActivityForResult(intent, REQUEST_SELECT_PICTURE);
            }
        });

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishTop();
            }
        });


    }

    private void addDescription(){
        ArrayList<String> titles= getTitles();
        titles.add(INPUT);
        new MaterialDialog.Builder(context)
                .items(titles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
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
                                            addDescriptionView(parts[0].trim(), parts[1].trim(), null, TYPE_FIXED);
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
                                            //addDescriptionView(HAIR_LENGTH,content,null,TYPE_HAIR_LENGTH);
                                            //startActivityForResult(new Intent(context,InchMain.class).putExtra(HAIR_LENGTH,selectedLength),199);
                                            new MaterialDialog.Builder(context)
                                                    .items(getPriceModels())
                                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                                        @Override
                                                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                                            priceModel=text.toString();
                                                            pm_tv.setVisibility(View.VISIBLE);
                                                            pm_tv.setText(priceModel);
                                                            addDescriptionView(HAIR_LENGTH,content,null,TYPE_HAIR_LENGTH);
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
                                                        if (text.equals(FIXED)) {
                                                            addDescriptionView(title, content, null, TYPE_FIXED);
                                                        }
                                                        if (text.equals(FLEXIBLE_NUMBER)) {
                                                            addDescriptionView(title, content, true, TYPE_FLEXIBLE_NUMBER);
                                                        }
                                                        if (text.equals(FLEXIBLE_TEXT)) {
                                                            addDescriptionView(title, content, true, TYPE_FLEXIBLE_TEXT);
                                                        }
                                                    }
                                                })
                                                .show();
                                    }
                                })
                                .show();
                    }

                })
                .show();
    }

    private void addDescriptionView(final String title, String content,Object tag,int type){
        final View view = View.inflate(context,R.layout.description_item,null);
        View main_layout = view.findViewById(R.id.main_layout);
        TextView title_tv = view.findViewById(R.id.title_tv);
        TextView text_tv = view.findViewById(R.id.text_tv);
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
                                description_layout.removeView(view);
                            }
                        })
                        .show();
                return true;
            }
        });
        title_tv.setText(title);
        text_tv.setText(content);

        if(tag!=null){
            if(type == TYPE_FLEXIBLE_NUMBER){
                but.setText("FLEXIBLE NUMBER");
                but.setVisibility(View.VISIBLE);
            }else if(type == TYPE_FLEXIBLE_TEXT){
                but.setText("FLEXIBLE TEXT");
                but.setVisibility(View.VISIBLE);
            } else if(type==TYPE_HAIR_LENGTH){
                but.setText("FLEXIBLE");
                but.setVisibility(View.VISIBLE);
            }
        }

        HashMap<String,Object> map = new HashMap<>();
            map.put(TITLE,title_tv.getText().toString());
            map.put(CONTENT,text_tv.getText().toString());

            if(tag!=null) {
                if (type == TYPE_FLEXIBLE_NUMBER) map.put(FLEXIBLE_NUMBER, true);
                if (type == TYPE_FLEXIBLE_TEXT) map.put(FLEXIBLE_TEXT, true);
                //if (type == TYPE_HAIR_LENGTH) map.put(THE_MAP,tag);
            }
        text_tv.setTag(map);

        if(type == TYPE_HAIR_LENGTH){
            int dprice = getPriceFromPriceModel(priceModel,content);
            text_tv.setText(String.format("%s (N%s)",content,formatNumber(dprice)));
        }


        addedTitles.add(title);
        description_layout.addView(view);
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
//        if(!addedTitles.contains(NAME))titles.add(NAME);
//        if(!addedTitles.contains(PRICE))titles.add(PRICE);
        Collections.sort(titles);
        return titles;
    }

    private ArrayList<String> getContent(String title){
        for(BaseModel bm:MyApplication.sections){
            if(title.equals(bm.getString(TITLE))){
                return (ArrayList<String>) bm.getList(CONTENT);
            }
        }
        return new ArrayList<String>();
    }

    private ArrayList<HashMap<String,Object>> getDetailsList(){
        ArrayList<HashMap<String,Object>> list = new ArrayList<>();
        for(int i=0;i<description_layout.getChildCount();i++){
            final View view = description_layout.getChildAt(i);
            TextView title_tv = view.findViewById(R.id.title_tv);
            TextView text_tv = view.findViewById(R.id.text_tv);
            HashMap<String,Object> map = (HashMap<String, Object>) text_tv.getTag();
            list.add(map);
        }
        return list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)return;

        /*if(requestCode == 199){
            addDescriptionView(HAIR_LENGTH,selectedLength,MyApplication.dummyInch.items,TYPE_HAIR_LENGTH);
            MyApplication.dummyInch=null;
        }*/

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
                vpTopAdapter.notifyDataSetChanged();
                vp_tab.setViewPager(vp);
            }

        }

    }

    private void handleCropResult(@NonNull Intent result) throws Exception {
        final Uri resultUri = UCrop.getOutput(result);

        if(resultUri==null)return;

        String path = resultUri.toString();
        File file = new File(new URI(path));
        File f1 = new Compressor(context).compressToFile(file);

//        vp_image.setImageURI(resultUri);
//        image_paths.clear();
        image_paths.add(f1.getAbsolutePath());
    }

    private void cropThis(String path){
        String destinationFileName = getRandomIdShort();
        destinationFileName += ".png";

        UCrop uCrop = UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(context.getCacheDir(), destinationFileName)));

//        if(itemCategory.equals(EXCLUSIVE)){
//            uCrop.withAspectRatio(2,1);
//        }else{
//            uCrop.withAspectRatio(1,1);
//        }
        uCrop.start(this);
    }

    private void publishTop(){
        write_up = desc_etv.getText().toString().trim();
        price = price_etv.getText().toString().trim();

        if(!isConnectingToInternet()){
            ToastNoInternet();
            return;
        }

        if(itemCategory.isEmpty()){
            Toast("Select Section");
            return;
        }

        if(image_paths.isEmpty()){
            Toast(getString(R.string.add_photo));
            return;
        }

        if(getDetailsList().isEmpty()){
            Toast("No info Added");
            return;
        }

        if(hasTitle(HAIR_LENGTH) && !hasTitle(HAIR_GRAM)){
            Toast("Add Hair Gram");
            return;
        }

        if(hasTitle(HAIR_GRAM) && !hasTitle(HAIR_LENGTH)){
            Toast("Add Hair Length");
            return;
        }

        if(write_up.isEmpty()){
            desc_etv.setError("No write up added");
            desc_etv.requestFocus();
            return;
        }

        if(price.isEmpty()){
            price_etv.setError("No price added");
            price_etv.requestFocus();
            return;
        }


        model.put(DESCRIPTION,getDetailsList());
        model.put(PRICE,Integer.valueOf(price));
        model.put(WRITE_UP,write_up);
        model.put(PRICE_MODEL,priceModel);

        ArrayList<String> imageList = new ArrayList<>(image_paths);
        if(videoUrl==null && videoPath!=null)imageList.add(videoPath);
        uploadImages(imageList);
    }

    private boolean hasTitle(String title){
        for(HashMap<String,Object> map:getDetailsList()){
            String t = (String) map.get(TITLE);
            if(t.equalsIgnoreCase(title))return true;
        }
        return false;
    }

    private void uploadImages(final ArrayList<String> imageList){
        md.show();
        if(imageList.isEmpty()){
            uploadTop();
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
                            uploadImages(imageList);
                        }
                    });
                    return;
                }

                String image = result.toString();

                if(videoPath!=null && imageList.size()==1){
                    videoUrl=image;
                }else {
                    images_urls.add(image);
                }
                imageList.remove(0);

                uploadImages(imageList);
            }
        });
    }

    private void uploadTop(){
        model.put(ITEM_CATEGORY,itemCategory);
        model.put(IMAGES,images_urls);

        if(videoUrl!=null)model.put(VIDEO_URL,videoUrl);

        model.justSave(PRODUCT_BASE, getRandomId(), new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(!task.isSuccessful()){
                    md.dismiss();
                    Toast(getString(R.string.error_try_again));
                    return;
                }
                md.dismiss();
                Toast(getString(R.string.successful));
                new MaterialDialog.Builder(context)
                        .content(R.string.add_again_question)
                        .positiveText(getString(R.string.yes))
                        .negativeText(getString(R.string.no))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                recreate();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                finish();
                            }
                        })
                        .show();
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
                preview.setUp(String.valueOf(destPath),"", Jzvd.SCREEN_WINDOW_NORMAL);
                preview_holder.setVisibility(View.VISIBLE);
                videoPath = destPath;
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


}
