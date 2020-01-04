package com.hairbyprogress.adapters;


import android.content.Context;
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
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.MyApplication.isAdmin;

/**
 * Created by John Ebere on 8/8/2016.
 */
public class VpTopAdapter extends PagerAdapter {

    Context context;
    ArrayList<BaseModel> baseModels;
    OnComplete onComplete;
    int type=0;

    public void setType(int type) {
        this.type = type;
    }

    MainActivity mainActivity;
        public  VpTopAdapter(Context context,ArrayList<BaseModel> baseModels){
            this.context = context;
            this.baseModels = baseModels;
            mainActivity = (MainActivity)context;
        }

    public void setOnComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    @Override
        public int getCount() {
                return baseModels.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, final int px) {
                View view = View.inflate(context, R.layout.frag_home_top_vp_image,null);
                SketchImageView vp_image = view.findViewById(R.id.vp_image);

                final BaseModel model = baseModels.get(px);
                ArrayList<String> images = (ArrayList<String>) model.getList(Base.IMAGES);
                String img = images.get(0);


                vp_image.displayImage(img);

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
                                                mainActivity.removeFromHomeList(home_display,model);
                                                model.deleteItem();
                                                baseModels.remove(px);
                                                notifyDataSetChanged();
                                                if(onComplete!=null)onComplete.onComplete(null,null);
                                            }
                                        });
                                    }
                                })
                                .show();
                        return true;
                    }
                });

            vp_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String key = mainActivity.getOneItemFromDescription(model);
                    mainActivity.startSearch(key);
                }
            });

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