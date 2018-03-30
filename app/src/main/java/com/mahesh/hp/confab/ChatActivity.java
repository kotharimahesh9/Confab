package com.mahesh.hp.confab;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private String  mChatUsername;
    private Toolbar mChatToolbar;
    private TextView mTitleView , mLastSeenView;
    private CircleImageView mProfileImage;
    private DatabaseReference mRootReference;
    private String UserId;
    private FirebaseAuth mAuth;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private  List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mAdapter;
    private static final int TOTAL_MESSAGES_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private StorageReference mImageStorage;
    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey="";
    private static final int GALLERY_PICK=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mChatUser = getIntent().getStringExtra("uid");
        mChatUsername = getIntent().getStringExtra("userName");
        mChatToolbar = findViewById(R.id.chatAppBar);


        mAuth = FirebaseAuth.getInstance();


        mChatAddBtn = findViewById(R.id.chatAdd);
        mChatMessageView = findViewById(R.id.chatSendMessageTextView);
        mChatSendBtn = findViewById(R.id.chatSendMessageBtn);
        mImageStorage = FirebaseStorage.getInstance().getReference();


        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_List);
        mRefreshLayout = findViewById(R.id.swipe_layout);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);


        mMessagesList.setAdapter(mAdapter);






        String userName = getIntent().getStringExtra("userName");
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
       // getSupportActionBar().setTitle(userName);
        UserId = mAuth.getCurrentUser().getUid();
        mRootReference.child("Chat").child(UserId).child(mChatUser).child("seen").setValue(true);
        loadMessages();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //---------------Custom Action bar Items ---------------------------//
        mTitleView = findViewById(R.id.customUserName);
        mLastSeenView = findViewById(R.id.customLastSeen);
        mProfileImage = findViewById(R.id.custom_bar_image);


        mTitleView.setText(userName);
        mRootReference.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
           String online = dataSnapshot.child("online").getValue().toString();
           String image = dataSnapshot.child("image").getValue().toString();
                Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_image)
                        .into(mProfileImage);
           if(online.equals("true")){
               mLastSeenView.setText("Online");
           }else{
               GetTimeAgo getTimeAgo = new GetTimeAgo();
               long lastTime = Long.parseLong(online);
               String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
               mLastSeenView.setText("Last Seen  " +lastSeenTime);
           }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootReference.child("Chat").child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+ UserId +"/"+ mChatUser, chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+UserId, chatAddMap);

                    mRootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!=null){
                                Log.d("CHAT_APP",databaseError.getMessage().toString());
                            }

                        }
                    });



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //------------------------------------------send Button Action--------------------------------------------//

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;

                loadMoreMessages();

            }
        });


        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });
    }


    //-----------------------SEND IMAGE-------------------------------------//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri = data.getData();
            final  String current_user_ref = "messages/"+UserId+"/"+mChatUser;
            final String chat_user_ref = "messages/"+mChatUser+"/"+UserId;

            DatabaseReference user_message_push = mRootReference.child("messages").child(UserId)
                    .child(mChatUser).push();
            final String push_id = user_message_push.getKey();


            StorageReference filePath = mImageStorage.child("message_images").child(push_id+"jpg");


            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", UserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");


                        mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }
                            }
                        });



                    }
                }
            });


        }
    }


    //---------------------------------------------------------------------------------------------------------------//



    void loadMoreMessages(){
        DatabaseReference messageRef = mRootReference.child("messages").child(UserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey  = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                }
                else{
                    mPrevKey=mLastKey;
                }


                if (itemPos == 1) {

                    mLastKey = messageKey;
                }




                Log.d("Total Keys ", "Last Key: " + mLastKey + " | Prev Key: " + mPrevKey + "| Message Key : " + messageKey);


                mAdapter.notifyDataSetChanged();
                //mMessagesList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void loadMessages() {

        DatabaseReference messageRef = mRootReference.child("messages").child(UserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_MESSAGES_TO_LOAD);
       messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos==1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey= messageKey;
                    mPrevKey=messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);


                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }







    private void sendMessage() {
        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref  = "messages/" + mAuth.getCurrentUser().getUid() + "/"+ mChatUser;
            String chat_user_ref = "messages/" + mChatUser +"/"+mAuth.getCurrentUser().getUid();
            DatabaseReference userMessagePush = mRootReference.child("messages").child(UserId)
                    .child(mChatUser).push();
            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",UserId);


             Map messageUserMap = new HashMap();
             messageUserMap.put(current_user_ref + "/" + pushId , messageMap);
             messageUserMap.put(chat_user_ref + "/" + pushId , messageMap);

             mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                 @Override
                 public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                     if(databaseError!=null){
                         Log.d("CHAT_APP",databaseError.getMessage().toString());
                     }
                 }
             });

             mChatMessageView.setText(null);

        }
    }




}
