package com.macode.supapp.utilities;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.macode.supapp.R;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView firstUserProfileImage, secondUserProfileImage;
    public TextView firstUserMessage, secondUserMessage;

    public ChatsViewHolder(@NonNull View itemView) {
        super(itemView);

        firstUserProfileImage = itemView.findViewById(R.id.firstUserProfileImage);
        secondUserProfileImage = itemView.findViewById(R.id.secondUserProfileImage);
        firstUserMessage = itemView.findViewById(R.id.firstUserMessage);
        secondUserMessage = itemView.findViewById(R.id.secondUserMessage);
    }
}
