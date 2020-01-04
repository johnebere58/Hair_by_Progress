package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.LoginActivity;
import com.hairbyprogress.ProductMain;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.cart_items;
import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.base.Base.AVATAR_POSITION;
import static com.hairbyprogress.base.Base.CART_BASE;
import static com.hairbyprogress.base.Base.DESCRIPTION;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.MAIN_OBJECT_ID;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE;
import static com.hairbyprogress.base.Base.QUANTITY;
import static com.hairbyprogress.base.Base.USER_ID;
import static com.hairbyprogress.base.Base.WRITE_UP;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class MarketAdapter extends BaseAdapter<MarketAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;

    public MarketAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.market_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        //bm.put(POSITION, p);

        holder.main_layout.setTag(bm);

        holder.desc_tv.setText(bm.getString(WRITE_UP));

        int price = bm.getInt(PRICE);
        holder.price_tv.setText(String.format("N%s",mainActivity.formatNumber(price)));

        ArrayList<String> images = (ArrayList<String>) bm.getList(IMAGES);
        if(!images.isEmpty()){
        String img0 = images.get(0);
        holder.imv.displayImage(img0);
        }

        holder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dummyProduct = bm;
                activity.startActivity(new Intent(mContext, ProductMain.class));
            }
        });

        holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isAdmin)return false;

                mainActivity.showYesNoDialog("Are you sure you want to delete?", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        bm.deleteItem();
                        //Toast(bm.getObjectId());
                        baseModels.remove(p);
                        notifyItemRemoved(p);
                        notifyItemRangeChanged(p,baseModels.size());
                        deleteSubItems(bm.getObjectId());
                    }
                });
                return true;
            }
        });

        holder.add_but.setOnClickListener(mainActivity.addToCartListener(bm));

        if(p ==baseModels.size()-1){
            holder.padding.setVisibility(View.VISIBLE);
        }else {
            holder.padding.setVisibility(View.GONE);
        }

        }

    private void deleteSubItems(String objectId){
        for(BaseModel bm:cart_items){
            if(bm.getString(MAIN_OBJECT_ID).equals(objectId)){
                bm.deleteItem();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.desc_tv) TextView desc_tv;
        @Nullable @BindView(R.id.price_tv) TextView price_tv;
        @Nullable @BindView(R.id.imv) SketchImageView imv;
        @Nullable @BindView(R.id.add_but) View add_but;

        @Nullable @BindView(R.id.padding) View padding;

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