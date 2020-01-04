package com.hairbyprogress.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;
import com.hairbyprogress.DownloadListener;
import com.hairbyprogress.DownloadTask;
import com.hairbyprogress.MainActivity;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;
import com.hairbyprogress.PreInit;
import com.hairbyprogress.R;
import com.hairbyprogress.SearchMain;
import com.hairbyprogress.base.Base;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import static com.hairbyprogress.MyApplication.AppIsVisible;
import static com.hairbyprogress.MyApplication.appSettingsModel;
import static com.hairbyprogress.MyApplication.cart_items;
import static com.hairbyprogress.MyApplication.dummyProduct;
import static com.hairbyprogress.MyApplication.getCurrentUser;
import static com.hairbyprogress.MyApplication.home_display;
import static com.hairbyprogress.MyApplication.isAdmin;
import static com.hairbyprogress.MyApplication.isJohn;
import static com.hairbyprogress.MyApplication.notificationHasLoaded;
import static com.hairbyprogress.MyApplication.feedbackList;
import static com.hairbyprogress.MyApplication.orderHasLoaded;
import static com.hairbyprogress.MyApplication.order_items;
import static com.hairbyprogress.MyApplication.products;
import static com.hairbyprogress.MyApplication.settingsHasLoaded;
import static com.hairbyprogress.MyApplication.userModel;
import static com.hairbyprogress.base.Base.ACTION;
import static com.hairbyprogress.base.Base.ACTION_DOWNLOAD;
import static com.hairbyprogress.base.Base.ACTION_REFRESH;
import static com.hairbyprogress.base.Base.APPROVED;
import static com.hairbyprogress.base.Base.APP_OPENED;
import static com.hairbyprogress.base.Base.APP_SETTINGS;
import static com.hairbyprogress.base.Base.APP_SETTINGS_BASE;
import static com.hairbyprogress.base.Base.BROADCAST_CHATTING;
import static com.hairbyprogress.base.Base.BROADCAST_DOWNLOAD_COMPLETED;
import static com.hairbyprogress.base.Base.BROADCAST_HOME_DISPLAY;
import static com.hairbyprogress.base.Base.BROADCAST_NOTIFY;
import static com.hairbyprogress.base.Base.BROADCAST_ORDER_UPDATED;
import static com.hairbyprogress.base.Base.BROADCAST_PRODUCTS;
import static com.hairbyprogress.base.Base.BROADCAST_SETUP_MARKET;
import static com.hairbyprogress.base.Base.BROADCAST_UPLOAD_OR_DOWNLOAD;
import static com.hairbyprogress.base.Base.CART_BASE;
import static com.hairbyprogress.base.Base.CATEGORIES_BASE;
import static com.hairbyprogress.base.Base.CATEGORY;
import static com.hairbyprogress.base.Base.CHAT_ID;
import static com.hairbyprogress.base.Base.CONVERSATIONS;
import static com.hairbyprogress.base.Base.CREATED_AT;
import static com.hairbyprogress.base.Base.DEFAULT_POSITION;
import static com.hairbyprogress.base.Base.ERROR;
import static com.hairbyprogress.base.Base.FILE_NAME;
import static com.hairbyprogress.base.Base.FILE_PATH;
import static com.hairbyprogress.base.Base.FILE_URL;
import static com.hairbyprogress.base.Base.HOME_DISPLAY_BASE;
import static com.hairbyprogress.base.Base.IMAGES;
import static com.hairbyprogress.base.Base.ITEM_TAG;
import static com.hairbyprogress.base.Base.LAST_SEEN_STORY;
import static com.hairbyprogress.base.Base.MESSAGE;
import static com.hairbyprogress.base.Base.MESSAGE_TYPE;
import static com.hairbyprogress.base.Base.MUTED;
import static com.hairbyprogress.base.Base.NEW_NOTIFICATION;
import static com.hairbyprogress.base.Base.NEW_UPDATE;
import static com.hairbyprogress.base.Base.NOTIFY_BASE;
import static com.hairbyprogress.base.Base.ORDER_BASE;
import static com.hairbyprogress.base.Base.ORDER_PENDING;
import static com.hairbyprogress.base.Base.PENDING;
import static com.hairbyprogress.base.Base.POSITION;
import static com.hairbyprogress.base.Base.PRICE_BASE;
import static com.hairbyprogress.base.Base.PRODUCT_BASE;
import static com.hairbyprogress.base.Base.PUSH_NOTIFICATION;
import static com.hairbyprogress.base.Base.READ;
import static com.hairbyprogress.base.Base.REMOVED_IDS;
import static com.hairbyprogress.base.Base.SEARCH_KEY;
import static com.hairbyprogress.base.Base.SECTIONS_BASE;
import static com.hairbyprogress.base.Base.SEEN_IDS;
import static com.hairbyprogress.base.Base.STATUS;
import static com.hairbyprogress.base.Base.STORY_BASE;
import static com.hairbyprogress.base.Base.SUB_CATS;
import static com.hairbyprogress.base.Base.TITLE;
import static com.hairbyprogress.base.Base.USER_BASE;
import static com.hairbyprogress.base.Base.USER_ID;

public class AppSettingsService extends Service {
    //BroadcastReceiver refreshReceiver;
    ListenerRegistration listenUser;
    ListenerRegistration listenSettings;
    ListenerRegistration listenCat;
    ArrayList<ListenerRegistration> listenerRegistrations = new ArrayList<>();

    ArrayList<BaseModel> chatList = new ArrayList<>();
    ArrayList<String> chatIds = new ArrayList<>();
    private long lastSoundTime = 0;

    int TYPE_CHAT = 0;
    int TYPE_OPINION = 1;
    int TYPE_NOTIFICATION = 2;
    private boolean listenerSet;
    private boolean adminListenerSet;
    private boolean subListenerSet;
    DownloadReceiver downloadReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setSectionListener();
        setPricesListener();
        setHomeDisplayListener();
        setProductsListener();
        setCartListener();
        setOrderListener();
        setFeedbackListener();
        setSettingsListener();
        createUserListener();

        downloadReceiver = new DownloadReceiver();
        registerReceiver(downloadReceiver,new IntentFilter(BROADCAST_UPLOAD_OR_DOWNLOAD));
    }

    private void setPricesListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(PRICE_BASE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        MyApplication.prices.clear();
                        for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                            BaseModel model = new BaseModel(documentSnapshot);
                            MyApplication.prices.add(model);
                        }

                    }
                });
        listenerRegistrations.add(listen);
    }

    private void setSectionListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(SECTIONS_BASE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        MyApplication.sections.clear();
                        for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                            BaseModel model = new BaseModel(documentSnapshot);
                            MyApplication.sections.add(model);
                        }

                    }
                });
        listenerRegistrations.add(listen);
    }


    private void setHomeDisplayListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(HOME_DISPLAY_BASE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        ArrayList<String> removedIds = new ArrayList<>();
                        for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                            boolean add = documentChange.getType() != DocumentChange.Type.REMOVED;

                            BaseModel model = new BaseModel(documentChange.getDocument());
                            addOnceToList(home_display,model,add);
                            if(!add)removedIds.add(model.getObjectId());
                        }
                        sendBroadcast(new Intent(BROADCAST_HOME_DISPLAY).putStringArrayListExtra(REMOVED_IDS,removedIds));

                    }
                });
        listenerRegistrations.add(listen);
    }

    private void setProductsListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(PRODUCT_BASE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        ArrayList<String> removedIds = new ArrayList<>();
                        for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                            boolean add = documentChange.getType() != DocumentChange.Type.REMOVED;

                            BaseModel model = new BaseModel(documentChange.getDocument());
                            addOnceToList(products,model,add);
                            if(!add)removedIds.add(model.getObjectId());
                        }
                        sendBroadcast(new Intent(BROADCAST_PRODUCTS).putStringArrayListExtra(REMOVED_IDS,removedIds));
                        sendBroadcast(new Intent(BROADCAST_SETUP_MARKET).putStringArrayListExtra(REMOVED_IDS,removedIds));
                    }
                });
        listenerRegistrations.add(listen);
    }

    private void setCartListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(CART_BASE).orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        ArrayList<String> removedIds = new ArrayList<>();
                        for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                            boolean add = documentChange.getType() != DocumentChange.Type.REMOVED;

                            BaseModel model = new BaseModel(documentChange.getDocument());
                            addOnceToList(cart_items,model,add);
                            if(!add)removedIds.add(model.getObjectId());
                        }

                    }
                });
        listenerRegistrations.add(listen);
    }

    private void setOrderListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(ORDER_BASE).orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        ArrayList<String> removedIds = new ArrayList<>();
                        for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                            boolean add = documentChange.getType() != DocumentChange.Type.REMOVED;

                            BaseModel model = new BaseModel(documentChange.getDocument());
                            addOnceToList(order_items,model,add);

                            if(isAdmin && userModel!=null && model.getInt(STATUS)==ORDER_PENDING){
                                ArrayList<String> seenIds = (ArrayList<String>) userModel.getList(SEEN_IDS);
                                if(!seenIds.contains(model.getObjectId())){
                                    seenIds.add(model.getObjectId());
                                    userModel.put(SEEN_IDS,seenIds);
                                    userModel.updateItem();
                                    sendAdminNotification("New Order to Deliver",33);
                                }
                            }

                            if(!add)removedIds.add(model.getObjectId());
                        }
                        orderHasLoaded=true;
                        sendBroadcast(new Intent(BROADCAST_ORDER_UPDATED).putStringArrayListExtra(REMOVED_IDS,removedIds));

                    }
                });
        listenerRegistrations.add(listen);
    }

    private void sendAdminNotification(String text,int code){
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Hair by Progress")
                .setContentText(text)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSound(!playSound()?null:Uri.parse("android.resource://com.hairbyprogress/"+ R.raw.solemn))
                .setContentIntent(pendingIntent);


        if (MyApplication.notificationManager == null) {
            MyApplication.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification notification = notificationBuilder.build();
        MyApplication.notificationManager.notify(code,notificationBuilder.build());


    }

    private void setFeedbackListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(NOTIFY_BASE).orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        ArrayList<String> removedIds = new ArrayList<>();
                        for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                            boolean add = documentChange.getType() != DocumentChange.Type.REMOVED;

                            BaseModel model = new BaseModel(documentChange.getDocument());
                            addOnceToList(feedbackList,model,add);

                            if(isAdmin && userModel!=null && model.getInt(STATUS)==PENDING){
                                ArrayList<String> seenIds = (ArrayList<String>) userModel.getList(SEEN_IDS);
                                if(!seenIds.contains(model.getObjectId())){
                                    seenIds.add(model.getObjectId());
                                    userModel.put(SEEN_IDS,seenIds);
                                    userModel.updateItem();
                                    sendAdminNotification("New Feedback to Approve",22);
                                }
                            }

                            if(!add)removedIds.add(model.getObjectId());
                        }
                        notificationHasLoaded=true;
                        sendBroadcast(new Intent(BROADCAST_NOTIFY).putStringArrayListExtra(REMOVED_IDS,removedIds));

                    }
                });
        listenerRegistrations.add(listen);
    }

    private void setSettingsListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(APP_SETTINGS_BASE).document(APP_SETTINGS)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            return;
                        }
                        if(documentSnapshot!=null && documentSnapshot.exists()){
                            MyApplication.appSettingsModel = new BaseModel(documentSnapshot);
                            HashMap<String,Object> newNotify = (HashMap<String, Object>) appSettingsModel.getMap(NEW_NOTIFICATION);

                            if(!newNotify.keySet().isEmpty()){
                                BaseModel model = new BaseModel(newNotify);
                                long time = model.getTime();
                                long appOpened = PreInit.getLongSettings(AppSettingsService.this,APP_OPENED);
                                String prevId = BaseActivity.getStringSettings(AppSettingsService.this,NEW_NOTIFICATION);
                                if(time>appOpened){
                                    if(prevId!=null && !prevId.equals(model.getObjectId()) /*&&!AppIsVisible*/){
                                        sendNewNotification(model);
                                        BaseActivity.putSettings(AppSettingsService.this,NEW_NOTIFICATION,model.getObjectId());
                                    }
                                }
                            }

                        }
                        settingsHasLoaded=true;
                    }
                });
        listenerRegistrations.add(listen);
    }




    public void addOnceToList(ArrayList<BaseModel> list, BaseModel item,boolean add){
        for(int i=0;i<list.size();i++){
            BaseModel bm = list.get(i);
            if(item.getObjectId().equals(bm.getObjectId())){
                if(!add){
                    list.remove(i);
                }else {
                    list.set(i, item);
                }
                return;
            }
        }
        list.add(item);
    }



    private boolean playSound(){
        long now = System.currentTimeMillis();

        if((now-lastSoundTime)<Base.oneMin){
            return false;
        }

        lastSoundTime = now;
        return true;
    }

    private void Toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String message,int unread,int type){
        if(userModel==null)return;
        if(!userModel.getBoolean(PUSH_NOTIFICATION) || AppIsVisible )return;

        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        if(message==null) {
            intent.putExtra(POSITION, type == TYPE_NOTIFICATION ? 3 : 2);
            intent.putExtra(DEFAULT_POSITION, type == TYPE_CHAT ? 1 : 0);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Oinder")
                .setContentText(message!=null?message:String.format("You have %s unread %s%s",
                        unread,type==TYPE_CHAT?"message":type==TYPE_OPINION?"opinion":"notification",unread>1?"s":""))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSound(!playSound()?null:Uri.parse("android.resource://com.hairbyprogress/"+ R.raw.solemn))
                .setContentIntent(pendingIntent);

        if (MyApplication.notificationManager == null) {
            MyApplication.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        MyApplication.notificationManager.notify(type,notificationBuilder.build());
    }

    private void sendNewNotification(BaseModel model){
        if(!BaseActivity.getBooleanSettings(this,PUSH_NOTIFICATION))return;

        ArrayList<String> images = (ArrayList<String>) model.getList(IMAGES);
        boolean bigStyle = !images.isEmpty();
        String tag = model.getString(ITEM_TAG);

        Intent intent;
        if(!tag.isEmpty()){
            intent = new Intent(this, SearchMain.class);
            intent.putExtra(SEARCH_KEY,tag);
        }else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(model.getString(TITLE))
                .setContentText(model.getString(MESSAGE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSound(!playSound()?null:Uri.parse("android.resource://com.hairbyprogress/"+ R.raw.solemn))
                .setContentIntent(pendingIntent);

        if(bigStyle){
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().setSummaryText(model.getString(MESSAGE)));
        }

        if (MyApplication.notificationManager == null) {
            MyApplication.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification notification = notificationBuilder.build();
        MyApplication.notificationManager.notify(1,notificationBuilder.build());

        final RemoteViews contentView = notification.bigContentView;
        final int big_image =getResources().getIdentifier("android:id/big_picture",null,null);

        if(contentView!=null && bigStyle) {
            try {
                Picasso.with(this).load(images.get(0)).into(contentView,
                        big_image, 1, notification);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    private void refreshMyChatList(){
        ArrayList<String> myChats = (ArrayList<String>) MyApplication.userModel.getList(Base.MY_CHATS);
        for(String id : myChats){
            if(chatIds.contains(id))continue;
            chatIds.add(id);

            setChatListener(id);
        }
    }

    private void countUnread(final String chatId, final long createdAt, final int type){
        new BaseModel().getObjectList(FirebaseFirestore.getInstance().collection(CONVERSATIONS)
                .whereEqualTo(CHAT_ID, chatId), new OnComplete() {
            @Override
            public void onComplete(String error, Object result) {
                if(error!=null){
                    return;
                }
                ArrayList<BaseModel> models = (ArrayList<BaseModel>) result;
                int unread = 0;
                for(BaseModel bm : models){
                    boolean read = bm.isRead();
                    boolean myItem = bm.myItem();
                    if(bm.getCreatedAt()<createdAt)continue;
                    if(!read && !myItem){
                        unread++;

                    }
                }

                if(unread!=0 && !userModel.isMuted(chatId)){
                    sendNotification(null,unread,type);
                }

            }
        });
    }

    private void setChatListener(final String chatId){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(CONVERSATIONS)
                .whereEqualTo(CHAT_ID,chatId)
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null || queryDocumentSnapshots==null){
                    return;
                }

                if(queryDocumentSnapshots.isEmpty())return;
                //BaseModel model = new BaseModel(queryDocumentSnapshots.iterator().next());
                countUnread(chatId,0,TYPE_CHAT);
            }
        });
        listenerRegistrations.add(listen);
    }

    private void refreshOpinion(){
        ArrayList<HashMap<String,Object>> myChats = (ArrayList<HashMap<String, Object>>) MyApplication.userModel.getList(Base.MY_OPINIONS);
        for(HashMap<String,Object> chat : myChats){
            BaseModel model = new BaseModel(chat);
            if(chatIds.contains(model.getObjectId()))continue;
            chatIds.add(model.getObjectId());

            setOpinionListener(model.getObjectId(),model.getLong(CREATED_AT));
        }
    }

    private void setOpinionListener(final String id,final long createdAt){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(Base.STORY_BASE)
                .document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    return;
                }
                if(documentSnapshot!=null && documentSnapshot.exists()){
                    countUnread(id,createdAt,TYPE_OPINION);
                }
            }
        });
        listenerRegistrations.add(listen);
    }

    /*private void setNotifyListener(){
        if(userModel==null)return;

        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(NOTIFY_BASE)
                .whereEqualTo(USER_ID,userModel.getUserId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null || queryDocumentSnapshots==null){
                    return;
                }

                if(queryDocumentSnapshots.isEmpty())return;
                int unread = 0;
                for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    BaseModel model = new BaseModel(documentSnapshot);
                    if(!model.getBoolean(READ)){
                        unread++;
                    }
                }
                if(unread!=0){
                    sendNotification(null,unread,TYPE_NOTIFICATION);
                }
            }
        });
        listenerRegistrations.add(listen);
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void createUserListener(){
        final FirebaseUser currentUser = getCurrentUser();
        if(currentUser==null)return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        listenUser = db.collection(USER_BASE).document(currentUser.getUid())
        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    return;
                }
                if(documentSnapshot!=null && documentSnapshot.exists()){
                    MyApplication.userModel = new BaseModel(documentSnapshot);
                    isAdmin = userModel.isAdminItem();
                    isJohn = userModel.getEmail().equalsIgnoreCase("johnebere58@gmail.com");
                }
            }
        });

        listenSettings = db.collection(APP_SETTINGS_BASE).document(APP_SETTINGS)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    return;
                }
                if(documentSnapshot!=null && documentSnapshot.exists()){
                    MyApplication.appSettingsModel = new BaseModel(documentSnapshot);
                    HashMap<String,Object> newNotify = (HashMap<String, Object>) appSettingsModel.getMap(NEW_NOTIFICATION);
                    BaseModel model = new BaseModel(newNotify);
                    boolean muted = MyApplication.userModel.isMuted(model.getObjectId());
                    String message = model.getString(MESSAGE);
                    if(!message.isEmpty() && !muted && !MyApplication.AppIsVisible
                            && model.getTime()>MyApplication.userModel.getCreatedAt()){
                        sendNotification(message,0,9);
                        MyApplication.userModel.addOnceToList(MUTED,model.getObjectId(),true);
                        MyApplication.userModel.updateItem();
                    }
                }
            }
        });

        listenCat = db.collection(CATEGORIES_BASE)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e!=null ){
                            return;
                        }
                        if(queryDocumentSnapshots==null)return;

                        MyApplication.categories.clear();
                        for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                            BaseModel model = new BaseModel(documentSnapshot);
                            String cat = model.getString(TITLE);
                            MyApplication.categories.add(cat);
                        }
                    }
                });
     }

     private void makeAdminListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(STORY_BASE).whereEqualTo(STATUS,PENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            sendNotification("New stories to approve", 0, 10);
                        }
                    }
                });
        listenerRegistrations.add(listen);
     }

     private void makeSubListener(){
        ListenerRegistration listen = FirebaseFirestore.getInstance().collection(STORY_BASE).whereEqualTo(STATUS,APPROVED)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty() && userModel!=null) {
                            //sendNotification("New stories to approve", 0, 10);
                            for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()) {
                                BaseModel model = new BaseModel(documentSnapshot);
                                if(userModel.getList(SUB_CATS).contains(model.getString(CATEGORY))
                                        && model.getCreatedAt() > BaseActivity.getLastSeenStory(AppSettingsService.this)){
                                    sendNotification("New stories from your subscribed categories", 0, 12);
                                }
                            }
                        }
                    }
                });
        listenerRegistrations.add(listen);
     }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(downloadReceiver!=null){
            try {
                unregisterReceiver(downloadReceiver);
            }catch (Exception e){e.printStackTrace();};
        }
        if(listenUser!=null){
            listenUser.remove();
        }
        if(listenSettings!=null){
            listenSettings.remove();
        }
        if(listenCat!=null){
            listenCat.remove();
        }
        try {
        for(ListenerRegistration listen:listenerRegistrations){
            listen.remove();
        }}catch (Exception e){};
    }

    /*private class RefreshReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(userModel==null)return;
            refreshMyChatList();
            refreshOpinion();
        }
    }*/

    public class DownloadReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(ACTION);
            String fileUrl = intent.getStringExtra(FILE_URL);
            String filePath = intent.getStringExtra(FILE_PATH);
            String fileName = intent.getStringExtra(FILE_NAME);

            if(action.equals(ACTION_DOWNLOAD)){
                DownloadTask downloadTask = new DownloadTask(fileUrl, filePath,
                        fileName, new DownloadListener() {
                    @Override
                    public void onComplete(String error, Object result) {
                        MyApplication.currentlyUploadingOrDownloading.remove(fileName);
                        Intent in = new Intent(BROADCAST_DOWNLOAD_COMPLETED);
                        in.putExtra(FILE_NAME,fileName);
                        in.putExtra(ERROR,error);
                        sendBroadcast(in);
                    }

                    @Override
                    public void onProgress(int progress) {
                    }
                });
                downloadTask.execute();
            }
        }
    }
}
