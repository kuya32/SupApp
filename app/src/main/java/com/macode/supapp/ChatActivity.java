package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.macode.supapp.utilities.Chats;
import com.macode.supapp.utilities.ChatsViewHolder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImageAppBar;
    private TextView usernameAppBar, statusAppBar;
    private EditText chatMessageInput;
    private ImageView chatAddImage, sendMessageButton;
    private RecyclerView chatRecyclerView;
    private String otherUserId, otherUsername, otherUserProfileImageUrl, otherUserStatus, chatMessageString;
    private DatabaseReference userReference, messageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseRecyclerOptions<Chats> chatOptions;
    private FirebaseRecyclerAdapter<Chats, ChatsViewHolder> chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.chatAppBar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImageAppBar = findViewById(R.id.profileImageChatAppBar);
        usernameAppBar = findViewById(R.id.usernameChatAppBar);
        statusAppBar = findViewById(R.id.statusChatAppBar);
        chatAddImage = findViewById(R.id.chatAddImage);
        sendMessageButton = findViewById(R.id.chatSendMessageButton);
        chatMessageInput = findViewById(R.id.chatMessageInput);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        messageReference = FirebaseDatabase.getInstance().getReference().child("Messages");

        otherUserId = getIntent().getStringExtra("otherUserId");

        loadOtherUsers();
        loadMessages();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadMessages() {
        chatOptions = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(messageReference.child(firebaseUser.getUid()).child(otherUserId), Chats.class).build();
        chatAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(chatOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chats model) {
                if (model.getUserId().equals(firebaseUser.getUid())) {
                    holder.firstUserMessage.setVisibility(View.GONE);
                    holder.firstUserProfileImage.setVisibility(View.GONE);
                    holder.secondUserMessage.setVisibility(View.VISIBLE);
                    holder.secondUserProfileImage.setVisibility(View.VISIBLE);

                    holder.secondUserMessage.setText(model.getMessage());
                } else {
                    holder.firstUserMessage.setVisibility(View.VISIBLE);
                    holder.firstUserProfileImage.setVisibility(View.VISIBLE);
                    holder.secondUserMessage.setVisibility(View.GONE);
                    holder.secondUserProfileImage.setVisibility(View.GONE);

                    holder.firstUserMessage.setText(model.getMessage());
                    Picasso.get().load(otherUserProfileImageUrl).into(holder.secondUserProfileImage);
                }
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_message, parent, false);

                return new ChatsViewHolder(view);
            }
        };
        chatAdapter.startListening();
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void sendMessage() {
        chatMessageString = chatMessageInput.getText().toString();
        if (chatMessageString.isEmpty()) {
            Toast.makeText(ChatActivity.this, "Please write something!", Toast.LENGTH_SHORT).show();
        } else {
            final HashMap hashMap = new HashMap();
            hashMap.put("message", chatMessageString);
            hashMap.put("status", "unseen");
            hashMap.put("userId", firebaseUser.getUid());
            messageReference.child(otherUserId).child(firebaseUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        messageReference.child(firebaseUser.getUid()).child(otherUserId).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    chatMessageInput.setText(null);
                                    Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadOtherUsers() {
        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    otherUsername = snapshot.child("username").getValue().toString();
                    otherUserProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    otherUserStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(otherUserProfileImageUrl).into(profileImageAppBar);
                    usernameAppBar.setText(otherUsername);
                    statusAppBar.setText(otherUserStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}