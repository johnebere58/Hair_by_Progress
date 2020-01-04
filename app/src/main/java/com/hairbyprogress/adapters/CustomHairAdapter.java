package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.CustomActivity;
import com.hairbyprogress.ProductMain;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.base.Base.CUSTOM_HAIR_LENGTH;
import static com.hairbyprogress.base.Base.CUSTOM_HAIR_TYPES;
import static com.hairbyprogress.base.Base.CUSTOM_TYPE;
import static com.hairbyprogress.base.Base.HAIR_COLOR;
import static com.hairbyprogress.base.Base.HAIR_LENGTH;
import static com.hairbyprogress.base.Base.HAIR_TOOL;
import static com.hairbyprogress.base.Base.HAIR_TYPE;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_COLORS;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_EXCLUSIVE;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_HAIR_TOOLS;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_HAIR_TYPES;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE;
import static com.hairbyprogress.base.Base.WRITE_UP;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class CustomHairAdapter extends BaseAdapter<CustomHairAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    CustomActivity activity;

    int chosenItem = -1;

    public CustomHairAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
        this.activity = (CustomActivity)activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new ViewHolder(mInflater.inflate(type==CUSTOM_HAIR_TYPES?
                R.layout.custom_hair_item:
                type==CUSTOM_HAIR_LENGTH?R.layout.custom_length_item:
                R.layout.frag_home_hair_tools_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        bm.put(POSITION, p);
        int type = bm.getInt(CUSTOM_TYPE);

        if(type==CUSTOM_HAIR_TYPES) {
            final ArrayList<String> images = (ArrayList<String>) bm.getList(IMAGES);
            if (holder.imv != null) holder.imv.displayImage(images.get(0));

            final String hair = mainActivity.getContentFromDescription(bm, HAIR_TYPE);
            holder.tv.setText(hair);

            if(p==chosenItem){
                holder.check.setVisibility(View.VISIBLE);
            }else{
                holder.check.setVisibility(View.GONE);
            }

        holder.imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectedHairType = hair;
                activity.selectedHairTypePath = images.get(0);
                chosenItem = p;
                notifyDataSetChanged();
            }
        });
        }

        if(type==CUSTOM_HAIR_LENGTH){
            if(p==chosenItem){
                holder.check.setVisibility(View.VISIBLE);
            }else{
                holder.check.setVisibility(View.GONE);
            }

            final String len = bm.getString(HAIR_LENGTH);
            holder.tv.setText(len);
            holder.tv1.setText("INCHES");

            holder.main_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.selectedHairLength = len;
                    chosenItem = p;
                    notifyDataSetChanged();
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {
        return baseModels.get(position).getInt(CUSTOM_TYPE);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.imv) SketchImageView imv;
        @Nullable @BindView(R.id.tv) TextView tv;
        @Nullable @BindView(R.id.tv1) TextView tv1;
        @Nullable @BindView(R.id.check) View check;
        @Nullable @BindView(R.id.main_layout) View main_layout;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);

        }
    }

    @Override
    public int getItemCount() {
        return baseModels.size();
    }


}