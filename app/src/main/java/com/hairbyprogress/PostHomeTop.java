package com.hairbyprogress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;
import com.yalantis.ucrop.UCrop;

import org.w3c.dom.Text;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import me.xiaopan.sketch.SketchImageView;

import static com.tangxiaolv.telegramgallery.GalleryActivity.GALLERY_CONFIG;

public class PostHomeTop extends BaseActivity {

    @BindView(R.id.display_section)TextView display_section;
    @BindView(R.id.vp_image)SketchImageView vp_image;
    @BindView(R.id.description_layout)LinearLayout description_layout;
    @BindView(R.id.add_description)View add_description;
    @BindView(R.id.select_photo)View select_photo;
    @BindView(R.id.publish)View publish;

    public ArrayList<String> images_urls = new ArrayList<>();
    public ArrayList<String> image_paths = new ArrayList<>();
    public ArrayList<String> dummy_paths = new ArrayList<>();

    public ArrayList<String> addedTitles = new ArrayList<>();

    MaterialDialog md;
    BaseModel model = new BaseModel();

    int type = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_home_top);
        ButterKnife.bind(this);

        md = getLoadingDialog(getString(R.string.uploading),false);

        if(MyApplication.sections.isEmpty()){
            Toast(getString(R.string.setting_up));
            finish();
            return;
        }

        display_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .items("TOP SECTION","HAIR COLORS","HAIR TYPES","HAIR TOOLS"/*,"EXCLUSIVE"*/)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                type=position;
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
                        .singlePhoto(true)
                        //.hintOfPick("You cannot add more than 10 images")
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
        new MaterialDialog.Builder(context)
                .items(getTitles())
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        final String title = text.toString();
                        if(title.equals(NAME)||title.equals(PRICE)){
                            new MaterialDialog.Builder(context)
                                    .input("Enter Name", "", false, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                            String content = input.toString();
                                            addDescriptionView(title,content);
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
                                        String content = text.toString();
                                        addDescriptionView(title,content);
                                    }
                                })
                                .show();
                    }
                })
                .show();
    }

    private void addDescriptionView(final String title, String content){
        final View view = View.inflate(context,R.layout.description_item,null);
        View main_layout = view.findViewById(R.id.main_layout);
        TextView title_tv = view.findViewById(R.id.title_tv);
        TextView text_tv = view.findViewById(R.id.text_tv);

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

    private ArrayList<HashMap<String,String>> getDetailsList(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        for(int i=0;i<description_layout.getChildCount();i++){
            final View view = description_layout.getChildAt(i);
            TextView title_tv = view.findViewById(R.id.title_tv);
            TextView text_tv = view.findViewById(R.id.text_tv);
            HashMap<String,String> map = new HashMap<>();
            map.put(TITLE,title_tv.getText().toString());
            map.put(CONTENT,text_tv.getText().toString());
            list.add(map);
        }
        return list;
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
            }

        }

    }

    private void handleCropResult(@NonNull Intent result) throws Exception {
        final Uri resultUri = UCrop.getOutput(result);

        if(resultUri==null)return;

        String path = resultUri.toString();
        File file = new File(new URI(path));
        File f1 = new Compressor(context).compressToFile(file);

        vp_image.setImageURI(resultUri);
        image_paths.clear();
        image_paths.add(f1.getAbsolutePath());
    }

    private void cropThis(String path){
        String destinationFileName = getRandomIdShort();
        destinationFileName += ".png";

        UCrop uCrop = UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(context.getCacheDir(), destinationFileName)));

//        if(type==HOME_DISPLAY_HAIR_TYPES){
//            uCrop.withAspectRatio(1,1);
//        }else{
//        uCrop.withAspectRatio(2,1);
//        }
        uCrop.start(this);
    }

    private void publishTop(){
        if(!isConnectingToInternet()){
            ToastNoInternet();
            return;
        }

        if(type==-1){
            Toast("Select Display Section");
            return;
        }

        if(image_paths.isEmpty()){
            Toast(getString(R.string.add_photo));
            return;
        }

        if(getDetailsList().isEmpty()){
            Toast(getString(R.string.no_description_added));
            return;
        }

        /*if(type!=HOME_DISPLAY_TOP && !addedTitles.contains(NAME)){
            Toast("Add Name in Description");
            return;
        }*/

        model.put(DESCRIPTION,getDetailsList());

        ArrayList<String> imageList = new ArrayList<>(image_paths);
        uploadImages(imageList);
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
                images_urls.add(image);

                imageList.remove(0);
                uploadImages(imageList);
            }
        });
    }

    private void uploadTop(){
        model.put(TYPE,type);
        model.put(IMAGES,images_urls);
        model.justSave(HOME_DISPLAY_BASE, getRandomId(), new OnCompleteListener() {
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
}
