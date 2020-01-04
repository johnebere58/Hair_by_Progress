package com.hairbyprogress.base;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hairbyprogress.MyApplication;
import com.hairbyprogress.OnComplete;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hairbyprogress.base.Base.*;


public class BaseModel {

	public HashMap<String, Object> items = new HashMap<>();

	private boolean hasMore;

	public BaseModel(){

	}
	public BaseModel(String title,String sub_title,int color,int icon){
		put(TITLE,title);
		put(SUB_TITLE,sub_title);
		put(COLORS,color);
		put(ICONS,icon);
	}

	public BaseModel(HashMap<String, Object> items){
		this.items = items;
	}


	public BaseModel(DocumentSnapshot doc){
		this.items = (HashMap<String, Object>) doc.getData();
		this.items.put(OBJECT_ID,doc.getId());
	}

	public BaseModel(Map<String, Object> items){
		this.items = (HashMap<String, Object>) items;
	}


	public void put(String key,Object value){
		items.put(key, value);
	}

	public void remove(String key){
		items.remove(key);
	}

	public String getObjectId(){
		Object value = items.get(OBJECT_ID);
		return value==null?"":value.toString();
	}


	public List getList(String key){
		Object value = items.get(key);
	    return value==null?new ArrayList(): (List) value;
	}

	public ArrayList<Object> addToList(String key,Object value,boolean add){
		ArrayList<Object> list = (ArrayList<Object>) items.get(key);
		list = list==null?new ArrayList<Object>():list;
		if(add){
			/*if(!list.contains(value))*/list.add(value);
		}else{
			list.removeAll(Collections.singleton(value));
		}
		put(key,list);
	    return list;
	}
	public ArrayList<Object> addOnceToList(String key,Object value,boolean add){
		ArrayList<Object> list = (ArrayList<Object>) items.get(key);
		list = list==null?new ArrayList<Object>():list;
		if(add){
			if(!list.contains(value))list.add(value);
		}else{
			list.removeAll(Collections.singleton(value));
		}
		put(key,list);
	    return list;
	}
	public ArrayList<HashMap<String,Object>> addOnceToMap(String mapName,BaseModel bm,boolean add){
		ArrayList<HashMap<String,Object>> maps = (ArrayList<HashMap<String,Object>>) items.get(mapName);
		maps = maps==null? new ArrayList<HashMap<String, Object>>() :maps;
		boolean canAdd = true;
		for(HashMap<String,Object> theMap : maps){
			BaseModel model = new BaseModel(theMap);
			if(model.getString(OBJECT_ID).equals(bm.getString(OBJECT_ID))){
				canAdd=false;
				if(!add)maps.remove(theMap);
				break;
			}
		}
		if(canAdd && add){
			maps.add(bm.items);
		}

		put(mapName,maps);
	    return maps;
	}
	public boolean hasMap(String mapName,BaseModel bm){
		ArrayList<HashMap<String,Object>> maps = (ArrayList<HashMap<String,Object>>) items.get(mapName);
		maps = maps==null? new ArrayList<HashMap<String, Object>>() :maps;
		boolean canAdd = true;
		for(HashMap<String,Object> theMap : maps){
			BaseModel model = new BaseModel(theMap);
			if(model.getString(OBJECT_ID).equals(bm.getString(OBJECT_ID))){
				return true;
			}
		}
		return false;
	}



	public Map getMap(String key){
	    Object value = items.get(key);
	    return value==null?new HashMap<String,String>(): (Map) value;
	}


	public Object get(String key){
        return items.get(key);
	}

	public String getUserId(){
		Object value = items.get(USER_ID);

		return value==null?"":value.toString();
	}
	public String getUserImage(){
		Object value = items.get(USER_IMAGE);
		return value==null?"":value.toString();
	}

	public String getUserCoverImage(){
		Object value = items.get(COVER_IMAGE);
		return value==null?"":value.toString();
	}


	public String getUserName(){
		Object value = items.get(USERNAME);
		String name = value==null?"":value.toString();
		if(name.length()>1){
			name = name.substring(0,1).toUpperCase()+name.substring(1);
		}
		return name;
	}


	public String getString(String key){
		Object value = items.get(key);

		return value==null?"":value.toString();
	}

	public String getEmail(){
		Object value = items.get(EMAIL);
		return value==null?"":value.toString();
	}
	public String getPassword(){
		Object value = items.get(PASSWORD);
		return value==null?"":value.toString();
	}

	public long getCreatedAt(){
		Object value = items.get(CREATED_AT);
		return value==null?0:((Date) value).getTime();
	}
	public long getCreatedAt(String time){
		Object value = items.get(time);
		return value==null?0:((Date) value).getTime();
	}

	public Date getCreatedAtDate(){
		Object value = items.get(CREATED_AT);
		return value==null?new Date():((Date) value);
	}
	public Date getUpdatedAtDate(){
		Object value = items.get(UPDATED_AT);
		return value==null?new Date():((Date) value);
	}


	public long getUpdatedAt(){
		Object value = items.get(UPDATED_AT);
		return value==null?0:((Date) value).getTime();
	}

	public boolean isRead(){
		ArrayList<String> readBy = (ArrayList<String>) getList(READ_BY);
		return readBy.contains(MyApplication.userModel.getObjectId());
	}
	public boolean isRead(String userId){
		ArrayList<String> readBy = (ArrayList<String>) getList(READ_BY);
		return readBy.contains(userId);
	}

	public boolean isMuted(String chatId){
		ArrayList<String> readBy = (ArrayList<String>) getList(MUTED);
		return readBy.contains(chatId);
	}
	public boolean isSilenced(){
		ArrayList<String> silence = (ArrayList<String>) getList(SILENCED);
		return silence.contains(MyApplication.userModel.getObjectId());
	}

	public boolean isKicked(){
		ArrayList<String> readBy = (ArrayList<String>) getList(KICKED_OUT);
		return readBy.contains(MyApplication.userModel.getObjectId());
	}

	public boolean isMale(){
		return getInt(GENDER)==0;
	}

	public void setRead(){
		ArrayList<String> readBy = (ArrayList<String>) getList(READ_BY);
		if(!readBy.contains(MyApplication.userModel.getObjectId())){
			readBy.add(MyApplication.userModel.getObjectId());
			put(READ_BY,readBy);
		}
		updateItem();
	}


	public void setUnRead(){
		ArrayList<String> readBy = (ArrayList<String>) getList(READ_BY);
		readBy.removeAll(Collections.singleton(MyApplication.userModel.getObjectId()));
		updateItem();
	}

	public boolean myItem(){
		return getUserId().equals(MyApplication.userModel.getUserId());
	}

	public boolean isHidden(){
		ArrayList<String> readBy = (ArrayList<String>) getList(HIDDEN);
		return readBy.contains(MyApplication.userModel.getObjectId());
	}

	
	public int getInt(String key){
		Object value = items.get(key);
		return value==null?0: ((Number) value).intValue();
	}
	public int getType(){
		Object value = items.get(TYPE);
		return value==null?0: ((Number) value).intValue();
	}

	public Double getDouble(String key){
		Object value = items.get(key);
		return value==null?0: (Double) value;
	}

	public long getTime(){
		Object value = items.get(TIME);
		return value==null?0: ((Number) value).longValue();
	}
	public long getLong(String key){
		Object value = items.get(key);
		return value==null?0: ((Number) value).longValue();
	}


	public CharSequence getChar(String key){
		Object value = items.get(key);
		return value==null?"": (CharSequence)value;
	}
	public Boolean getBoolean(String key){
		Object value = items.get(key);
		return value==null?false: (Boolean) value;
	}


	public Boolean isAdminItem(){
		return getBoolean(IS_ADMIN);
	}
	public Boolean isJohn(){
		return getEmail().equals("johnebere58@gmail.com");
	}


	public Boolean isHiring(){
		Object value = items.get(IS_HIRING);
		return value==null?false: (Boolean) value;
	}


	public JSONObject getJsonObject(String key){
		Object value = items.get(key);
		return value==null?new JSONObject(): (JSONObject) value;
	}

	public void updateItem(){
		String dName = (String) items.get(DATABASE_NAME);
		if(dName==null ||dName.isEmpty())return;

		String id = (String) items.get(OBJECT_ID);

		items.put(UPDATED_AT,FieldValue.serverTimestamp());
		items.put(FULL_MODE,false);

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(dName).document(id).set(items);
	}

	public void updateItem(OnCompleteListener onCompleteListener){
		String dName = (String) items.get(DATABASE_NAME);
		if(dName==null ||dName.isEmpty())return;

		String id = (String) items.get(OBJECT_ID);

		items.put(UPDATED_AT,FieldValue.serverTimestamp());
		items.put(FULL_MODE,false);

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(dName).document(id).set(items).addOnCompleteListener(onCompleteListener);
	}
	public void updateItem(String dName,String id,OnCompleteListener onCompleteListener){
		items.put(UPDATED_AT,FieldValue.serverTimestamp());
		items.put(FULL_MODE,false);

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(dName).document(id).set(items).addOnCompleteListener(onCompleteListener);
	}


    public void updateItem(final String cName, final String docId, final String key, final Object value, final OnComplete onCompleteListener){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference docRef = db.collection(cName).document(docId);
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if(task.isSuccessful()){
					DocumentSnapshot doc = task.getResult();
					if(doc.exists()){
						BaseModel model = new BaseModel(doc);
						model.put(key, value);
						model.updateItem();
						if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
					}else{
						final BaseModel model = new BaseModel();
						model.put(key,value);
						model.saveItem(cName, docId, new OnCompleteListener() {
							@Override
							public void onComplete(@NonNull Task task) {
								if(!task.isSuccessful()){
									if(onCompleteListener!=null)onCompleteListener.onComplete(
											EMPTY,null);
									return;
								}
								if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
							}
						});

					}
				}else{
					if(onCompleteListener!=null){
						onCompleteListener.onComplete(
								task.getException()==null?"Error":task.getException().getMessage(),null);
					}
				}
			}
		});
	}

	public void updateListItem(String cName, String docId, final String key, final Object value, final boolean add, final OnComplete onCompleteListener){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference docRef = db.collection(cName).document(docId);
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if(task.isSuccessful()){
					DocumentSnapshot doc = task.getResult();
					if(doc.exists()){
						BaseModel model = new BaseModel(doc);
						model.addOnceToList(key, value,add);
						model.updateItem();
						if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
					}else{
						if(onCompleteListener!=null)onCompleteListener.onComplete(
								EMPTY,null);
					}
				}else{
					if(onCompleteListener!=null)onCompleteListener.onComplete(
							task.getException()==null?"Error":task.getException().getMessage(),null);
				}
			}
		});
	}

	public void getObject(String cName, String docId,final OnComplete onCompleteListener){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference docRef = db.collection(cName).document(docId);
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if(task.isSuccessful()){
					DocumentSnapshot doc = task.getResult();
					if(doc.exists()){
						BaseModel model = new BaseModel(doc);
						if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
					}else{
						if(onCompleteListener!=null)onCompleteListener.onComplete(
								EMPTY,null);
					}
				}else{
					if(onCompleteListener!=null)onCompleteListener.onComplete(
							task.getException()==null?"Error":task.getException().getMessage(),null);
				}
			}
		});
	}

	public void getObject(Query query, final OnComplete onCompleteListener){
		query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if(task.isSuccessful()){
					QuerySnapshot qr = task.getResult();
					if(qr.getDocuments().isEmpty()){
						if(onCompleteListener!=null)onCompleteListener.onComplete(
								EMPTY,null);
						return;
					}
					DocumentSnapshot doc = qr.getDocuments().iterator().next();
					if(doc.exists()){
						BaseModel model = new BaseModel(doc);
						if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
					}else{
						if(onCompleteListener!=null)onCompleteListener.onComplete(
								EMPTY,null);
					}
				}else{
					if(onCompleteListener!=null)onCompleteListener.onComplete(
							task.getException()==null?"Error":task.getException().getMessage(),null);
				}
			}
		});
	}

    public void getObjectList(Query query, final OnComplete onCompleteListener){
		query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if(task.isSuccessful()){
					QuerySnapshot qr = task.getResult();
					ArrayList<BaseModel> models = new ArrayList<>();
					for(DocumentSnapshot doc:qr.getDocuments()){
						BaseModel model = new BaseModel(doc);
						models.add(model);
					}
					if(onCompleteListener!=null)onCompleteListener.onComplete(null,models);
				}else{
					if(onCompleteListener!=null)onCompleteListener.onComplete(
							task.getException()==null?"Error":task.getException().getMessage(),null);
				}
			}
		});
	}





	public void increaseItem(String cName, String docId, final String key, final boolean increase, final OnComplete onCompleteListener){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference docRef = db.collection(cName).document(docId);
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if(task.isSuccessful()){
					DocumentSnapshot doc = task.getResult();
					if(doc.exists()){
						BaseModel model = new BaseModel(doc);
						int value = model.getInt(key);
						value = increase?value+1:value-1;
						model.put(key, value);
						model.updateItem();
						if(onCompleteListener!=null)onCompleteListener.onComplete(null,model);
					}else{
						if(onCompleteListener!=null)onCompleteListener.onComplete(
								EMPTY,null);
					}
				}else{
					if(onCompleteListener!=null)onCompleteListener.onComplete(
							task.getException()==null?"Error":task.getException().getMessage(),null);
				}
			}
		});
	}



	public void deleteItem(){
		String dName = (String) items.get(DATABASE_NAME);
		String id = (String) items.get(OBJECT_ID);

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(dName).document(id).delete();
	}

	private FirebaseFirestore processSave(String name){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		items.put(DATABASE_NAME,name);
		items.put(UPDATED_AT, FieldValue.serverTimestamp());
		items.put(CREATED_AT,FieldValue.serverTimestamp());
		items.put(TIME,System.currentTimeMillis());
		//items.put(FULL_MODE,false);
		/*if(!name.equals(USER_BASE) && !name.equals(REPORT_PROFILE)
				&& !name.equals(APP_SETTINGS_BASE)
				&& !name.equals(NOTIFY_BASE)
				)BaseActivity.addMyDetailsToModel(items);*/
		return db;
	}

	public void saveItem(String name){
		processSave(name).collection(name).add(items);
	}

	public void saveItem(String name, OnSuccessListener successListener, OnFailureListener onFailureListener){
		processSave(name).collection(name).add(items)
				.addOnSuccessListener(successListener).addOnFailureListener(onFailureListener);
	}
	public void saveItem(String name,String document, OnSuccessListener successListener, OnFailureListener onFailureListener){
		items.put(OBJECT_ID,document);
		processSave(name).collection(name).document(document).set(items)
				.addOnSuccessListener(successListener).addOnFailureListener(onFailureListener);
	}

    public void saveItem(String name,String document){
		items.put(OBJECT_ID,document);
		processSave(name).collection(name).document(document).set(items);
	}

	public void justSave(String name,String document, OnCompleteListener onCompleteListener){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		items.put(OBJECT_ID,document);
		items.put(DATABASE_NAME,name);
		items.put(UPDATED_AT, FieldValue.serverTimestamp());
		items.put(CREATED_AT,FieldValue.serverTimestamp());
		items.put(TIME,System.currentTimeMillis());
		//items.put(FULL_MODE,false);
		db.collection(name).document(document).set(items).addOnCompleteListener(onCompleteListener);
	}



	public void saveItem(String name,String document, OnCompleteListener onCompleteListener){
		items.put(OBJECT_ID,document);
		processSave(name).collection(name).document(document).set(items)
				.addOnCompleteListener(onCompleteListener);

	}
	public void saveItem(String name,OnCompleteListener onCompleteListener){
		processSave(name).collection(name).add(items)
				.addOnCompleteListener(onCompleteListener);

	}

	public void uploadFile(String path, final OnComplete onComplete){
		File f = new File(path);
		final String fileName = f.getName();
		Uri file = Uri.fromFile(f);
		final String ref = BaseActivity.getRandomId();

		StorageReference storageReference =  FirebaseStorage.getInstance().getReference();
		final StorageReference riversRef = storageReference.child(ref);
		riversRef.putFile(file)
				.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

						riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
							@Override
							public void onSuccess(Uri uri) {
								BaseModel model = new BaseModel();
								model.put(NAME, fileName);
								model.put(FILE_URL, uri.toString());
								model.put(REFERENCE,ref);
								model.saveItem(REFERENCE_BASE);
								onComplete.onComplete(null,uri.toString());
							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								onComplete.onComplete(e.getMessage(),null);
							}
						});



					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception exception) {

						onComplete.onComplete(exception.getMessage(),null);
					}
				});
	}

}
