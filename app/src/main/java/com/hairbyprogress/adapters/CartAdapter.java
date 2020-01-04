package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.ProductMain;
import com.hairbyprogress.R;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.isJohn;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.base.Base.CART_BASE;
import static com.hairbyprogress.base.Base.CONTENT;
import static com.hairbyprogress.base.Base.DESCRIPTION;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.MY_CART_ITEMS;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE;
import static com.hairbyprogress.base.Base.PRIVATE_CHAT;
import static com.hairbyprogress.base.Base.QUANTITY;
import static com.hairbyprogress.base.Base.TITLE;
import static com.hairbyprogress.base.Base.USER_ID;
import static com.hairbyprogress.base.Base.WRITE_UP;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class CartAdapter extends BaseAdapter<CartAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    OnComplete onTotalChanged;
    boolean viewMode;
    boolean forRating;
    View.OnClickListener selectionListener;

    public void setForRating(boolean forRating) {
        this.forRating = forRating;
    }

    public void setSelectionListener(View.OnClickListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
    }

    public CartAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
    }

    public void setOnTotalChanged(OnComplete onTotalChanged) {
        this.onTotalChanged = onTotalChanged;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        //bm.put(POSITION, p);

        holder.add_but.setTag(bm);
        if(forRating) {
            holder.add_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectionListener.onClick(view);
                    baseModels.remove(p);
                    notifyItemRemoved(p);
                    notifyItemRangeChanged(p, baseModels.size());
                }
            });
        }else{
            holder.add_but.setOnClickListener(mainActivity.addToCartListener(bm));
        }

        holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isJohn) return false;

                mainActivity.showYesNoDialog("Are you sure you want to permanently delete this item", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        baseModels.remove(p);
                        notifyItemRemoved(p);
                        notifyItemRangeChanged(p,baseModels.size());
                        bm.deleteItem();
                    }
                });
                return true;
            }
        });

        holder.desc_tv.setText(bm.getString(WRITE_UP));

        final int price = bm.getInt(PRICE);
        int quantity = bm.getInt(QUANTITY);
        quantity = quantity==0?1:quantity;
        holder.price_tv.setText(String.format("N%s",mainActivity.formatNumber(price)));

        final ArrayList<String> images = (ArrayList<String>) bm.getList(IMAGES);
        if(!images.isEmpty()){
        String img0 = images.get(0);
        holder.imv.displayImage(img0);
        }

        holder.imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startImageViewer(0,images);
            }
        });

        ArrayList<HashMap<String,Object>> infos = (ArrayList<HashMap<String, Object>>) bm.get(DESCRIPTION);
        StringBuilder sb = new StringBuilder();
        if(infos!=null){
            for(int i=0;i<infos.size();i++){
                HashMap<String,Object> map = infos.get(i);
                BaseModel model = new BaseModel(map);
                sb.append(String.format("%s - %s",model.getString(TITLE),model.getString(CONTENT)));
                if(infos.size()>1 && i!=infos.size()-1){
                    sb.append(", ");
                }
            }}
        holder.desc_tv1.setText(sb);

        holder.num_tv.setText(String.format("Item %s",p+1));

        final int finalQuantity = quantity;
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showYesNoDialog("Are you sure you want to remove this item?", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        baseModels.remove(p);
                        notifyItemRemoved(p);
                        notifyItemRangeChanged(p,baseModels.size());
                        bm.deleteItem();

                        MyApplication.cart_items.remove(bm);

                        userModel.addToList(MY_CART_ITEMS,bm.getObjectId(),false);
                        userModel.updateItem();
                        activity.sendBroadcast(new Intent(Base.BROADCAST_CART_UPDATED));

                        if(onTotalChanged!=null)onTotalChanged.onComplete(null, finalQuantity *price);
                    }
                });
            }
        });



        holder.total_tv.setText(String.format("N%s",mainActivity.formatNumber(quantity*price)));

        holder.quantity_tv.setText(String.valueOf(quantity));

        final int finalQuantity1 = quantity;
        holder.quantity_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewMode)return;
                new MaterialDialog.Builder(mContext)
                        .title("Quantity")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("What quantity?", String.valueOf(finalQuantity1), false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                int q = Integer.valueOf(input.toString().trim());
                                bm.put(QUANTITY,q);
                                bm.updateItem();

                                holder.total_tv.setText(String.format("N%s",mainActivity.formatNumber(q*price)));

                                if(onTotalChanged!=null)onTotalChanged.onComplete(null,null);

                                notifyItemChanged(p);

                                Toast("Updated");
                            }
                        })
                        .positiveText("UPDATE")
                        .negativeText("CANCEL").show();
            }
        });

        if(viewMode){
            holder.remove.setVisibility(View.GONE);
            holder.add_but.setVisibility(View.VISIBLE);
        }else{
            holder.remove.setVisibility(View.VISIBLE);
            holder.add_but.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.add_but) View add_but;
        @Nullable @BindView(R.id.num_tv) TextView num_tv;
        @Nullable @BindView(R.id.imv) SketchImageView imv;
        @Nullable @BindView(R.id.remove) ImageView remove;
        @Nullable @BindView(R.id.desc_tv) TextView desc_tv;
        @Nullable @BindView(R.id.desc_tv1) TextView desc_tv1;
        @Nullable @BindView(R.id.price_tv) TextView price_tv;
        @Nullable @BindView(R.id.quantity_tv) TextView quantity_tv;
        @Nullable @BindView(R.id.total_tv) TextView total_tv;

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