
package com.mahesh.hp.confab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private Button login_Button;
    private EditText mEmail , mPassword;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        login_Button = findViewById(R.id.login_btn);
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);
        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(!(TextUtils.isEmpty(mail) || TextUtils.isEmpty(password))){
                    mProgressDialog.setTitle("Logging In");
                    mProgressDialog.setMessage("Please wait while we verify your credentials");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    login_user(mail,password);
                }
            }
        });


    }

    private void login_user(String mail, String password) {
       mAuth.signInWithEmailAndPassword(mail,password)
               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){


                        mProgressDialog.dismiss();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUserId =mAuth.getCurrentUser().getUid();
                        mUserDatabaseReference.child(currentUserId).child("device_token")
                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });



                    }
                    else{
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Cannot Login ", Toast.LENGTH_LONG).show();
                    }
                   }
               });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
