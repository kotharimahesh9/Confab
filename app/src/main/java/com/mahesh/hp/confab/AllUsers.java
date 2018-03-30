package com.mahesh.hp.confab;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mToolbar = findViewById(R.id.allUsers_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Users");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        mUsersList = findViewById(R.id.allUsersRecyclerView);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.all_users_display_layout,
                UsersViewHolder.class,
                mDatabaseReference

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
               viewHolder.setName(model.getName());
                viewHolder.setThumb_image(AllUsers.this,model.getThumb_image());
               viewHolder.setStatus(model.getStatus());

              final  String uid = getRef(position).getKey();

               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       Intent profileIntent = new Intent(AllUsers.this,ProfileActivity.class);
                       profileIntent.putExtra("uid",uid);
                       startActivity(profileIntent);
                   }
               });




            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);


    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView username = mView.findViewById(R.id.allUsersUsername);
            username.setText(name);
        }
        public void setThumb_image(final Context ctx , final String thumbImage)
        {

            final CircleImageView userImageView = mView.findViewById(R.id.allUsersDisplayImage);
            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_image)
                    .into(userImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_image)
                                    .into(userImageView);
                        }
                    });
        }
        public void setStatus(String status ){
            TextView userStatus = mView.findViewById(R.id.allUsersUserStatus);
            userStatus.setText(status);
        }
    }
}
