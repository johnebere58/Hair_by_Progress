package com.hairbyprogress;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hairbyprogress.adapters.CustomHairAdapter;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;
import com.hairbyprogress.utils.KeyboardUtils;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.appSettingsModel;
import static com.hairbyprogress.MyApplication.categories;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.reuseDummy;
import static com.hairbyprogress.MyApplication.userModel;
import static com.tangxiaolv.telegramgallery.GalleryActivity.GALLERY_CONFIG;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class CustomActivity extends BaseActivity {

    @BindView(R.id.vp) CustomViewPager vp;
    @BindView(R.id.tab) LinearLayout tab;
    @BindView(R.id.next) public View next;
    @BindView(R.id.add_tv) public TextView add_tv;
    @BindView(R.id.add_icon) public ImageView add_icon;
    int vpSize = 6;

    public String title="";
    public String story="";
    public String cat="";

    public String selectedHairType="";
    public String selectedHairTypePath="";
    public String selectedHairLength="";
    public String hairBundles="";
    public String hairGram="";
    public String customizedPath="";

    public ArrayList<HashMap<String,Object>> detailsList = new ArrayList();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_main);
        ButterKnife.bind(this);

        vp.setPagingEnabled(false);

        vp.setOffscreenPageLimit(vpSize);
        vp.setAdapter(new PageAdapter(getFragmentManager()));
        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                hideKeyboard(vp);
                setSelection(position);

                if(position==5){
                    add_tv.setText("ADD");
                    add_icon.setVisibility(View.VISIBLE);
                }else{
                    add_tv.setText("NEXT");
                    add_icon.setVisibility(View.GONE);
                }
            }
        });

    }

    private void setSelection(int p){
        for(int i=0;i<tab.getChildCount();i++){
            View v = tab.getChildAt(i);
            if(i<=p){
                v.setAlpha(1f);
                v.setBackgroundResource(R.drawable.circle);
            }else{
                v.setAlpha(.3f);
                v.setBackgroundResource(R.drawable.circle1);
            }
        }
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

   public static class ImageFragments extends Fragment {

       int position;
       @Nullable @BindView(R.id.hair_rsv)RecyclerView hair_rsv;
       @Nullable @BindView(R.id.len_rsv)RecyclerView len_rsv;
       @Nullable @BindView(R.id.bundle_etv)EditText bundle_etv;
       @Nullable @BindView(R.id.gram_etv)EditText gram_etv;
       @Nullable @BindView(R.id.select_photo)TextView select_photo;
       @Nullable @BindView(R.id.imv)ImageView imv;
       @Nullable @BindView(R.id.info_holder)LinearLayout info_holder;
       @Nullable @BindView(R.id.price_tv)TextView price_tv;
       @Nullable @BindView(R.id.fee_tv)TextView fee_tv;
       CustomHairAdapter customHairAdapter;

       ArrayList<BaseModel> mainList = new ArrayList<>();

       Context context;
       Activity activity;
       CustomActivity mainActivity;
       MaterialDialog md;

       BaseModel model = new BaseModel();

       public ArrayList<String> images_urls = new ArrayList<>();

       @Override
       public void onCreate(@Nullable Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           Bundle bundle = getArguments();
           position = bundle.getInt(POSITION);
           context = getActivity();
           activity = getActivity();
           mainActivity = (CustomActivity)activity;

       }

       @Override
       public void setRetainInstance(boolean retain) {
           super.setRetainInstance(true);
       }

       @Nullable
       @Override
       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
           ViewGroup rootView = (ViewGroup) inflater
                   .inflate(position==0?R.layout.custom_page1:
                           position==1?R.layout.custom_page2:
                           position==2?R.layout.custom_page3:
                           position==3?R.layout.custom_page4:
                           position==4?R.layout.custom_page5:
                           R.layout.custom_page6, container, false);

           ButterKnife.bind(this,rootView);



           if(position==0)mainActivity.next.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   clickNext();
               }
           });

           if(position==0){
               customHairAdapter = new CustomHairAdapter(activity,mainList);
               hair_rsv.setLayoutManager(new GridLayoutManager(context,3));
               hair_rsv.setAdapter(customHairAdapter);

               for(BaseModel model: MyApplication.home_display){
                   if(model.getType()==Base.HOME_DISPLAY_HAIR_TYPES) {
                       model.put(CUSTOM_TYPE,CUSTOM_HAIR_TYPES);
                       mainList.add(model);
                   }
               }

               customHairAdapter.notifyDataSetChanged();
           }

           if(position==1){
               customHairAdapter = new CustomHairAdapter(activity,mainList);
               len_rsv.setLayoutManager(new GridLayoutManager(context,3));
               len_rsv.setAdapter(customHairAdapter);

               ArrayList<String> list = mainActivity.getFromSections(HAIR_LENGTH);
               for(String l : list){
                   BaseModel model = new BaseModel();
                   model.put(HAIR_LENGTH,l);
                   model.put(CUSTOM_TYPE,CUSTOM_HAIR_LENGTH);
                   mainList.add(model);
               }

               customHairAdapter.notifyDataSetChanged();
           }

           if(select_photo!=null){
               select_photo.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       if(!mainActivity.customizedPath.isEmpty()){
                           imv.setImageResource(0);
                           select_photo.setText("UPLOAD PHOTO");
                           mainActivity.customizedPath = "";
                           return;
                       }

                       GalleryConfig config = new GalleryConfig.Build()
                               .singlePhoto(true)
                               .filterMimeTypes(new String[]{"image/*"})
                               .build();

                       Intent intent = new Intent(activity, GalleryActivity.class);
                       intent.putExtra(GALLERY_CONFIG, config);
                       startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                   }
               });
           }

           if(fee_tv!=null){
               String fee = mainActivity.getFromTextSections(CUSTOMIZING_FEE);
               if(!fee.isEmpty()){
               int customFee = Integer.valueOf(fee);
               fee_tv.setText(String.format("Customized hair color cost additional N%s per 100 gram",mainActivity.formatNumber(customFee)));
           }}

           return rootView;
       }

       @Override
       public void onActivityResult(int requestCode, int resultCode, Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           if(resultCode!=RESULT_OK)return;
           if(data==null)return;

           if (requestCode == REQUEST_SELECT_PICTURE) {
               List<String> pics = (List<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
               String path = pics.get(0);
               cropThis(path);
           }

           if (requestCode == UCrop.REQUEST_CROP) {
               try {
                   handleCropResult(data);
               } catch (Exception e) {
                   e.printStackTrace();
                   Toast(e.getMessage());
               }
           }
       }

       private void handleCropResult(@NonNull Intent result) throws Exception {
           final Uri resultUri = UCrop.getOutput(result);

           if(resultUri==null)return;

           String path = resultUri.toString();
           File file = new File(new URI(path));
           File f1 = new Compressor(context).compressToFile(file);

           mainActivity.customizedPath = f1.getAbsolutePath();
           imv.setImageURI(Uri.fromFile(f1));
           select_photo.setText("REMOVE PHOTO");
       }

       private void cropThis(String path){
           String destinationFileName = getRandomIdShort();
           destinationFileName += ".png";

           UCrop uCrop = UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(context.getCacheDir(), destinationFileName)));

           //uCrop.withAspectRatio(2,1);

           uCrop.start(activity,this);
       }

       private void Toast(String s){
           Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
       }

       private void clickNext() {
           if(position==0){
               if(mainActivity.selectedHairType.isEmpty()){
                   Toast("Select Hair Type");
                   return;
               }
               mainActivity.vp.setCurrentItem(1);
           }

           if(position==1){
               if(mainActivity.selectedHairLength.isEmpty()){
                   Toast("Select Hair Length");
                   return;
               }
               mainActivity.vp.setCurrentItem(2);
           }

           if(position==2){
               mainActivity.hairBundles = bundle_etv.getText().toString();
               if(mainActivity.hairBundles.isEmpty()){
                   bundle_etv.setError("How many bundles do you want?");
                   bundle_etv.requestFocus();
                   return;
               }
               mainActivity.vp.setCurrentItem(3);
           }

           if(position==3){
               mainActivity.hairGram = gram_etv.getText().toString();
               if(mainActivity.hairGram.isEmpty()){
                   gram_etv.setError("How many grams do you want?");
                   gram_etv.requestFocus();
                   return;
               }
               mainActivity.vp.setCurrentItem(4);
           }

           if(position==4){
               mainActivity.vp.setCurrentItem(5);
           }

           if(position==5){
               //Toast("Uploading");
               if(!mainActivity.isConnectingToInternet()){
                   mainActivity.ToastNoInternet();
                   return;
               }

               if(userModel==null){
                   activity.startActivity(new Intent(context, LoginActivity.class));
                   return;
               }

               md = mainActivity.getLoadingDialog("Adding",false);

               new MaterialDialog.Builder(context)
                       .title("Quantity")
                       .inputType(InputType.TYPE_CLASS_NUMBER)
                       .input("What quantity?", "1", false, new MaterialDialog.InputCallback() {
                           @Override
                           public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                               int q = Integer.valueOf(input.toString().trim());

                               model.put(DESCRIPTION,mainActivity.detailsList);
                               model.put(PRICE,priceFromTv());
                               model.put(QUANTITY,q);
                               model.put(WRITE_UP,"Customized Hair");
                               model.put(USER_ID,userModel.getUserId());

                               if(!mainActivity.customizedPath.isEmpty()) {
                                   ArrayList<String> imageList = new ArrayList<>();
                                   imageList.add(mainActivity.customizedPath);
                                   uploadImages(imageList);
                               }else{
                                   ArrayList<String> imageList = new ArrayList<>();
                                   imageList.add(mainActivity.selectedHairTypePath);
                                   model.put(IMAGES,imageList);
                                   uploadTop();
                               }

                           }
                       })
                       .positiveText("ADD")
                       .negativeText("CANCEL").show();

           }

       }

       private void uploadImages(final ArrayList<String> imageList){
           md.show();
           if(imageList.isEmpty()){
               model.put(IMAGES,images_urls);
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
                       mainActivity.showErrorDialog(new MaterialDialog.SingleButtonCallback() {
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
           md.show();
           final String id = getRandomId();

           model.justSave(CART_BASE,id, new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(!task.isSuccessful()){
                       md.dismiss();
                       Toast(getString(R.string.error_try_again));
                       return;
                   }

                   userModel.addToList(MY_CART_ITEMS,id,true);
                   userModel.updateItem();
                   activity.sendBroadcast(new Intent(BROADCAST_CART_UPDATED));

                   md.dismiss();
                   Toast(getString(R.string.successful));
                   activity.finish();
               }
           });
       }

       private int priceFromTv(){
           String p = price_tv.getText().toString();
           p = p.replace(",","");
           p = p.replace("N","");
           return Integer.valueOf(p.trim());
       }



       @Override
       public void setUserVisibleHint(boolean isVisibleToUser) {
           super.setUserVisibleHint(isVisibleToUser);
           if(isVisibleToUser && mainActivity!=null){
               mainActivity.next.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       clickNext();
                   }
               });

               if(position==5){
                   createTotal();
               }
           }
       }

       private void createTotal(){
           info_holder.removeAllViews();
           mainActivity.detailsList.clear();

           String cFee = mainActivity.getFromTextSections(CUSTOMIZING_FEE);
           if(cFee.isEmpty()){
               Toast("Error: No customizing fee");
               return;
           }

           addDescriptionView(HAIR_TYPE,mainActivity.selectedHairType);
           addDescriptionView(HAIR_LENGTH,mainActivity.selectedHairLength);
           addDescriptionView(HAIR_BUNDLES,mainActivity.hairBundles);
           addDescriptionView(HAIR_GRAM,mainActivity.hairGram);
           if(!mainActivity.customizedPath.isEmpty()){
               addDescriptionView(CUSTOMIZED_COLOR,"YES");
               addDescriptionView(CUSTOMIZING_FEE,String.format("N%s",mainActivity.formatNumber(getCustomFee())));
           }

           calPrice();
       }

       private int getCustomFee(){
           if(mainActivity.customizedPath.isEmpty())return 0;

           int customFee = Integer.valueOf(mainActivity.getFromTextSections(CUSTOMIZING_FEE));
           int bundles = Integer.valueOf(mainActivity.hairBundles);
           int gram = Integer.valueOf(mainActivity.hairGram);
           int totalGram = bundles*gram;

           int remainder = totalGram%100;
           int divider = totalGram/100;

           int multiplier = (remainder!=0?1:0) + (divider==0?1:divider);

           int theFee =  customFee*multiplier;

           return theFee;
       }

       private void addDescriptionView(final String title, String content){
           final View view = View.inflate(context,R.layout.description_item,null);
           View main_layout = view.findViewById(R.id.main_layout);
           TextView title_tv = view.findViewById(R.id.title_tv);
           TextView text_tv = view.findViewById(R.id.text_tv);

           title_tv.setText(title);
           text_tv.setText(content);

           HashMap<String,Object> map = new HashMap<>();
           map.put(TITLE,title_tv.getText().toString());
           map.put(CONTENT,text_tv.getText().toString());

           mainActivity.detailsList.add(map);

           info_holder.addView(view);
       }

       private void calPrice(){
           int pr = mainActivity.getPriceFromPriceModel(
                   mainActivity.selectedHairType.toLowerCase().startsWith("straig")?
                   HAIR_PRICE_STRAIGHT:HAIR_PRICE_CURLY,String.valueOf(mainActivity.selectedHairLength));
           if(pr==0){
               Toast("Error: could not find hair price");
               return;
           }

           int priceForLength = Integer.valueOf(pr);

           int gram = Integer.valueOf(mainActivity.hairGram);
           int bundle = Integer.valueOf(mainActivity.hairBundles);

           double pricePerGram = (double)priceForLength/100;

           double mainPrice = pricePerGram * gram * bundle;

           double totalPrice = mainPrice + getCustomFee();

           price_tv.setText(String.format("N%s",mainActivity.formatNumber((int)totalPrice)));
       }
   }



    @Override
    public void onBackPressed() {
        if(vp.getCurrentItem()==0){
            super.onBackPressed();
            return;
        }
        int p = vp.getCurrentItem();

        /*if(p==vpSize-1){
            p--;p--;
            vp.setCurrentItem(p);
            return;
        }*/

        p--;
        p = p<0?0:p;
        vp.setCurrentItem(p);
    }
}
