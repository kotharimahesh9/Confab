package com.mahesh.hp.confab;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileCount, mProfileName, mProfileStatus;
    private Button mProfileSendReqBtn, mProfileDeclineFriendRequestBtn;
    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private String CURRENT_STATE;
    private DatabaseReference mFriendRequestReference;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendsReference;
    private DatabaseReference mNotificationReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String userid = getIntent().getExtras().get("uid").toString();
        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProfileCount = findViewById(R.id.profileTotalFriends);
        mProfileImage = findViewById(R.id.profileImage);
        mProfileName = findViewById(R.id.profileUsername);
        mProfileStatus = findViewById(R.id.profileStatus);
        mProfileSendReqBtn = findViewById(R.id.profileSendFriendRequest);
        mProfileDeclineFriendRequestBtn = findViewById(R.id.profileDeclineFriendRequest);
        mFriendRequestReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        mFriendRequestReference.keepSynced(true);
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notifications");
        CURRENT_STATE="not_friends";
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendsReference=FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsReference.keepSynced(true);



        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the profile data ");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
        mUsersDatabase.keepSynced(true);

        mProfileDeclineFriendRequestBtn.setEnabled(false);
        mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();


                mProfileName.setText(name);
                mProfileStatus.setText(status);
                Picasso.with(getApplicationContext()).load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_image)
                        .into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(image)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(mProfileImage);
                            }
                        });
                mProgressDialog.dismiss();

//--------------------------------FRIEND LIST  / REQUEST FEATURE -------------------------------------------//

                mFriendRequestReference.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userid)){
                         String reqType = dataSnapshot.child(userid).child("request_type").getValue().toString();
                         if(reqType.equals("received")){
                             CURRENT_STATE="request_received";
                             mProfileSendReqBtn.setText("Accept Friend Request");
                             mProfileDeclineFriendRequestBtn.setVisibility(View.VISIBLE);
                             mProfileDeclineFriendRequestBtn.setEnabled(true);
                         }
                         else if(reqType.equals("sent")){
                             CURRENT_STATE = "request_sent";
                             mProfileSendReqBtn.setText("Cancel Friend Request");
                             mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                             mProfileDeclineFriendRequestBtn.setEnabled(false);

                         }
                         else {
                            mFriendsReference.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userid)){
                                        CURRENT_STATE="friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");
                                        mProgressDialog.dismiss();
                                       mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineFriendRequestBtn.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                         }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);
   //----------------------------------NOT FRIENDS------------------------------------//
                if(CURRENT_STATE.equals("not_friends")){
                    mFriendRequestReference.child(mCurrentUser.getUid()).child(userid).child("request_type")
                            .setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestReference.child(userid).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            HashMap<String,String> notificationData = new HashMap<>();
                                            notificationData.put("from",mCurrentUser.getUid());
                                            notificationData.put("type","request");
                                            mNotificationReference.child(userid).push().setValue(notificationData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                CURRENT_STATE="request_sent";
                                                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                                                mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                mProfileDeclineFriendRequestBtn.setEnabled(false);
                                                            }
                                                        }
                                                    });

                                            Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();

                                        }else{

                                        }
                                    }
                                });

                            }else{
                                Toast.makeText(ProfileActivity.this, "Sorry Unable to send Request", Toast.LENGTH_LONG).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }

//-------------------------------------CANCEL REQUEST STATE-------------------------------------------------//

                if(CURRENT_STATE.equals("request_sent")){
                    mFriendRequestReference.child(mCurrentUser.getUid()).child(userid).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestReference.child(userid).child(mCurrentUser.getUid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mProfileSendReqBtn.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                mProfileSendReqBtn.setText("Send Friend Request");
                                                mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineFriendRequestBtn.setEnabled(false);

                                            }
                                            }
                                        });
                            }
                        }
                    });
                }

//------------------------------------REQUEST RECEIVED STATE------------------------------------------//
                if(CURRENT_STATE.equals("request_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendsReference.child(mCurrentUser.getUid()).child(userid).child("date")
                            .setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsReference.child(userid).child(mCurrentUser.getUid())
                                            .child("date").setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            CURRENT_STATE="friends";
                                            mFriendRequestReference.child(mCurrentUser.getUid()).child(userid).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                mFriendRequestReference.child(userid).child(mCurrentUser.getUid()).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                                    CURRENT_STATE="friends";
                                                                                    mProfileSendReqBtn.setText("Unfriend this person");
                                                                                   mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                                    mProfileDeclineFriendRequestBtn.setEnabled(false);

                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });


                }

//--------------------------UNFRIENDING A PERSON-------------------------------//
                if(CURRENT_STATE.equals("friends")){
                    mFriendsReference.child(mCurrentUser.getUid()).child(userid).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendsReference.child(userid).child(mCurrentUser.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            mProfileSendReqBtn.setEnabled(true);
                                                            CURRENT_STATE="not_friends";
                                                            mProfileSendReqBtn.setText("Send Friend Request");
                                                           mProfileDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                            mProfileDeclineFriendRequestBtn.setEnabled(false);

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });




                }

            }
        });
    }
}
/*ISSUSES
1. Decline friend request is appearing after accepting friend request upon hitting the back button
2. In profile Activity extract the thumb image not the actual image
*/