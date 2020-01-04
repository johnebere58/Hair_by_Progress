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
import com.hairbyprogress.InchMain;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.PricesMain;
import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;

import static com.hairbyprogress.base.Base.CONTENT;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.TITLE;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class PricesAdapter extends BaseAdapter<PricesAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    PricesMain activity;

    public PricesAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
        this.activity = (PricesMain)activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.description_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        bm.put(POSITION, p);

        final String title = bm.getString(TITLE);
        final String content = bm.getMap(CONTENT).toString();
        holder.title_tv.setText(title);
        holder.text_tv.setText(content);
        holder.main_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialDialog.Builder(mContext)
                        .items("Edit Content","Delete Section")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int xp, CharSequence text) {
                                if(xp==0){
                                    MyApplication.ID_TO_UPDATE = bm.getObjectId();
                                    MyApplication.dummyInch = new BaseModel(bm.getMap(CONTENT));
                                    activity.startActivityForResult(new Intent(mContext, InchMain.class),199);
                                }
                                if(xp==1){
                                    mainActivity.showYesNoDialog("Delete this section?", new MaterialDialog.SingleButtonCallback() {
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

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.main_layout) View main_layout;
        @Nullable @BindView(R.id.title_tv) TextView title_tv;
        @Nullable @BindView(R.id.text_tv) TextView text_tv;

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