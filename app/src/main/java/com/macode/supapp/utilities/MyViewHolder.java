package com.macode.supapp.utilities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.macode.supapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profileImage;
    public ImageView postImage;
    public ImageButton imageLikeButton, imageCommentButton;
    public TextView username, timeAgo, postDesc, likeCounter, commentCounter;

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
    }
}
