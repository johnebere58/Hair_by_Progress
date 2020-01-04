package com.hairbyprogress.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hairbyprogress.R;
import com.hairbyprogress.base.BaseModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.Nullable;

import static com.hairbyprogress.base.Base.POSITION;

/**
 * Created by John Ebere on 5/13/2016.
 */
public class PostMainAdapter extends BaseAdapter<PostMainAdapter.ViewHolder> {

    ArrayList<BaseModel> baseModels;
    int fragType;

    public PostMainAdapter(Activity activity, ArrayList<BaseModel> baseModels,int fragType) {
        super(activity, baseModels);
        this.baseModels = baseModels;
        this.fragType = fragType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.frag_home_top_vp_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int p = pos;
        final BaseModel bm = baseModels.get(p);
        bm.put(POSITION, p);

        }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @Nullable @BindView(R.id.options) View options;


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