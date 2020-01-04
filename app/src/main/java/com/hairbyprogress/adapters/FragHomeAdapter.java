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
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.ProductMain;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.base.Base.CONTENT;
import static com.hairbyprogress.base.Base.DESCRIPTION;
import static com.hairbyprogress.base.Base.HAIR_COLOR;
import static com.hairbyprogress.base.Base.HAIR_TOOL;
import static com.hairbyprogress.base.Base.HAIR_TYPE;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_EXCLUSIVE;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_HAIR_TOOLS;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_HAIR_TYPES;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_COLORS;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.NAME;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE;
import static com.hairbyprogress.base.Base.SUB_CATEGORY;
import static com.hairbyprogress.base.Base.TITLE;
import static com.hairbyprogress.base.Base.WRITE_UP;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class FragHomeAdapter extends BaseAdapter<FragHomeAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;

    public FragHomeAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new ViewHolder(mInflater.inflate(type==HOME_DISPLAY_COLORS?
                R.layout.frag_home_colors_item:
                type==HOME_DISPLAY_HAIR_TOOLS?R.layout.frag_home_hair_tools_item:
                        type==HOME_DISPLAY_HAIR_TYPES?
                        R.layout.frag_home_hair_types_item:R.layout.frag_home_exclusive_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        bm.put(POSITION, p);
        int type = bm.getType();

        ArrayList<String> images = (ArrayList<String>) bm.getList(IMAGES);
        if(holder.imv!=null)holder.imv.displayImage(images.get(0));

        if(type==HOME_DISPLAY_COLORS) holder.tv.setText(mainActivity.getContentFromDescription(bm,HAIR_COLOR));
        if(type==HOME_DISPLAY_HAIR_TYPES) holder.tv.setText(mainActivity.getContentFromDescription(bm,HAIR_TYPE));
        if(type==HOME_DISPLAY_HAIR_TOOLS) holder.tv.setText(mainActivity.getContentFromDescription(bm,HAIR_TOOL));

        if(type==HOME_DISPLAY_EXCLUSIVE){
            holder.desc_tv.setText(bm.getString(WRITE_UP));
            int price = bm.getInt(PRICE);
            holder.price_tv.setText(String.format("N%s",mainActivity.formatNumber(price)));

            holder.main_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dummyProduct = bm;
                    activity.startActivity(new Intent(mContext, ProductMain.class));
                }
            });
        }

        if(holder.add_but!=null){
            holder.add_but.setOnClickListener(mainActivity.addToCartListener(bm));
        }

        if(holder.main_layout!=null){
            holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!isAdmin)return false;

                    new MaterialDialog.Builder(mContext)
                            .items(mContext.getString(R.string.delete))
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    mainActivity.showYesNoDialog(mContext.getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mainActivity.Toast(mContext.getString(R.string.deleted));
                                            mainActivity.removeFromHomeList(home_display,bm);
                                            bm.deleteItem();
                                            baseModels.remove(p);
                                            notifyItemRemoved(p);
                                            notifyItemRangeChanged(p,baseModels.size());
                                        }
                                    });
                                }
                            })
                            .show();
                    return true;
                }
            });

            holder.main_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String key = mainActivity.getOneItemFromDescription(bm);
                    mainActivity.startSearch(key);
                }
            });
        }
        }



    @Override
    public int getItemViewType(int position) {
        return baseModels.get(position).getType();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.imv) SketchImageView imv;
        @Nullable @BindView(R.id.tv) TextView tv;
        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.add_but) View add_but;

        @Nullable @BindView(R.id.desc_tv) TextView desc_tv;
        @Nullable @BindView(R.id.price_tv) TextView price_tv;


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