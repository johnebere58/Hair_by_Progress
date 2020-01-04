package com.hairbyprogress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.AddAddress;
import com.hairbyprogress.CartMain;
import com.hairbyprogress.CustomActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.base.Base.CITY;
import static com.hairbyprogress.base.Base.CUSTOM_HAIR_LENGTH;
import static com.hairbyprogress.base.Base.CUSTOM_HAIR_TYPES;
import static com.hairbyprogress.base.Base.CUSTOM_TYPE;
import static com.hairbyprogress.base.Base.HAIR_LENGTH;
import static com.hairbyprogress.base.Base.HAIR_TYPE;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.MY_ADDRESS;
import static com.hairbyprogress.base.Base.NAME;
import static com.hairbyprogress.base.Base.PHONE_NUMBER;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.STATE;
import static com.hairbyprogress.base.Base.STREET;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class AddressAdapter extends BaseAdapter<AddressAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    CartMain activity;

    int chosenItem = -1;

    public AddressAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
        this.activity = (CartMain)activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new ViewHolder(mInflater.inflate(R.layout.address_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);

        holder.title_tv.setText(String.format("Address %s",pos+1));
        holder.name_tv.setText(bm.getString(NAME));
        holder.phone_tv.setText(bm.getString(PHONE_NUMBER));
        holder.street_tv.setText(bm.getString(STREET));
        holder.city_tv.setText(bm.getString(CITY));
        holder.state_tv.setText(bm.getString(STATE));

        holder.edit_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mContext)
                        .items("Delete Address")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                                /*if(xp==0){
                                    MyApplication.dummyAddress = bm;
                                    activity.startActivity(new Intent(mContext, AddAddress.class));
                                }*/
                                if(xp==0){
                                    mainActivity.showYesNoDialog("Are you sure you want to delete this address?",
                                            new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    if(chosenItem==p){
                                                        chosenItem=-1;
                                                        activity.selectedAddress=null;
                                                    }

                                                    MyApplication.userModel.addToList(MY_ADDRESS,bm.items,false);
                                                    MyApplication.userModel.updateItem();
                                                    baseModels.remove(p);
                                                    notifyItemRemoved(p);
                                                    notifyItemRangeChanged(p,baseModels.size());
                                                }
                                            });
                                }
                            }
                        })
                        .show();
            }
        });

        if(p==chosenItem){
            holder.check.setImageResource(R.drawable.ic_check);
        }else{
            holder.check.setImageResource(0);
        }

        holder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectedAddress = bm;
                chosenItem = p;
                notifyDataSetChanged();
            }
        });

        if(baseModels.size()==1){
            holder.check.setImageResource(R.drawable.ic_check);
            chosenItem=0;
            activity.selectedAddress=bm;
        }
    }


    @Override
    public int getItemViewType(int position) {
        return baseModels.get(position).getInt(CUSTOM_TYPE);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.edit_but) View edit_but;
        @Nullable @BindView(R.id.check) ImageView check;
        @Nullable @BindView(R.id.name_tv) TextView name_tv;
        @Nullable @BindView(R.id.phone_tv) TextView phone_tv;
        @Nullable @BindView(R.id.street_tv) TextView street_tv;
        @Nullable @BindView(R.id.city_tv) TextView city_tv;
        @Nullable @BindView(R.id.state_tv) TextView state_tv;
        @Nullable @BindView(R.id.title_tv) TextView title_tv;


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