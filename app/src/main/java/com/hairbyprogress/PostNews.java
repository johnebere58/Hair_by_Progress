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
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;

import static com.tangxiaolv.telegramgallery.GalleryActivity.GALLERY_CONFIG;

public class PostNews extends BaseActivity {

    @BindView(R.id.vp)ViewPager vp;
    @BindView(R.id.vp_tab)SmartTabLayout vp_tab;

    @BindView(R.id.select_photo)View select_photo;

    @BindView(R.id.title_etv)EditText title_etv;
    @BindView(R.id.message_etv)EditText message_etv;
    @BindView(R.id.tag_tv)TextView tag_tv;

    @BindView(R.id.post_but)View post_but;

    public ArrayList<String> images_urls = new ArrayList<>();
    public ArrayList<String> image_paths = new ArrayList<>();
    public ArrayList<String> dummy_paths = new ArrayList<>();

    String title;
    String message;
    String tag;

    ProductVpTopAdapter vpTopAdapter;

    MaterialDialog md;
    BaseModel model = new BaseModel();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_news);
        ButterKnife.bind(this);

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

        select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryConfig config = new GalleryConfig.Build()
                        .singlePhoto(true)
//                        .hintOfPick("You cannot add more than 6 images")
//                        .limitPickPhoto(6-image_paths.size())
                        .filterMimeTypes(new String[]{"image/*"})
                        .build();

                Intent intent = new Intent(activity, GalleryActivity.class);
                intent.putExtra(GALLERY_CONFIG, config);
                startActivityForResult(intent, REQUEST_SELECT_PICTURE);
            }
        });

        post_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNews();
            }
        });

        tag_tv.setOnClickListener(v->{
            addDescription();
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
                                            tag_tv.setText(parts[1].trim());
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

                                        tag_tv.setText(content);
                                    }
                                })
                                .show();
                    }

                })
                .show();
    }

    private ArrayList<String> getTitles(){
        ArrayList<String> titles = new ArrayList<>();
        for(BaseModel bm:MyApplication.sections){
            int sectionType = bm.getInt(SECTION_TYPE);
            if(sectionType!=SECTION_TYPE_LIST)continue;

            String title = bm.getString(TITLE);
            titles.add(title);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=RESULT_OK)return;

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

        //uCrop.withAspectRatio(2,1);

        uCrop.start(this);
    }

    private void publishNews(){
        title = title_etv.getText().toString().trim();
        message = message_etv.getText().toString().trim();
        tag = tag_tv.getText().toString().trim();

        if(!isConnectingToInternet()){
            ToastNoInternet();
            return;
        }

        if(!MyApplication.settingsHasLoaded){
            Toast("Settings not loaded");
            return;
        }

        if(title.isEmpty()){
            title_etv.setError("Add a title");
            title_etv.requestFocus();
            return;
        }

        if(message.isEmpty()){
            message_etv.setError("Add a message");
            message_etv.requestFocus();
            return;
        }


        model.put(TITLE,title);
        model.put(MESSAGE,message);
        model.put(ITEM_TAG,tag);
        model.put(OBJECT_ID,getRandomIdShort());

        ArrayList<String> imageList = new ArrayList<>(image_paths);
        uploadImages(imageList);
    }

    private void uploadImages(final ArrayList<String> imageList){
        md.show();
        if(imageList.isEmpty()){
            uploadNews();
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


                images_urls.add(image);
                imageList.remove(0);

                uploadImages(imageList);
            }
        });
    }

    private void uploadNews(){
        model.put(IMAGES,images_urls);
        model.put(TIME,System.currentTimeMillis());
        md.show();

        MyApplication.appSettingsModel.put(NEW_NOTIFICATION,model.items);
        MyApplication.appSettingsModel.updateItem(APP_SETTINGS_BASE,APP_SETTINGS,new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                md.dismiss();
                if(!task.isSuccessful()){
                    Toast(getString(R.string.error_try_again));
                    return;
                }
                Toast(getString(R.string.successful));
                finish();
            }
        });
    }
}
