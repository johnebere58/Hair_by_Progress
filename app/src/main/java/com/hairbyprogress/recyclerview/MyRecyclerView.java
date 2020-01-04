package com.hairbyprogress.recyclerview;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hairbyprogress.R;
import com.hairbyprogress.recyclerview.impl.PrvInterface;
import com.hairbyprogress.recyclerview.loadmore.BaseLoadMoreView;
import com.hairbyprogress.recyclerview.loadmore.DefaultLoadMoreView;
import com.hairbyprogress.recyclerview.util.PullToRefreshRecyclerViewUtil;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Ebere on 2015/11/11.
 */
public class MyRecyclerView extends SwipeRefreshLayout implements PrvInterface,Scrollable {

    private RecyclerView mMyRecyclerView;

    private FrameLayout mRootFrameLayout;

    private int mLoadMoreCount = 2;

    private int mCurScroll;

    private boolean mIsSwipeEnable = false;

    private BaseLoadMoreView mLoadMoreFooter;

    private PagingableListener mPagingableListener;

    private OnScrollListener mOnScrollLinstener;

    private PullToRefreshRecyclerViewUtil mPtrrvUtil;



    // Fields that should be saved onSaveInstanceState
    private int mPrevFirstVisiblePosition;
    private int mPrevFirstVisibleChildHeight = -1;
    private int mPrevScrolledChildrenHeight;
    private int mPrevScrollY;
    private int mScrollY;
    private SparseIntArray mChildrenHeights;

    // Fields that don't need to be saved onSaveInstanceState
    private ObservableScrollViewCallbacks mCallbacks;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;
    private boolean mIntercepted;
    private MotionEvent mPrevMoveEvent;
    private ViewGroup mTouchInterceptionViewGroup;

    private View retry_holder,retry_layout,empty_layout,loading_layout;
    private AVLoadingIndicatorView loadingIndicator;
    private RefreshListener refreshListener;

    private boolean staggered;

    public boolean atBottom;
    private boolean hasMoreItems;

    private View empty1;
    private View empty2;
    private ImageView empty2_icon;
    private TextView empty2_tv;
    private TextView empty2_tv2;



    private Context context;

    private boolean chatMode;

    public void setChatMode(boolean chatMode) {
        this.chatMode = chatMode;
    }

    public boolean atTheBottom(){
        return atBottom;
    }

    public void setStaggered(boolean staggered) {
        this.staggered = staggered;
    }

    public interface PagingableListener{
        void onLoadMoreItems();
    }

    public interface OnScrollListener{
        void onScrollStateChanged(RecyclerView myRecyclerView, int newState);
        void onScrolled(RecyclerView myRecyclerView, int dx, int dy);

        //old-method, like listview 's onScroll ,but it's no use ,right ? by linhonghong 2015.10.29
        void onScroll(RecyclerView myRecyclerView, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }

    public MyRecyclerView(Context context) {
        this(context,null);
        this.context = context;
    }

    public MyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setup();
    }

    private void setup(){
        setupExtra();
        initView();
        setLinster();
    }

    public void setRefreshing(boolean refreshing){
        super.setRefreshing(refreshing);
    }

    public interface RefreshListener{
        void startRefreshing();
    }

    public void setRefreshListener(RefreshListener refreshListener){
        this.refreshListener = refreshListener;
    }

    private void startRefreshing(){
        if(refreshListener!=null)refreshListener.startRefreshing();
    }

    private void initView(){
        mRootFrameLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.ptrrv_root_view, null);

        retry_holder = mRootFrameLayout.findViewById(R.id.retry_holder);
        retry_layout = mRootFrameLayout.findViewById(R.id.retry_layout);
        empty_layout = mRootFrameLayout.findViewById(R.id.empty_layout);
        loading_layout = mRootFrameLayout.findViewById(R.id.loading_layout);

        loadingIndicator = mRootFrameLayout.findViewById(R.id.loading_indicator);

        this.addView(mRootFrameLayout);

        this.setColorSchemeResources(R.color.swap_holo_green_bright, R.color.swap_holo_bule_bright,
                R.color.swap_holo_green_bright, R.color.swap_holo_bule_bright);

        mMyRecyclerView = mRootFrameLayout.findViewById(R.id.rv);

        empty1 = mRootFrameLayout.findViewById(R.id.empty1);
        empty2 = mRootFrameLayout.findViewById(R.id.empty2);
        empty2_tv = mRootFrameLayout.findViewById(R.id.empty2_tv);
        empty2_tv2 = mRootFrameLayout.findViewById(R.id.empty2_tv2);
        empty2_icon = mRootFrameLayout.findViewById(R.id.empty2_icon);

        mMyRecyclerView.setHasFixedSize(true);



        if(!mIsSwipeEnable) {
            this.setEnabled(false);
        }
        hideAllView();
    }

    public interface RetryListener{
        void onRetry();
    }

    public void showLoading(){
        hideAllView();
        loading_layout.setVisibility(VISIBLE);
        loadingIndicator.show();
        loadingIndicator.setVisibility(VISIBLE);
    }

    public void hideLoading(final RetryListener retryListener){
        hideAllView();
        if(retryListener!=null){
            retry_holder.setVisibility(VISIBLE);
            retry_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryListener.onRetry();
                }
            });
        }
    }
    public void hideLoading(){
        hideAllView();
    }

    public void showEmpty(){
        hideAllView();
        empty_layout.setVisibility(VISIBLE);
        empty1.setVisibility(VISIBLE);
        empty2.setVisibility(GONE);
    }
    public void showEmpty(int icon,String title,String sub){
        hideAllView();
        empty_layout.setVisibility(VISIBLE);
        empty1.setVisibility(GONE);
        empty2.setVisibility(VISIBLE);

        empty2_icon.setImageResource(icon);
        empty2_tv.setText(title);
        empty2_tv2.setText(sub);
    }


    public void hideAllView(){
        loading_layout.setVisibility(GONE);
        retry_holder.setVisibility(GONE);
        empty_layout.setVisibility(GONE);
    }

    /**
     * Init
     */
    private void setupExtra(){
        mPtrrvUtil = new PullToRefreshRecyclerViewUtil();
    }

    private void setLinster(){
        InterOnScrollListener mInterOnScrollListener = new InterOnScrollListener();
        mMyRecyclerView.addOnScrollListener(mInterOnScrollListener);
    }

    @Override
    public void setOnRefreshComplete() {
        this.setRefreshing(false);
    }

    @Override
    public void setOnLoadMoreComplete() {
        setHasMoreItems(false);
    }

    @Override
    public void setPagingableListener(PagingableListener pagingableListener) {
        mPagingableListener = pagingableListener;
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        mMyRecyclerView.setAdapter(adapter);
    }

    @Override
    public void scrollToPosition(int position) {
        mMyRecyclerView.scrollToPosition(position);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        mMyRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void setLoadMoreFooter(BaseLoadMoreView loadMoreFooter) {
        mLoadMoreFooter = loadMoreFooter;
    }

    @Override
    public BaseLoadMoreView getLoadMoreFooter() {
        return mLoadMoreFooter;
    }

    @Override
    public void addOnScrollListener(OnScrollListener onScrollLinstener) {
        mOnScrollLinstener = onScrollLinstener;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        if(mMyRecyclerView != null) {
            return mMyRecyclerView.getLayoutManager();
        }
        return null;
    }

    @Override
    public void onFinishLoading(boolean hasMoreItems, boolean needSetSelection) {

        if(getLayoutManager() == null){
            return;
        }

        if(!hasMoreItems && mLoadMoreFooter != null){
            //if it's last line, minus the extra height of loadmore
            mCurScroll = mCurScroll - mLoadMoreFooter.getLoadMorePadding();
        }

        // if items is too short, don't show loadingview
        if (getLayoutManager().getItemCount() < mLoadMoreCount) {
            hasMoreItems = false;
        }

        setHasMoreItems(hasMoreItems);

        if (needSetSelection) {
            int first = findFirstVisibleItemPosition();
            mMyRecyclerView.scrollToPosition(--first);
        }
    }

    private int findFirstVisibleItemPosition(){
        return mPtrrvUtil.findFirstVisibleItemPosition(getLayoutManager());
    }

    private int findLastVisibleItemPosition(){
        return mPtrrvUtil.findLastVisibleItemPosition(getLayoutManager());
    }

    private int findFirstCompletelyVisibleItemPosition(){
        return mPtrrvUtil.findFirstCompletelyVisibleItemPosition(getLayoutManager());
    }

    @Override
    public void setSwipeEnable(boolean enable) {
        //just like extra setEnable(boolean).but it's more easy to use, like super.setEnable
        mIsSwipeEnable = enable;
        this.setEnabled(mIsSwipeEnable);
    }

    @Override
    public boolean isSwipeEnable() {
        return mIsSwipeEnable;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return this.mMyRecyclerView;
    }

    @Override
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if(mMyRecyclerView != null){
            mMyRecyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void setLoadMoreCount(int count) {
        mLoadMoreCount = count;
    }

    @Override
    public void release() {

    }

    private void setHasMoreItems(boolean hasMoreItems) {
        this.hasMoreItems = hasMoreItems;
        if(mLoadMoreFooter == null){
            mLoadMoreFooter = new DefaultLoadMoreView(getContext(),getRecyclerView());

        }
        if(!this.hasMoreItems) {
            //remove loadmore
            mMyRecyclerView.removeItemDecoration(mLoadMoreFooter);
        } else {
            //add loadmore
            mMyRecyclerView.removeItemDecoration(mLoadMoreFooter);
            mMyRecyclerView.addItemDecoration(mLoadMoreFooter);
        }
    }

    private static final int HIDE_THRESHOLD = 20;

    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;

    private class InterOnScrollListener extends RecyclerView.OnScrollListener{

        @Override
        public void onScrollStateChanged(RecyclerView myRecyclerView, int newState) {
            super.onScrollStateChanged(myRecyclerView, newState);
            //do super before callback
            if(mOnScrollLinstener != null){
                mOnScrollLinstener.onScrollStateChanged(myRecyclerView,newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView myRecyclerView, int dx, int dy) {
            super.onScrolled(myRecyclerView, dx, dy);
            //do super before callback
            if(getLayoutManager() == null){
                //here layoutManager is null
                return;
            }

            int firstVisibleItem = !staggered?((LinearLayoutManager) myRecyclerView.getLayoutManager()).findFirstVisibleItemPosition():
                    ((StaggeredGridLayoutManager)myRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPositions(null)[0];

            if (firstVisibleItem == 0) {
                if(!mControlsVisible) {
                    mControlsVisible = true;
                }
            } else {
                if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                    mControlsVisible = false;
                    mScrolledDistance = 0;
                } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                    mControlsVisible = true;
                    mScrolledDistance = 0;
                }
            }
            if((mControlsVisible && dy>0) || (!mControlsVisible && dy<0)) {
                mScrolledDistance += dy;
            }

            mCurScroll = dy + mCurScroll;

            int  visibleItemCount, totalItemCount, lastVisibleItem;
            visibleItemCount = getLayoutManager().getChildCount();
            totalItemCount = getLayoutManager().getItemCount();
            firstVisibleItem = findFirstVisibleItemPosition();
            //sometimes ,the last item is too big so as that the screen cannot show the item fully
            lastVisibleItem = findLastVisibleItemPosition();
            //lastVisibleItem = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

            if(mIsSwipeEnable) {
                if (findFirstCompletelyVisibleItemPosition() != 0) {
                    //here has a bug, if the item is too big , use findFirstCompletelyVisibleItemPosition will cannot swipe
                    /*if(!staggered)*/MyRecyclerView.this.setEnabled(false);
                } else {
                    MyRecyclerView.this.setEnabled(true);


                }
            }


            if(totalItemCount < mLoadMoreCount){
                setHasMoreItems(false);
            }else {
                if(!staggered) {
                    if (hasMoreItems && ((lastVisibleItem + 1) == totalItemCount)) {
                        if (mPagingableListener != null) {
                            mPagingableListener.onLoadMoreItems();
                        }
                    }
                    if (((lastVisibleItem + 1) >= totalItemCount)){
                        atBottom=true;
                    }else{
                        atBottom=false;
                    }

                }else{
                    if (hasMoreItems && ((lastVisibleItem + 1) >= (totalItemCount/2)+(totalItemCount/3))) {
                        if (mPagingableListener != null) {

                            mPagingableListener.onLoadMoreItems();
                        }
                    }

                }
            }

            if(mOnScrollLinstener != null){
                mOnScrollLinstener.onScrolled(myRecyclerView, dx, dy);
                mOnScrollLinstener.onScroll(myRecyclerView, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        mPrevFirstVisiblePosition = ss.prevFirstVisiblePosition;
        mPrevFirstVisibleChildHeight = ss.prevFirstVisibleChildHeight;
        mPrevScrolledChildrenHeight = ss.prevScrolledChildrenHeight;
        mPrevScrollY = ss.prevScrollY;
        mScrollY = ss.scrollY;
        mChildrenHeights = ss.childrenHeights;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.prevFirstVisiblePosition = mPrevFirstVisiblePosition;
        ss.prevFirstVisibleChildHeight = mPrevFirstVisibleChildHeight;
        ss.prevScrolledChildrenHeight = mPrevScrolledChildrenHeight;
        ss.prevScrollY = mPrevScrollY;
        ss.scrollY = mScrollY;
        ss.childrenHeights = mChildrenHeights;
        return ss;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mCallbacks != null) {
            if (getChildCount() > 0) {
                int firstVisiblePosition = getRecyclerView().getChildPosition(getChildAt(0));
                int lastVisiblePosition = getRecyclerView().getChildPosition(getChildAt(getChildCount() - 1));
                for (int i = firstVisiblePosition, j = 0; i <= lastVisiblePosition; i++, j++) {
                    if (mChildrenHeights.indexOfKey(i) < 0 || getChildAt(j).getHeight() != mChildrenHeights.get(i)) {
                        mChildrenHeights.put(i, getChildAt(j).getHeight());
                    }
                }

                View firstVisibleChild = getChildAt(0);
                if (firstVisibleChild != null) {
                    if (mPrevFirstVisiblePosition < firstVisiblePosition) {
                        // scroll down
                        int skippedChildrenHeight = 0;
                        if (firstVisiblePosition - mPrevFirstVisiblePosition != 1) {
                            for (int i = firstVisiblePosition - 1; i > mPrevFirstVisiblePosition; i--) {
                                if (0 < mChildrenHeights.indexOfKey(i)) {
                                    skippedChildrenHeight += mChildrenHeights.get(i);
                                } else {
                                    // Approximate each item's height to the first visible child.
                                    // It may be incorrect, but without this, scrollY will be broken
                                    // when scrolling from the bottom.
                                    skippedChildrenHeight += firstVisibleChild.getHeight();
                                }
                            }
                        }
                        mPrevScrolledChildrenHeight += mPrevFirstVisibleChildHeight + skippedChildrenHeight;
                        mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                    } else if (firstVisiblePosition < mPrevFirstVisiblePosition) {
                        // scroll up
                        int skippedChildrenHeight = 0;
                        if (mPrevFirstVisiblePosition - firstVisiblePosition != 1) {
                            for (int i = mPrevFirstVisiblePosition - 1; i > firstVisiblePosition; i--) {
                                if (0 < mChildrenHeights.indexOfKey(i)) {
                                    skippedChildrenHeight += mChildrenHeights.get(i);
                                } else {
                                    // Approximate each item's height to the first visible child.
                                    // It may be incorrect, but without this, scrollY will be broken
                                    // when scrolling from the bottom.
                                    skippedChildrenHeight += firstVisibleChild.getHeight();
                                }
                            }
                        }
                        mPrevScrolledChildrenHeight -= firstVisibleChild.getHeight() + skippedChildrenHeight;
                        mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                    } else if (firstVisiblePosition == 0) {
                        mPrevFirstVisibleChildHeight = firstVisibleChild.getHeight();
                    }
                    if (mPrevFirstVisibleChildHeight < 0) {
                        mPrevFirstVisibleChildHeight = 0;
                    }
                    mScrollY = mPrevScrolledChildrenHeight - firstVisibleChild.getTop();
                    mPrevFirstVisiblePosition = firstVisiblePosition;

                    mCallbacks.onScrollChanged(mScrollY, mFirstScroll, mDragging);
                    if (mFirstScroll) {
                        mFirstScroll = false;
                    }

                    if (mPrevScrollY < mScrollY) {
                        //down
                        mScrollState = ScrollState.UP;
                    } else if (mScrollY < mPrevScrollY) {
                        //up
                        mScrollState = ScrollState.DOWN;
                    } else {
                        mScrollState = ScrollState.STOP;
                    }
                    mPrevScrollY = mScrollY;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Whether or not motion events are consumed by children,
                    // flag initializations which are related to ACTION_DOWN events should be executed.
                    // Because if the ACTION_DOWN is consumed by children and only ACTION_MOVEs are
                    // passed to parent (this view), the flags will be invalid.
                    // Also, applications might implement initialization codes to onDownMotionEvent,
                    // so call it here.
                    mFirstScroll = mDragging = true;
                    mCallbacks.onDownMotionEvent();
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIntercepted = false;
                    mDragging = false;
                    mCallbacks.onUpOrCancelMotionEvent(mScrollState);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mPrevMoveEvent == null) {
                        mPrevMoveEvent = ev;
                    }
                    float diffY = ev.getY() - mPrevMoveEvent.getY();
                    mPrevMoveEvent = MotionEvent.obtainNoHistory(ev);
                    if (getCurrentScrollY() - diffY <= 0) {
                        // Can't scroll anymore.

                        if (mIntercepted) {
                            // Already dispatched ACTION_DOWN event to parents, so stop here.
                            return false;
                        }

                        // Apps can set the interception target other than the direct parent.
                        final ViewGroup parent;
                        if (mTouchInterceptionViewGroup == null) {
                            parent = (ViewGroup) getParent();
                        } else {
                            parent = mTouchInterceptionViewGroup;
                        }

                        // Get offset to parents. If the parent is not the direct parent,
                        // we should aggregate offsets from all of the parents.
                        float offsetX = 0;
                        float offsetY = 0;
                        for (View v = this; v != null && v != parent; v = (View) v.getParent()) {
                            offsetX += v.getLeft() - v.getScrollX();
                            offsetY += v.getTop() - v.getScrollY();
                        }
                        final MotionEvent event = MotionEvent.obtainNoHistory(ev);
                        event.offsetLocation(offsetX, offsetY);

                        if (parent.onInterceptTouchEvent(event)) {
                            mIntercepted = true;

                            // If the parent wants to intercept ACTION_MOVE events,
                            // we pass ACTION_DOWN event to the parent
                            // as if these touch events just have began now.
                            event.setAction(MotionEvent.ACTION_DOWN);

                            // Return this onTouchEvent() first and set ACTION_DOWN event for parent
                            // to the queue, to keep events sequence.
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    parent.dispatchTouchEvent(event);
                                }
                            });
                            return false;
                        }
                        // Even when this can't be scrolled anymore,
                        // simply returning false here may cause subView's click,
                        // so delegate it to super.
                        return super.onTouchEvent(ev);
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        mCallbacks = listener;
    }

    @Override
    public void setTouchInterceptionViewGroup(ViewGroup viewGroup) {
        mTouchInterceptionViewGroup = viewGroup;
    }

    @Override
    public void scrollVerticallyTo(int y) {
        View firstVisibleChild = getChildAt(0);
        if (firstVisibleChild != null) {
            int baseHeight = firstVisibleChild.getHeight();
            int position = y / baseHeight;
            scrollVerticallyToPosition(position);
        }
    }

    /**
     * <p>Same as {@linkplain #scrollToPosition(int)} but it scrolls to the position not only make
     * the position visible.</p>
     * <p>It depends on {@code LayoutManager} how {@linkplain #scrollToPosition(int)} works,
     * and currently we know that {@linkplain LinearLayoutManager#scrollToPosition(int)} just
     * make the position visible.</p>
     * <p>In LinearLayoutManager, scrollToPositionWithOffset() is provided for scrolling to the position.
     * This method checks which LayoutManager is set,
     * and handles which method should be called for scrolling.</p>
     * <p>Other know classes (StaggeredGridLayoutManager and GridLayoutManager) are not tested.</p>
     *
     * @param position position to scroll
     */
    private void scrollVerticallyToPosition(int position) {
        RecyclerView.LayoutManager lm = getLayoutManager();

        if (lm != null && lm instanceof LinearLayoutManager) {
            ((LinearLayoutManager) lm).scrollToPositionWithOffset(position, 0);
        } else {
            scrollToPosition(position);
        }
    }

    @Override
    public int getCurrentScrollY() {
        return mScrollY;
    }

    private void init() {
        mChildrenHeights = new SparseIntArray();
    }

    /**
     * This saved state class is a Parcelable and should not extend
     * {@link BaseSavedState} nor {@link android.view.AbsSavedState}
     * because its super class AbsSavedState's constructor
     * {@link android.view.AbsSavedState#AbsSavedState(Parcel)} currently passes null
     * as a class loader to read its superstate from Parcelable.
     * This causes {@link android.os.BadParcelableException} when restoring saved states.
     * <p/>
     * The super class "RecyclerView" is a part of the support library,
     * and restoring its saved state requires the class loader that loaded the RecyclerView.
     * It seems that the class loader is not required when restoring from RecyclerView itself,
     * but it is required when restoring from RecyclerView's subclasses.
     */
    static class SavedState implements Parcelable {
        public static final SavedState EMPTY_STATE = new SavedState() {
        };

        int prevFirstVisiblePosition;
        int prevFirstVisibleChildHeight = -1;
        int prevScrolledChildrenHeight;
        int prevScrollY;
        int scrollY;
        SparseIntArray childrenHeights;

        // This keeps the parent(RecyclerView)'s state
        Parcelable superState;

        /**
         * Called by EMPTY_STATE instantiation.
         */
        private SavedState() {
            superState = null;
        }

        /**
         * Called by onSaveInstanceState.
         */
        SavedState(Parcelable superState) {
            this.superState = superState != EMPTY_STATE ? superState : null;
        }

        /**
         * Called by CREATOR.
         */
        private SavedState(Parcel in) {
            // Parcel 'in' has its parent(RecyclerView)'s saved state.
            // To restore it, class loader that loaded RecyclerView is required.
            Parcelable superState = in.readParcelable(RecyclerView.class.getClassLoader());
            this.superState = superState != null ? superState : EMPTY_STATE;

            prevFirstVisiblePosition = in.readInt();
            prevFirstVisibleChildHeight = in.readInt();
            prevScrolledChildrenHeight = in.readInt();
            prevScrollY = in.readInt();
            scrollY = in.readInt();
            childrenHeights = new SparseIntArray();
            final int numOfChildren = in.readInt();
            if (0 < numOfChildren) {
                for (int i = 0; i < numOfChildren; i++) {
                    final int key = in.readInt();
                    final int value = in.readInt();
                    childrenHeights.put(key, value);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(superState, flags);

            out.writeInt(prevFirstVisiblePosition);
            out.writeInt(prevFirstVisibleChildHeight);
            out.writeInt(prevScrolledChildrenHeight);
            out.writeInt(prevScrollY);
            out.writeInt(scrollY);
            final int numOfChildren = childrenHeights == null ? 0 : childrenHeights.size();
            out.writeInt(numOfChildren);
            if (0 < numOfChildren) {
                for (int i = 0; i < numOfChildren; i++) {
                    out.writeInt(childrenHeights.keyAt(i));
                    out.writeInt(childrenHeights.valueAt(i));
                }
            }
        }

        public Parcelable getSuperState() {
            return superState;
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
