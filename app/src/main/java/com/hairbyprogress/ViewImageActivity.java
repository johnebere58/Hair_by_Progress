
package com.hairbyprogress;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.hairbyprogress.base.BaseActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.BindView;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.POSITION;


public class ViewImageActivity extends BaseActivity {
    @BindView(R.id.vp) ViewPager vp;

    @BindView(R.id.back) public View back;
    @BindView(R.id.save_but) public View save_but;

    @BindView(R.id.dot_holder)LinearLayout dot_holder;
    int vpSize;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_activity);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        final int position = intent.getIntExtra(POSITION,0);
        final ArrayList<String> imageUrls = intent.getStringArrayListExtra(IMAGES);
        vpSize = imageUrls.size();

        ViewImageAdapter viewImageAdapter = new ViewImageAdapter(position,imageUrls,getSupportFragmentManager());
        vp.setAdapter(viewImageAdapter);
        vp.setOffscreenPageLimit(imageUrls.size());

        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for(int i=0;i<vpSize;i++){
                    LinearLayout l = (LinearLayout) dot_holder.getChildAt(i);
                    if(l==null)return;
                    ImageView v = (ImageView) l.getChildAt(0);

                    if(i==position){
                        v.setBackgroundResource(R.drawable.circle);
                        v.setAlpha(1f);
                    }else{
                        v.setBackgroundResource(R.drawable.circle1);
                        v.setAlpha(.5f);
                    }
                }
            }
        });

        vp.setCurrentItem(position);

        for(int i=0;i<vpSize;i++){
            LinearLayout l = (LinearLayout) View.inflate(context,R.layout.dot_item,null);
            ImageView v = (ImageView) l.getChildAt(0);

            if(i==position){
                v.setBackgroundResource(R.drawable.circle);
                v.setAlpha(1f);
            }else{
                v.setBackgroundResource(R.drawable.circle1);
                v.setAlpha(.5f);
            }

            dot_holder.addView(l);
        }

    }

    public class ViewImageAdapter extends FragmentPagerAdapter {

        private ArrayList<String> images;
        private int position;

        public ViewImageAdapter(int position, ArrayList<String> images, FragmentManager fm) {
            super(fm);
            this.images = images;
            this.position = position;
        }

        @Override
        public Fragment getItem(int p) {
            Fragment fragment = new ViewImageFragmentActivity();
            Bundle bundle = new Bundle();
            bundle.putString(IMAGES,images.get(p));
            bundle.putBoolean(CURRENT_POSITION,position==p);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return images.size();
        }

    }

    public static class ViewImageFragmentActivity extends Fragment {

        @BindView(R.id.imageView)
        SketchImageView imageView;

        private ViewImageActivity viewImageActivity;

        private String imageUrl;
        private boolean currentPositon;
        private Context context;
        private Activity activity;
        private boolean hasLoaded;

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater
                    .inflate(R.layout.view_image_activity_imv, container, false);
            ButterKnife.bind(this,rootView);

            viewImageActivity = (ViewImageActivity) activity;

            imageView.setOnTouchListener(new ImageMatrixTouchHandler(context));

            viewImageActivity.save_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        savePhoto();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast("Could not save photo");
                    }
                }
            });


            if(currentPositon ) {
                imageView.displayImage(imageUrl);
                hasLoaded=true;
            }

            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getActivity();
            activity = getActivity();

            Bundle bundle = this.getArguments();
            imageUrl = bundle.getString(IMAGES);
            currentPositon = bundle.getBoolean(CURRENT_POSITION);

        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if(isVisibleToUser&&viewImageActivity!=null){
                if(!hasLoaded){
                    imageView.displayImage(imageUrl);
                    hasLoaded=true;
                }

                viewImageActivity.save_but.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            savePhoto();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast("Could not save photo");
                        }
                    }
                });
                }
            }

        private void savePhoto()throws Exception{
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if(drawable!=null){
                Bitmap bitmap = drawable.getBitmap();
                if(bitmap!=null){
                    FileOutputStream outputStream = null;
                    String fileName = String.format(Locale.getDefault(),"%d.jpg",System.currentTimeMillis());
                    File file = new File(SAVE_PHOTO_FILE_PATH,fileName);
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Toast("Saved to this device");

                }
            }
        }

        public void Toast(Object text){
            Toast.makeText(context, String.valueOf(text), Toast.LENGTH_SHORT).show();
        }
        }

}
