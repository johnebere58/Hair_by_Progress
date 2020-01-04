package com.hairbyprogress;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hairbyprogress.adapters.AddressAdapter;
import com.hairbyprogress.adapters.CartAdapter;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;
import com.hairbyprogress.recyclerview.MyRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class CartMain extends BaseActivity {

    @BindView(R.id.vp) CustomViewPager vp;
    @BindView(R.id.tab) LinearLayout tab;
    @BindView(R.id.next) public View next;
    @BindView(R.id.next_tv) public TextView next_tv;
    @BindView(R.id.next_icon) public ImageView next_icon;
    int vpSize = 3;

    public BaseModel selectedAddress;
    public ArrayList<BaseModel> mainList = new ArrayList<>();

    public int itemsCount = 0;
    public int totalPayment = 0;
    public int grandTotal = 0;
    public int amountPaid = 0;

    public String orderId = getRandomIdShort();

    public boolean hasPaid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cart_main);
        ButterKnife.bind(this);

        vp.setPagingEnabled(false);

        vp.setOffscreenPageLimit(vpSize);
        vp.setAdapter(new PageAdapter(getFragmentManager()));
        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                hideKeyboard(vp);
                setSelection(position);

                if(position==0){
                    next_tv.setText("DELIVERY ADDRESS");
                    next_icon.setImageResource(R.drawable.ic_next1);
                    next_icon.setVisibility(View.VISIBLE);
                }
                if(position==1){
                    next_tv.setText("PAYMENT METHOD");
                    next_icon.setImageResource(R.drawable.ic_next1);
                    next_icon.setVisibility(View.VISIBLE);
                }
                if(position==2){
                    next_tv.setText("PAY AND PLACE ORDER");
                    next_icon.setImageResource(R.drawable.ic_cart);
                    next_icon.setVisibility(View.GONE);
                }

            }
        });

    }

    private void setSelection(int p){
        for(int i=0;i<tab.getChildCount();i++){
            View v = tab.getChildAt(i);
            if(i<=p){
                v.setAlpha(1f);
            }else{
                v.setAlpha(.3f);
            }
        }
    }

   private class PageAdapter extends FragmentPagerAdapter {

      public PageAdapter(FragmentManager fm) {
           super(fm);
       }


       @Override
       public Fragment getItem(int position) {
           ImageFragments fragments = new ImageFragments();
           Bundle args = new Bundle();
           args.putInt(POSITION,position);
           fragments.setArguments(args);
           return fragments;
       }

       @Override
       public int getCount() {
           return vpSize;
       }
   }

   public static class ImageFragments extends Fragment {

       int position;
       @Nullable @BindView(R.id.rsv) MyRecyclerView rsv;
       @Nullable @BindView(R.id.itemstv) TextView itemstv;
       @Nullable @BindView(R.id.add_but) View add_but;
       @Nullable @BindView(R.id.pay_lay) LinearLayout pay_lay;
       @Nullable @BindView(R.id.items_tv) TextView items_tv;
       @Nullable @BindView(R.id.total_tv) TextView total_tv;
       @Nullable @BindView(R.id.d_fee_tv) TextView d_fee_tv;
       @Nullable @BindView(R.id.g_total_v) TextView g_total_v;
       @Nullable @BindView(R.id.part_tv) TextView part_tv;

       Context context;
       Activity activity;
       CartMain mainActivity;
       MaterialDialog md;

       AddressAdapter addressAdapter;
       ArrayList<BaseModel> addressList = new ArrayList<>();

       CartAdapter cartAdapter;
       int paymentMethod = PAY_FULLY;

       @Override
       public void onCreate(@Nullable Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           Bundle bundle = getArguments();
           position = bundle.getInt(POSITION);
           context = getActivity();
           activity = getActivity();
           mainActivity = (CartMain)activity;

       }

       @Override
       public void setRetainInstance(boolean retain) {
           super.setRetainInstance(true);
       }

       @Nullable
       @Override
       public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
           ViewGroup rootView = (ViewGroup) inflater
                   .inflate(position==0?R.layout.cart_page1:position==1?R.layout.cart_page2:R.layout.cart_page3, container, false);

           ButterKnife.bind(this,rootView);

           if(position==0)mainActivity.next.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   clickNext();
               }
           });

           if(position==0){
               if(MyApplication.cart_items.isEmpty()){
                   Toast("Cart is empty");
                   resetMyOrder();
                   activity.finish();
                   return rootView;
               }

               cartAdapter = new CartAdapter(activity,mainActivity.mainList);
               cartAdapter.setOnTotalChanged(new OnComplete() {
                   @Override
                   public void onComplete(String error, Object result) {
                       calcPrice();
                   }
               });

               mainActivity.setUpRecycleView(rsv,new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false),null,null);
               rsv.setAdapter(cartAdapter);

               loadItems();
           }

           if(position==1){
               addressAdapter = new AddressAdapter(activity,addressList);
               rsv.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
               rsv.setAdapter(addressAdapter);

               refreshAddress();

               add_but.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       MyApplication.dummyAddress=null;
                       startActivityForResult(new Intent(context,AddAddress.class),11);
                   }
               });
           }

           if(position==2){
               for(int i=0;i<pay_lay.getChildCount();i++){
                   View view = pay_lay.getChildAt(i);
                   final ImageView check = view.findViewWithTag("check");
                   final int finalI = i;
                   view.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {

                           int maxAmount = 60000;
                           if(MyApplication.appSettingsModel!=null){
                               int m = MyApplication.appSettingsModel.getInt(MAX_PENDING_AMOUNT);
                               maxAmount = m==0?maxAmount:m;
                           }

                           if(finalI==PAY_ON_DELIVERY && mainActivity.grandTotal>maxAmount){
                               new MaterialDialog.Builder(context)
                                       .content(String.format("This option is only available for order below N%s",
                                               mainActivity.formatNumber(maxAmount)))
                                       .positiveText("OK")
                                       .show();
                               return;
                           }

                           resetPayLayout(-1);
                           paymentMethod = finalI;
                           check.setImageResource(R.drawable.ic_check);

                           if(paymentMethod==0){
                               mainActivity.next_tv.setText("PLACE ORDER");
                           }else{
                               mainActivity.next_tv.setText("PAY AND PLACE ORDER");
                           }
                       }
                   });
               }

           }

           return rootView;
       }

       private void resetPayLayout(int position){
           for(int i=0;i<pay_lay.getChildCount();i++){
               View view = pay_lay.getChildAt(i);
               ImageView check = view.findViewWithTag("check");
               check.setImageResource(0);
               if(position==i)check.setImageResource(R.drawable.ic_check);
           }
       }

       private void refreshAddress(){

            ArrayList<HashMap<String,Object>> addresses = (ArrayList<HashMap<String, Object>>) MyApplication.userModel.getList(MY_ADDRESS);

            if(addresses.isEmpty()){
                rsv.showEmpty(R.drawable.ic_deliver,"No Delivery Address Yet","Click below to add a delivery address");
                return;
            }

            for(HashMap<String,Object> adr:addresses){
                BaseModel bm = new BaseModel(adr);
                if(addressList.contains(bm))continue;
                addressList.add(bm);
            }

            addressAdapter.notifyDataSetChanged();

       }

       private void clickNext(){
           if(position==0){
               mainActivity.vp.setCurrentItem(1);
           }
           if(position==1){
               if(mainActivity.selectedAddress==null){
                   Toast("Select a delivery address");
                   return;
               }
               mainActivity.vp.setCurrentItem(2);
           }

           if(position==2){
               if(paymentMethod==PAY_ON_DELIVERY){
                   //Toast("Placing Order");
                   placeOrder();
               }else{
                   if(mainActivity.hasPaid){
                       placeOrder();
                       return;
                   }

                   String nara = String.format("Your payment of N%s has been received",mainActivity.formatNumber(mainActivity.grandTotal));

                   mainActivity.amountPaid = paymentMethod==PAY_PARTLY?mainActivity.grandTotal/10:mainActivity.grandTotal;

                   new RavePayManager(activity).setAmount(Double.parseDouble(String.valueOf(mainActivity.amountPaid)))
                           .setCountry("NG")
                           .setCurrency("NGN")
                           .setEmail(MyApplication.userModel.getEmail())
                           .setfName(mainActivity.selectedAddress.getString(NAME))
                           .setlName("")
                           .setNarration(nara)
                           .setPublicKey("FLWPUBK-15167238e37a58d12c8b7b9bd68db46e-X")
                           .setSecretKey("FLWSECK-2609bf8ff2ff65661f7b2d04ea39fece-X")
                           .setTxRef(mainActivity.orderId)
                           .acceptMpesaPayments(false)
                           .acceptAccountPayments(true)
                           .acceptCardPayments(true)
                           .acceptGHMobileMoneyPayments(false)
                           .onStagingEnv(false)
                           .initialize();
               }
           }
       }

       private void placeOrder(){
           ArrayList<String> cartItem = new ArrayList<>();
           for(BaseModel bm:mainActivity.mainList){
               cartItem.add(bm.getObjectId());
           }

           md = mainActivity.getLoadingDialog("Placing Order",false);
           md.show();

           BaseModel model = new BaseModel();
           model.put(ITEMS_IN_CART,cartItem);
           model.put(TOTAL_ITEMS,mainActivity.itemsCount);
           model.put(PRICE,mainActivity.grandTotal);
           model.put(AMOUNT_PAID,mainActivity.amountPaid);
           model.put(ADDRESS,mainActivity.selectedAddress.items);
           model.put(PAYMENT_OPTION,paymentMethod);
           model.put(OBJECT_ID,mainActivity.orderId);
           model.put(USER_ID,MyApplication.userModel.getUserId());
           model.put(EMAIL,MyApplication.userModel.getEmail());
           model.put(STATUS,ORDER_PENDING);
           model.saveItem(ORDER_BASE, mainActivity.orderId, new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   md.dismiss();
                   if(!task.isSuccessful()){
                       Toast("Error Placing Order, Try Again");
                       if(paymentMethod!=PAY_ON_DELIVERY)mainActivity.next_tv.setText("PLACE ORDER");
                       return;
                   }
                   Toast("Successful");
                   resetMyOrder();
                   activity.finish();
               }
           });
       }

       private void resetMyOrder(){
           MyApplication.userModel.put(MY_CART_ITEMS,new ArrayList<String>());
           MyApplication.userModel.updateItem();
           activity.sendBroadcast(new Intent(BROADCAST_CART_UPDATED));
       }

       @Override
       public void onActivityResult(int requestCode, int resultCode, Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           mainActivity.hideKeyboard(mainActivity.next);

           if(requestCode==11 && resultCode==RESULT_OK){
               MyApplication.userModel.addToList(MY_ADDRESS,MyApplication.dummyAddress.items,true);
               MyApplication.userModel.updateItem();

               addressList.add(MyApplication.dummyAddress);
               rsv.hideAllView();
               addressAdapter.notifyDataSetChanged();
               MyApplication.dummyAddress=null;
           }

           if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {

               String message = data.getStringExtra("response");

               if (message != null) {
                   Log.d("rave response", message);
               }

               if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                   String name = data.getStringExtra(NAME);

                   Toast("Payment successful");
                   mainActivity.hasPaid=true;

                   BaseModel model = new BaseModel();
                   model.put(REFERENCE,mainActivity.orderId);
                   model.put(EMAIL,MyApplication.userModel.getEmail());
                   model.put(NAME,mainActivity.selectedAddress.getString(NAME));
                   model.put(AMOUNT,mainActivity.amountPaid);
                   model.saveItem(PAYMENT_REF_BASE);
                   placeOrder();

               }
               else if (resultCode == RavePayActivity.RESULT_ERROR) {
                   Toast("ERROR OCCURRED");
               }
            /*else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                //Toast.makeText(this, "PAYMENT CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }*/
           }
       }

       private void loadItems(){
           ArrayList<BaseModel> models = new ArrayList<>();
           ArrayList<String> cartIds = (ArrayList<String>) MyApplication.userModel.getList(MY_CART_ITEMS);

           for(BaseModel bm:MyApplication.cart_items){
               if(cartIds.contains(bm.getObjectId())){
                   models.add(bm);
               }
           }

           mainActivity.mainList.addAll(models);
           cartAdapter.notifyDataSetChanged();

           calcPrice();
       }

       private void calcPrice(){
           if(mainActivity.mainList.isEmpty()){
               Toast("Your cart is empty");
               activity.finish();
               return;
           }

           int total = 0;
           int count = 0;
           for(BaseModel bm:mainActivity.mainList){
               int quantity = bm.getInt(QUANTITY);
               quantity = quantity==0?1:quantity;
               int price = bm.getInt(PRICE);

               int t = price*quantity;

               count = count+quantity;
               total = total+t;
           }

           total_tv.setText(String.format("N%s",mainActivity.formatNumber(total)));
           itemstv.setText(String.format("%s Item%s",count,mainActivity.mainList.size()>1?"s":""));

           mainActivity.itemsCount=count;
           mainActivity.totalPayment=total;
       }


       private void Toast(String s){
           Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
       }
       @Override
       public void setUserVisibleHint(boolean isVisibleToUser) {
           super.setUserVisibleHint(isVisibleToUser);
           if(isVisibleToUser && mainActivity!=null){
               mainActivity.next.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       clickNext();
                   }
               });

               if(position==2){
                   total_tv.setText(String.format("N%s",mainActivity.formatNumber(mainActivity.totalPayment)));
                   items_tv.setText(String.format("%s Item%s",mainActivity.itemsCount,mainActivity.itemsCount>1?"s":""));

                   String dFee = mainActivity.getFromTextSections(DELIVERY_FEE);
                   int deliveryFee = dFee.isEmpty()?0:Integer.valueOf(dFee);

                   d_fee_tv.setText(String.format("N%s",mainActivity.formatNumber(deliveryFee)));

                   mainActivity.grandTotal = deliveryFee + mainActivity.totalPayment;
                   g_total_v.setText(String.format("N%s",mainActivity.formatNumber(mainActivity.grandTotal)));

                   part_tv.setText(String.format("%s Part Payment (N%s)","10%",mainActivity.formatNumber(mainActivity.grandTotal/10)));

                   paymentMethod = PAY_FULLY;
                   resetPayLayout(PAY_FULLY);
                   mainActivity.next_tv.setText("PAY AND PLACE ORDER");
               }
           }
       }

   }



    @Override
    public void onBackPressed() {
        if(vp.getCurrentItem()==0){
            super.onBackPressed();

            ArrayList<String> ids = new ArrayList<>();
            for(BaseModel bm:mainList){
                ids.add(bm.getObjectId());
            }
            if(MyApplication.userModel!=null){
                MyApplication.userModel.put(MY_CART_ITEMS,ids);
                MyApplication.userModel.updateItem();
                sendBroadcast(new Intent(BROADCAST_CART_UPDATED));
            }

            return;
        }
        int p = vp.getCurrentItem();

        p--;
        p = p<0?0:p;
        vp.setCurrentItem(p);
    }
}
