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
import com.hairbyprogress.R;
import com.hairbyprogress.SectionsMain;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;

import static com.hairbyprogress.base.Base.CONTENT;
import static com.hairbyprogress.base.Base.HAIR_PRICE;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.SECTION_TYPE;
import static com.hairbyprogress.base.Base.SECTION_TYPE_LIST;

import static com.hairbyprogress.base.Base.TITLE;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class SectionAdapter extends BaseAdapter<SectionAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    SectionsMain activity;

    public SectionAdapter(Activity activity, ArrayList<BaseModel> baseModels) {
        super(activity, baseModels);
        this.baseModels = baseModels;
        this.activity = (SectionsMain)activity;
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

//        final HashMap<String,Object> map = (HashMap<String, Object>) bm.getMap(THE_MAP);
//        boolean hasMap = !map.keySet().isEmpty();

        final String title = bm.getString(TITLE);
        int sectionType = bm.getInt(SECTION_TYPE);
        final String content = sectionType == SECTION_TYPE_LIST?
                               mainActivity.convertListToString(",", (ArrayList<String>) bm.getList(CONTENT)):
                               bm.getString(CONTENT);
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

                                    new MaterialDialog.Builder(mContext)
                                            .title(R.string.content)
                                            .input(mContext.getString(R.string.use_comma), content, false, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    bm.put(CONTENT,
                                                            bm.getInt(SECTION_TYPE)==SECTION_TYPE_LIST?
                                                            mainActivity.convertStringToList(",",input.toString().trim()):
                                                    input.toString().trim());
                                                    bm.updateItem();
                                                    notifyItemChanged(p);
                                                }
                                            })
                                            .show();
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