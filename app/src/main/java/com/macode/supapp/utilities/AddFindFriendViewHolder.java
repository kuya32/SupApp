package com.macode.supapp.utilities;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.macode.supapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFindFriendViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView findFriendProfileImage;
    public TextView findFriendUsername, findFriendProfession;

    public AddFindFriendViewHolder(@NonNull View itemView) {
        super(itemView);

        findFriendProfileImage = itemView.findViewById(R.id.findFriendProfileImage);
        findFriendUsername = itemView.findViewById(R.id.findFriendUsername);
        findFriendProfession = itemView.findViewById(R.id.findFriendProfession);
    }
}
