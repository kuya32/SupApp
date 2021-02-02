package com.macode.supapp.utilities;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.macode.supapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profileImage;
    public ImageView postImage, postCommentButton;
    public ImageButton imageLikeButton, imageCommentButton;
    public TextView username, timeAgo, postDesc, likeCounter, commentCounter;
    public EditText commentInput;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImage = itemView.findViewById(R.id.profileImagePost);
        postImage = itemView.findViewById(R.id.postImage);
        username = itemView.findViewById(R.id.profileUsernamePost);
        timeAgo = itemView.findViewById(R.id.timeAgo);
        postDesc = itemView.findViewById(R.id.postDescription);
        imageLikeButton = itemView.findViewById(R.id.likeButton);
        imageCommentButton = itemView.findViewById(R.id.commentButton);
        likeCounter = itemView.findViewById(R.id.likeCount);
        commentCounter = itemView.findViewById(R.id.commentCount);
        postCommentButton = itemView.findViewById(R.id.sendCommentButton);
        commentInput = itemView.findViewById(R.id.postCommentInput);
    }

    public void countLikes(String postKey, String uId, final DatabaseReference likeReference) {
        likeReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalLikes = (int) snapshot.getChildrenCount();
                    likeCounter.setText(totalLikes + "");
                } else {
                    likeCounter.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        likeReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(uId).exists()) {
                    imageLikeButton.setColorFilter(Color.rgb(0, 201, 255));
                } else {
                    imageLikeButton.setColorFilter(Color.GRAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
