package com.hairbyprogress.base;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.adapters.BaseAdapter;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;

import static com.hairbyprogress.base.Base.CREATED_AT;
import static com.hairbyprogress.base.Base.LOAD_CURRENT;
import static com.hairbyprogress.base.Base.LOAD_PREVIOUS;
import static com.hairbyprogress.base.Base.LOAD_REFRESH;

public abstract class BaseActivityWithRecyclerView extends BaseActivity {

    protected ArrayList<BaseModel> mainList = new ArrayList<>();

    private ArrayList<String> loadedIds = new ArrayList<>();

    String cName;
    Query query;
    boolean canPage;
    boolean canSwipe;
    protected int dummyType;

    protected void setUp(String cName,boolean canSwipe,boolean canPage){
    this.cName = cName;
    this.canPage= canPage;
    this.canSwipe= canSwipe;

     setRecycler();

     if(cName!=null){
         startLoading();
     }
    }

    protected void setUp( Query query,boolean canSwipe,boolean canPage){
    this.query = query;
    this.canPage= canPage;
    this.canSwipe= canSwipe;

    setRecycler();

     if(query!=null){
         startLoading();
     }
    }

    protected void setUpWithSnapshot(final Query query){

    query.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        getRecyclerView().hideLoading();
                        if(e!=null || snapshot==null){
                            if(mainList.isEmpty()){
                                showEmpty();
                            }
                            return;
                        }

                        final boolean current = mainList.isEmpty();

                        for(QueryDocumentSnapshot documentSnapshot:snapshot) {
                            BaseModel model = new BaseModel(documentSnapshot);
                            updateInMainList(!current,model);
                        }
                        notifyChanges();
                    }
                });
    }

    protected void setUpWithListSnapshot(String cName){
        this.cName = cName;
        setRecycler();
    }

    protected void loadListSnapshot(ArrayList<String> docIds) {

        for (String id : docIds) {

            if (loadedIds.contains(id))continue;
            loadedIds.add(id);

            FirebaseFirestore.getInstance().collection(cName).document(id).
                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            getRecyclerView().hideLoading();
                            if(e!=null){
                                return;
                            }
                            if(documentSnapshot!=null && documentSnapshot.exists()){
                                BaseModel model = new BaseModel(documentSnapshot);
                                updateInMainList(true,model);
                                }
                            notifyChanges();
                        }
                    });
        }
        if(docIds.isEmpty() && mainList.isEmpty()){
            showEmpty();
        }
    }


    protected void setRecycler(){
        setUpRecycleView(getRecyclerView(),
                getLayoutManager()==null?new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
                        :getLayoutManager()
                ,canPage?getPagingListener():null,canSwipe?getRefreshListener():null);
        getRecyclerView().setAdapter(getAdapter());
    }

    private void startLoading(){

        getRecyclerView().showLoading();

        loadQuery(LOAD_CURRENT);

    }

    public abstract MyRecyclerView getRecyclerView();

    public abstract BaseAdapter getAdapter();

    public abstract RecyclerView.LayoutManager getLayoutManager();

    private MyRecyclerView.PagingableListener getPagingListener(){
        return new MyRecyclerView.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                loadQuery(LOAD_PREVIOUS);
            }
        };
    }
    public SwipeRefreshLayout.OnRefreshListener getRefreshListener(){
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadQuery(mainList.isEmpty()?LOAD_CURRENT:LOAD_REFRESH);
            }
        };
    }

    protected void updateInMainList(boolean isNew,BaseModel model){
        if(MyApplication.userModel.getList(BLOCKED).contains(model.getUserId())){
            model.put(TYPE,TYPE_HIDDEN);
        }
        String objectId1 = model.getObjectId();
        for(int i=0;i<mainList.size();i++){
            String objectId2 = mainList.get(i).getObjectId();
            if(objectId1.equals(objectId2)){
                mainList.set(i,model);
                //getRecyclerView().getRecyclerView().getAdapter().notifyItemChanged(i);
                notifyChanges();
                return;
            }
        }

        if(isNew){
            mainList.add(0,model);
        }else{
            mainList.add(model);
        }
    }


    public abstract int getListMax();

    private void loadQuery(final int loadWhat){
        if(query!=null){
            loadQueryFromCollection(loadWhat);
            return;
        }

        if(loadWhat==LOAD_CURRENT) {
                    db.collection(cName)
                    .limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                    .get().addOnCompleteListener(onCompleteListener(loadWhat));
        }
        if(loadWhat==LOAD_REFRESH) {
                    db.collection(cName)
                    .limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.ASCENDING)
                    .whereGreaterThan(CREATED_AT, getNewestTimeFromList(mainList))
                    .get().addOnCompleteListener(onCompleteListener(loadWhat));
        }
        if(loadWhat==LOAD_PREVIOUS) {
                    db.collection(cName)
                    .limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                    .whereLessThan(CREATED_AT, getOldestTimeFromList(mainList))
                    .get().addOnCompleteListener(onCompleteListener(loadWhat));
        }

    }

    private void loadQueryFromCollection(int loadWhat){
        if(loadWhat==LOAD_CURRENT) {
            query.limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                    .get().addOnCompleteListener(onCompleteListener(loadWhat));
        }
        if(loadWhat==LOAD_REFRESH) {
            query.limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.ASCENDING)
                    .whereGreaterThan(CREATED_AT, getNewestTimeFromList(mainList))
                    .get().addOnCompleteListener(onCompleteListener(loadWhat));
        }
        if(loadWhat==LOAD_PREVIOUS) {
            query.limit(getListMax())
                    .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                    .whereLessThan(CREATED_AT, getOldestTimeFromList(mainList)).get().addOnCompleteListener(onCompleteListener(loadWhat));
        }

    }

    private OnCompleteListener onCompleteListener(final int loadWhat){
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                getRecyclerView().setOnRefreshComplete();
                getRecyclerView().hideLoading();

                if(!task.isSuccessful()){
                    if(loadWhat==LOAD_CURRENT)getRecyclerView().hideLoading(new MyRecyclerView.RetryListener() {
                        @Override
                        public void onRetry() {
                            loadQuery(LOAD_CURRENT);
                        }
                    });
                    return;
                }

                boolean empty = task.getResult().isEmpty();
                if(empty && mainList.isEmpty() && loadWhat==LOAD_CURRENT){
                    showEmpty();
                    return;
                }

                for(QueryDocumentSnapshot shot:task.getResult()){
                    updateInMainList(loadWhat==LOAD_REFRESH,new BaseModel(shot));
                }


                notifyChanges();

                if(loadWhat!=LOAD_REFRESH){
                    getRecyclerView().onFinishLoading(task.getResult().size()==getListMax(),false);
                }
            }
        };
    }

    public abstract void showEmpty();

    @Override
    public void onResume() {
        super.onResume();
        notifyChanges();
    }

    private void notifyChanges(){
        if(getRecyclerView().getRecyclerView().getAdapter()!=null){
            getRecyclerView().getRecyclerView().getAdapter().notifyDataSetChanged();
        }
    }

    public abstract int DummyType();
}
