package com.hairbyprogress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hairbyprogress.base.BaseActivity;
import com.hairbyprogress.base.BaseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;


import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hairbyprogress.MyApplication.alreadyStarted;
import static com.hairbyprogress.MyApplication.getCurrentUser;


/**
 * Created by John Ebere on 1/2/2017.
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.email_etv)EditText email_etv;
    @BindView(R.id.pass_etv)EditText pass_etv;
    @BindView(R.id.next)View next;
    @BindView(R.id.vis_tv)TextView vis_tv;
    @BindView(R.id.forgot)View forgot;
    @BindView(R.id.signup_but)View signup_but;
    public String email,pass;

    MaterialDialog md;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_main);
        ButterKnife.bind(this);

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,Forgot.class));
            }
        });

        signup_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,SignupActivity.class));
                finish();
            }
        });



        md = getLoadingDialog("Logging in",false);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        vis_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hidden = vis_tv.getText().toString().equals("SHOW");
                if(hidden){
                    vis_tv.setText("HIDE");
                    pass_etv.setTransformationMethod(null);
                    pass_etv.setSelection(pass_etv.getText().toString().length());
                }else{
                    vis_tv.setText("SHOW");
                    pass_etv.setTransformationMethod(new PasswordTransformationMethod());
                    pass_etv.setSelection(pass_etv.getText().toString().length());
                }
            }
        });
    }

    private void login(){
        email = email_etv.getText().toString().trim().toLowerCase();
        pass = pass_etv.getText().toString();

        email_etv.setError(null);
        pass_etv.setError(null);

        if(!isEmailValid(email)){
            email_etv.setError("Your email is invalid");
            email_etv.requestFocus();
            return;
        }
        if(pass.length()<5){
            pass_etv.setError("Your password is too short");
            pass_etv.requestFocus();
            return;
        }

        md.show();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    md.dismiss();

                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidCredentialsException exception){
                        Toast("Invalid email or password");
                    }
                    catch (Exception e) {
                        Toast("An error occurred, Try again");
                    }

                    return;
                }
                createUserListener();
            }
        });
    }

    private void createUserListener(){
        md.show();

        final FirebaseUser currentUser = getCurrentUser();
        DocumentReference docRef = db.collection(USER_BASE).document(currentUser.getUid());
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
