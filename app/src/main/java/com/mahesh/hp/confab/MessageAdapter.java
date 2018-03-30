package com.mahesh.hp.confab;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HP on 3/26/2018.
 */

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent , false);

        return new MessageViewHolder(v);
    }



    public class MessageViewHolder extends  RecyclerView.ViewHolder
    {
        public TextView messageText;
        public TextView NameText;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            profileImage = itemView.findViewById(R.id.message_profile_layout);
            NameText = itemView.findViewById(R.id.message_displayName_layout);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }




    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
       mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Log.d("LAPIT CHAT ERROR", current_user_id);

        final Messages c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                holder.NameText.setText(name);
                Picasso.with(holder.profileImage.getContext()).load(image).placeholder(R.drawable.default_profile_image)
                        .into(holder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        /*if(from_user.equals(current_user_id)){


            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);

        }
        else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }*/


        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else{
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(holder.profileImage.getContext()).load(c.getMessage())
                    .into(holder.messageImage);

        }

        //holder.NameText.setText();


    }

    @Override
    public int getItemCount() {

        return mMessageList.size();
    }
}
