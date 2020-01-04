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
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.hairbyprogress.custom.CustomViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.xiaopan.sketch.SketchImageView;

import static com.hairbyprogress.MyApplication.alreadyStarted;
import static com.hairbyprogress.MyApplication.getCurrentUser;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class SignupActivity extends BaseActivity {

    @BindView(R.id.email_etv) EditText email_etv;
    @BindView(R.id.pass_etv) EditText pass_etv;
    @BindView(R.id.pass_etv1) EditText pass_etv1;
    @BindView(R.id.signup_but) View signup_but;
    @BindView(R.id.login_but) View login_but;

    public String email,pass,pass1;

    MaterialDialog md;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_main);
        ButterKnife.bind(this);

        login_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,LoginActivity.class));
                finish();
            }
        });

        signup_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
    }

    private void signup() {

        email_etv.setError(null);
        pass_etv.setError(null);
        pass_etv1.setError(null);

        email = email_etv.getText().toString().trim();
        pass = pass_etv.getText().toString().trim();
        pass1 = pass_etv1.getText().toString().trim();

        if (!isEmailValid(email)) {
            email_etv.setError("Please use a valid email address");
            email_etv.requestFocus();
            return;
        }

        if (pass.length() < 6) {
            pass_etv.setError("Your password is too short");
            pass_etv.requestFocus();
            return;
        }

        if (!pass.equals(pass1)) {
            pass_etv1.setError("Password does not match");
            pass_etv1.requestFocus();
            return;
        }

        md = getLoadingDialog("Signing up",false);
        md.show();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    md.dismiss();

                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException exception){

                        pass_etv.setError("Your password is weak");
                        pass_etv.requestFocus();
                    }
                    catch (FirebaseAuthUserCollisionException exception){
                        email_etv.setError("Email address already taken");
                        email_etv.requestFocus();
                    }
                    catch (Exception e) {
                        Toast("An error occurred, Try again");
                    }

                    return;
                }
                FirebaseUser user = mAuth.getCurrentUser();
                if(user==null){
                    Toast("No such user");return;
                }
                createProfile(user.getUid());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    md.dismiss();
                }
            });
    }

    private void createProfile(final String uId){
        md.show();

        final BaseModel model = new BaseModel();
        model.put(USER_ID,uId);
        model.put(EMAIL,email);
        model.put(PASSWORD,pass);
        model.put(DEVICE_ID,getDeviceId());
        model.put(IS_ADMIN,email.equals("johnebere58@gmail.com"));
        model.saveItem(USER_BASE,uId, new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                createUserListener();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                md.dismiss();
                showErrorDialog(e.getMessage(),new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        createProfile(uId);
                    }
                });
            }
        });
    }

    private void createUserListener(){
        md.show();

        final FirebaseUser currentUser = getCurrentUser();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(USER_BASE).document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                md.dismiss();
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        MyApplication.pausedTime = System.currentTimeMillis();
                        MyApplication.userModel = new BaseModel(doc);
                        if(alreadyStarted){
                            finish();
                        }else {
                            startActivity(new Intent(context, MainActivity.class));
                        }
                    }else{
                        Toast("User Does Not Exist, Try Signing up");
                        FirebaseAuth.getInstance().signOut();
                    }
                }else{
                    showErrorDialog(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            createUserListener();
                        }
                    });
                }
            }
        });
    }

}
