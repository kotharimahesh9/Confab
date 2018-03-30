package com.mahesh.hp.confab;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsReference;
    private DatabaseReference mUsersReference;
    private String mCurrent_user_id;
    private View mMainView;
    private FirebaseAuth mAuth;
    private ImageView imageView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList =  mMainView.findViewById(R.id.friends_list);
        mAuth =FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersReference.keepSynced(true);
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsReference.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));



        // Inflate the layout for this fragment
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerView =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        mFriendsReference) {

                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int position) {
                        viewHolder.setDate(model.getDate());
                      final  String list_user_id =getRef(position).getKey();
                        mUsersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                                String userOnlineStatus = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserName(userName);
                                viewHolder.setThumbImage(getContext(),userThumbImage);

                                if(dataSnapshot.hasChild("online")){
                                    viewHolder.setUserOnline(userOnlineStatus);
                                }


                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Click event for each item
                                                if(which == 0){
                                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                    profileIntent.putExtra("uid",list_user_id);
                                                    startActivity(profileIntent);
                                                }
                                                if(which == 1){
                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    chatIntent.putExtra("uid",list_user_id);
                                                    chatIntent.putExtra("userName",userName);
                                                    startActivity(chatIntent);
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });





                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });




                    }
                };
        mFriendsList.setAdapter(friendsRecyclerView);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView userNameView = mView.findViewById(R.id.allUsersUserStatus);
            userNameView.setText(date);
        }

        public void setUserName(String userName) {
            TextView userNameDisplay = mView.findViewById(R.id.allUsersUsername);
            userNameDisplay.setText(userName);

        }

        public void setThumbImage(final Context ctx, final String thumbImage) {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.allUsersDisplayImage);

            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_image)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_image).into(thumb_image);
                        }
                    });
        }
        public void setUserOnline(String userOnlineStatus){
           ImageView onlineStatusView = mView.findViewById(R.id.allUsersOnlineIcon);
            if(userOnlineStatus.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }
            else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
