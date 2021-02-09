package com.macode.supapp.utilities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.macode.supapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profileImageUrl;
    public TextView username, profession;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImageUrl = itemView.findViewById(R.id.singleFriendProfileImage);
        username = itemView.findViewById(R.id.singleFriendUsername);
        profession = itemView.findViewById(R.id.singleFriendProfession);
    }
}
