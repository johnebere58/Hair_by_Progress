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
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.OrderItems;
import com.hairbyprogress.R;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.RelativeTimeTextView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.dummyOrderIds;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.order_items;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.base.Base.ADDRESS;
import static com.hairbyprogress.base.Base.ITEMS_IN_CART;
import static com.hairbyprogress.base.Base.CONTENT;
import static com.hairbyprogress.base.Base.DELIVERY_TIME;
import static com.hairbyprogress.base.Base.DESCRIPTION;
import static com.hairbyprogress.base.Base.HAVE_RATED;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.MY_CART_ITEMS;
import static com.hairbyprogress.base.Base.ORDER_CANCELLED;
import static com.hairbyprogress.base.Base.ORDER_DELIVERED;
import static com.hairbyprogress.base.Base.ORDER_PENDING;
import static com.hairbyprogress.base.Base.PAYMENT_OPTION;
import static com.hairbyprogress.base.Base.PAY_ON_DELIVERY;
import static com.hairbyprogress.base.Base.PAY_PARTLY;
import static com.hairbyprogress.base.Base.PENDING;
import static com.hairbyprogress.base.Base.PHONE_NUMBER;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE;
import static com.hairbyprogress.base.Base.QUANTITY;
import static com.hairbyprogress.base.Base.STATUS;
import static com.hairbyprogress.base.Base.TITLE;
import static com.hairbyprogress.base.Base.TOTAL_ITEMS;
import static com.hairbyprogress.base.Base.WRITE_UP;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class OrderAdapter extends BaseAdapter<OrderAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    OnComplete onTotalChanged;
    View.OnClickListener clickRate;

    public void setClickRate(View.OnClickListener clickRate) {
        this.clickRate = clickRate;
    }

    public OrderAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
    }

    public void setOnTotalChanged(OnComplete onTotalChanged) {
        this.onTotalChanged = onTotalChanged;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);

        final int status = bm.getInt(STATUS);

        holder.rate_but.setVisibility(status==ORDER_CANCELLED?View.GONE:View.VISIBLE);
        //holder.action_tv.setTextColor(mContext.getResources().getColor(status==ORDER_DELIVERED?R.color.blue0:R.color.red0));
        holder.status_tv.setTextColor(mContext.getResources().getColor(status==ORDER_DELIVERED?R.color.blue0:R.color.red0));
//        holder.action_icon.setColorFilter(mContext.getResources().getColor(status==ORDER_DELIVERED?R.color.blue0:R.color.red0));
//        holder.action_icon.setImageResource(status==ORDER_DELIVERED?R.drawable.ic_star:R.drawable.ic_close);
//        holder.action_tv.setText(status==ORDER_DELIVERED?"RATE ORDER":"CANCEL ORDER");

        boolean rated = bm.getBoolean(HAVE_RATED);
        if(status==ORDER_DELIVERED){
            if(rated){
                holder.rate_but.setVisibility(View.GONE);
            }else{
                holder.rate_but.setVisibility(View.VISIBLE);
            }
        }else{
            holder.rate_but.setVisibility(View.GONE);
        }

        holder.rate_but.setTag(bm);
        holder.rate_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status==ORDER_DELIVERED){
                    if(clickRate!=null)clickRate.onClick(view);
                }
            }
        });

        holder.id_tv.setText(String.format("ORDER ID: %s",bm.getObjectId()));
        holder.status_tv.setText(status==ORDER_PENDING?"PENDING DELIVERY":status==ORDER_DELIVERED?"DELIVERED":"CANCELLED");

        int count = bm.getInt(TOTAL_ITEMS);
        holder.items_tv.setText(String.format("%s Item%s",count,count>1?"s":""));
        holder.price_tv.setText(String.format("N%s",mainActivity.formatNumber(bm.getInt(PRICE))));

        int option = bm.getInt(PAYMENT_OPTION);
        holder.payment_option_tv.setText(option==PAY_ON_DELIVERY?"Pay on Delivery":
                option==PAY_PARTLY?String.format("%s Part Payment (N%s)","10%",mainActivity.formatNumber(bm.getInt(PRICE)/10)):
                "Full Payment");

        StringBuilder sb= new StringBuilder();
        final HashMap<String,Object> dInfo = (HashMap<String, Object>) bm.getMap(ADDRESS);
        Object[] keys = dInfo.keySet().toArray();
        for(int i=0;i<keys.length;i++){
            String k = keys[i].toString();
            String value = (String) dInfo.get(k);
            sb.append(String.format("%s: %s",k.toUpperCase(),value));
            if(keys.length>1 && i!=keys.length-1){
                sb.append(", ");
            }
        }
        holder.delivery_info_tv.setText(sb);

        holder.time_tv.setReferenceTime(bm.getTime());

        long deliveryTime = bm.getLong(DELIVERY_TIME);
        if(deliveryTime==0){
            holder.delivery_time_tv.setText("1 - 14 Days");
        }else{
            holder.delivery_time_tv.setReferenceTime(deliveryTime);
        }

        holder.view_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dummyOrderIds = (ArrayList<String>) bm.getList(ITEMS_IN_CART);
                activity.startActivity(new Intent(mContext, OrderItems.class));
            }
        });

        holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isAdmin)return false;

                new MaterialDialog.Builder(mContext)
                        .items("SET PENDING","SET DELIVERED","SET CANCELLED","CALL CUSTOMER","DELETE ITEM")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, final int xp, CharSequence text) {
                                if(xp==3){
                                    String phone = (String) dInfo.get(PHONE_NUMBER);
                                    mainActivity.callPhone(phone);
                                }
                                else if(xp==4){
                                    mainActivity.showYesNoDialog("Delete Item?", new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            bm.deleteItem();
                                            baseModels.remove(p);
                                            order_items.remove(bm);
                                            notifyItemRemoved(p);
                                            notifyItemRangeChanged(p,baseModels.size());
                                        }
                                    });
                                }else{
                                    mainActivity.showYesNoDialog(String.format("SET %s", xp == 0 ? "PENDING?" : xp == 1 ? "DELIVERED" : "CANCELLED"
                                    ), new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            bm.put(DELIVERY_TIME,xp==1?System.currentTimeMillis():0);
                                            bm.put(STATUS,xp);
                                            bm.updateItem();
                                            notifyItemChanged(xp);
                                        }
                                    });
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

        if(p==baseModels.size()-1){
            holder.padding.setVisibility(View.VISIBLE);
        }else{
            holder.padding.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.padding) View padding;
        @Nullable @BindView(R.id.id_tv) TextView id_tv;
        @Nullable @BindView(R.id.status_tv) TextView status_tv;
        @Nullable @BindView(R.id.items_tv) TextView items_tv;
        @Nullable @BindView(R.id.price_tv) TextView price_tv;
        @Nullable @BindView(R.id.payment_option_tv) TextView payment_option_tv;
        @Nullable @BindView(R.id.delivery_info_tv) TextView delivery_info_tv;
        @Nullable @BindView(R.id.view_but) View view_but;
        @Nullable @BindView(R.id.rate_but) View rate_but;
//        @Nullable @BindView(R.id.action_tv) TextView action_tv;
//        @Nullable @BindView(R.id.action_icon) ImageView action_icon;
        @Nullable @BindView(R.id.time_tv) RelativeTimeTextView time_tv;
        @Nullable @BindView(R.id.delivery_time_tv) RelativeTimeTextView delivery_time_tv;

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