package com.macode.supapp.utilities;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.macode.supapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profileImageComment;
    public TextView commentUsername, comment, commentTimeAgo;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImageComment = itemView.findViewById(R.id.profileImageComment);
        commentUsername = itemView.findViewById(R.id.commentUsernameText);
        commentTimeAgo = itemView.findViewById(R.id.commentDateText);
        comment = itemView.findViewById(R.id.comment);
    }
}
