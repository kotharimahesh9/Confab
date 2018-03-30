package com.mahesh.hp.confab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;
    //Android Layout
    private Button changeStatus , changeImage;
    private CircleImageView mDisplayImage;
    private TextView mName , mStatus;
    private static final int GALLERY_PICK=1;
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mProgressDialog = new ProgressDialog(SettingsActivity.this);
        mDisplayImage = findViewById(R.id.settings_Image);
        mName = findViewById(R.id.settings_display_name);
        mStatus=findViewById(R.id.settings_display_status);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        changeImage = findViewById(R.id.settings_change_image);
        changeStatus = findViewById(R.id.settings_change_status);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        final String current_id = mCurrentUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_id);
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").toString();
                mName.setText(name);
                mStatus.setText(status);
                Picasso.with(SettingsActivity.this).load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                        .into(mDisplayImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image)
                                        .into(mDisplayImage);
                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    changeStatus.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String status_value=mStatus.getText().toString();
            Intent statusIntent = new Intent(SettingsActivity.this , StatusActivity.class);
            statusIntent.putExtra("oldValue",status_value);
            startActivity(statusIntent);
            finish();
        }
    });

    changeImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            /*CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(SettingsActivity.this);*/
        }
    });
    }
  /*  @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }*/

        if(requestCode== GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);




        }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try{
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we change your image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();


                File thumb_file_path = new File(resultUri.getPath());
    final String uid = mCurrentUser.getUid();


                    Bitmap thumbBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_file_path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                     final byte[] thumbByte = baos.toByteArray();




                StorageReference filePath = mImageStorage.child("profile_images").child(uid + ".jpg");
                final StorageReference thumb_image = mImageStorage.child("profile_images").child("thumbs").child(uid+ ".jpg");




                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();


                            UploadTask uploadTask = thumb_image.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_download_url =thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){
                                        Map updateHashmap= new HashMap();

                                        updateHashmap.put("image",downloadUrl);
                                        updateHashmap.put("thumb_image",thumb_download_url);


                                        mDatabaseReference.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Successfull", Toast.LENGTH_SHORT).show();


                                                }
                                                else{

                                                }
                                            }
                                        });




                                    }
                                    else{

                                    }
                                }
                            });
                           /* mDatabaseReference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Successfull", Toast.LENGTH_SHORT).show();


                                    }
                                    else{

                                    }
                                }
                            });*/



                        }
                    }
                });




            }catch (Exception e){

                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                mProgressDialog.dismiss();
                Exception error = result.getError();
            }
            }

        }

    }

