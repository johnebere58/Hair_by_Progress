package com.hairbyprogress.adapters;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.MainActivity;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.R;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.io.File;
import java.util.ArrayList;

import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.base.Base.IMAGES;

/**
 * Created by John Ebere on 8/8/2016.
 */
public class ProductVpTopAdapter extends PagerAdapter {

    Context context;
    ArrayList<String> imageList;
    OnComplete onComplete;
    int type;


    BaseActivity mainActivity;
        public ProductVpTopAdapter(Context context, ArrayList<String> imageList, int type){
            this.context = context;
            this.imageList = imageList;
            this.type = type;
            mainActivity = (BaseActivity)context;
        }

    public void setOnComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    @Override
        public int getCount() {
                return imageList.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, final int px) {
                View view = View.inflate(context, R.layout.frag_home_top_vp_image,null);
                SketchImageView vp_image = view.findViewById(R.id.vp_image);

                final String image = imageList.get(px);

                if(type==Base.TYPE_POST_PRODUCT) {
                    vp_image.setImageURI(Uri.fromFile(new File(image)));

                    vp_image.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            new MaterialDialog.Builder(context)
                                    .items(context.getString(R.string.delete))
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                            mainActivity.showYesNoDialog(context.getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    mainActivity.Toast(context.getString(R.string.deleted));
                                                    imageList.remove(px);
                                                    notifyDataSetChanged();
                                                    if (onComplete != null)
                                                        onComplete.onComplete(null, null);
                                                }
                                            });
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });
                }
                if(type==Base.TYPE_PRODUCT_MAIN){
                    vp_image.displayImage(image);
                    vp_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.startImageViewer(px,imageList);
                        }
                    });

                    vp_image.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if(!isAdmin)return false;

                            new MaterialDialog.Builder(context)
                                    .items(context.getString(R.string.delete))
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                            mainActivity.showYesNoDialog(context.getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    mainActivity.Toast(context.getString(R.string.deleted));
                                                    imageList.remove(px);
                                                    dummyProduct.put(IMAGES,imageList);
                                                    dummyProduct.updateItem();
                                                    notifyDataSetChanged();
                                                    if (onComplete != null)
                                                        onComplete.onComplete(null, null);
                                                }
                                            });
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });
                }
                container.addView(view);

                return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return (View)o==view;
        }

        }