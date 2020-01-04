package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.OrderItems;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.RelativeTimeTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;

import static com.hairbyprogress.MyApplication.dummyOrderIds;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.base.Base.APPROVED;
import static com.hairbyprogress.base.Base.ITEMS_IN_CART;
import static com.hairbyprogress.base.Base.MESSAGE;
import static com.hairbyprogress.base.Base.NAME;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE_FEEDBACK;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE_NORMAL;
import static com.hairbyprogress.base.Base.NOTIFY_TYPE_OPINION;
import static com.hairbyprogress.base.Base.OPINION;
import static com.hairbyprogress.base.Base.PENDING;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.RATINGS;
import static com.hairbyprogress.base.Base.RATINGS_TEXT;
import static com.hairbyprogress.base.Base.REPLIES_COUNT;
import static com.hairbyprogress.base.Base.STARS;
import static com.hairbyprogress.base.Base.STATUS;
import static com.hairbyprogress.base.Base.STORY;
import static com.hairbyprogress.base.Base.TYPE;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class FragNotifyAdapter extends BaseAdapter<FragNotifyAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;

    public FragNotifyAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case NOTIFY_TYPE_FEEDBACK:
                view = mInflater.inflate(R.layout.frag_notify_feedback_item, parent, false);
                break;
            default:
                view = mInflater.inflate(R.layout.null_layout, parent, false);
                break;
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);

        final int notifyType = bm.getInt(NOTIFY_TYPE);
        if (notifyType == 0){

            if(p==baseModels.size()-1){
                holder.padding.setVisibility(View.VISIBLE);
            }else{
                holder.padding.setVisibility(View.GONE);
            }

           //holder.title_tv.setText("New Feedback!");
            holder.rating_num_tv.setText(bm.getString(RATINGS));
            holder.rating_tv.setText(bm.getString(RATINGS_TEXT));
            holder.name_tv.setText(String.format("%s",bm.getString(NAME)));
            holder.view_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dummyOrderIds = (ArrayList<String>) bm.getList(ITEMS_IN_CART);
                    activity.startActivity(new Intent(mContext, OrderItems.class));
                }
            });

            holder.main_layout.setAlpha(bm.getInt(STATUS)==PENDING?.5f:1);
            holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!isAdmin)return false;

                    new MaterialDialog.Builder(mContext)
                            .items("Approve Feedback","Delete")
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                                    if(xp==0){
                                        bm.put(STATUS,APPROVED);
                                        bm.updateItem();
                                        baseModels.remove(p);
                                        notifyItemRemoved(p);
                                        notifyItemRangeChanged(p,baseModels.size());
                                    }

                                    if(xp==1){
                                        mainActivity.showYesNoDialog("Are you sure you want to delete this feedback?", new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                bm.deleteItem();
                                                baseModels.remove(p);
                                                notifyItemRemoved(p);
                                                notifyItemRangeChanged(p,baseModels.size());
                                            }
                                        });
                                    }
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return baseModels.get(position).getInt(NOTIFY_TYPE);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.padding) View padding;
        //@Nullable @BindView(R.id.title_tv) TextView title_tv;
        @Nullable @BindView(R.id.rating_num_tv) TextView rating_num_tv;
        @Nullable @BindView(R.id.rating_tv) TextView rating_tv;
        @Nullable @BindView(R.id.name_tv) TextView name_tv;
        @Nullable @BindView(R.id.view_but) View view_but;


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