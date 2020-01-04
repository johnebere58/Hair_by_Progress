package com.hairbyprogress;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Forgot extends AppCompatActivity {

    @BindView(R.id.ok)View ok;
    @BindView(R.id.email_etv)EditText email_etv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        ButterKnife.bind(this);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email_etv.getText().toString().trim();
                if(email.isEmpty()){
                    email_etv.setError("Enter your email address");
                    email_etv.requestFocus();
                    return;
                }

                final MaterialDialog md = getLoadingDialog("Checking Email",false);
                md.show();

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                md.dismiss();
                                if(!task.isSuccessful()){
                                    try{
                                        throw task.getException();
                                    }catch (FirebaseAuthEmailException exception){
                                        Toast("Email does not exist");
                                    }
                                    catch (Exception e) {
                                        Toast("An error occurred, Try again");
                                    }
                                    return;
                                }
                                new MaterialDialog.Builder(Forgot.this)
                                        .content("A password reset link has been emailed to you. Follow the link to reset your password")
                                        .positiveText("OK")
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        });

            }
        });
    }

    public void Toast(Object text){
        Toast.makeText(this, String.valueOf(text), Toast.LENGTH_SHORT).show();
    }

    public MaterialDialog getLoadingDialog(String title, boolean cancelable){
        return new MaterialDialog.Builder(this)
                .title(title)
                .content("Wait a Moment...")
                .progress(true, 0)
                .canceledOnTouchOutside(cancelable)
                .progressIndeterminateStyle(false)
                .build();
    }
}
