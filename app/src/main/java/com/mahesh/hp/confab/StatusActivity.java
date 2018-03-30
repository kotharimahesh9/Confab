package com.mahesh.hp.confab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mTextInputLayout;
    private Button saveChanges;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mToolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTextInputLayout = findViewById(R.id.textInputLayout);
        saveChanges = findViewById(R.id.status_save_changes);
        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(StatusActivity.this);
        String current_user = mAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);
        String oldStatus = getIntent().getExtras().getString("oldValue");
        mTextInputLayout.getEditText().setText(oldStatus);
        mTextInputLayout.getEditText().setSelectAllOnFocus(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);




        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Please wait while we change your status");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                String status = mTextInputLayout.getEditText().getText().toString();
                mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(StatusActivity.this , "Status Saved Successfully",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                            Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                        }
                    }
                });


            }
        });


    }


}
