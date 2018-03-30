package com.mahesh.hp.confab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private EditText mDisplayName;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private final String TAG = "";
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabase ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Android Fields
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mDisplayName = findViewById(R.id.reg_name);
        mCreateBtn = findViewById(R.id.reg_create_btn);
        mToolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(RegisterActivity.this);
        //Android Fields


        //Firebase Fields

        mAuth = FirebaseAuth.getInstance();

        //Firebase Fields
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(TextUtils.isEmpty(display_name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this," Please Enter All The Details", Toast.LENGTH_LONG).show();

                }
                else{
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait while we register you !");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                register_user(display_name , email , password);
                }
            }
        });

    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid= current_user.getUid();
                            String device_token= FirebaseInstanceId.getInstance().getToken();
                            mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            Map<String , String> userMap = new HashMap<>();
                            userMap.put("device_token",device_token);
                            userMap.put("name",display_name);
                            userMap.put("status","Hey there, I am using Confab");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                    }
                                }
                            });


                            mProgressDialog.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
                            startActivity(mainIntent);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            finish();

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                        } else {
                            mProgressDialog.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }
}
