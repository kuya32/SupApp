package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profileImageAppBar;
    private TextView usernameAppBar, statusAppBar;
    private EditText chatMessageInput;
    private ImageView chatAddImage, sendMessageButton, statusImageChatAppBar;
    private RecyclerView chatRecyclerView;
    private String otherUserId, otherUsername, otherUserProfileImageUrl, otherUserStatus, chatMessageString, userProfileImageUrl, username;
    private DatabaseReference userReference, messageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseRecyclerOptions<Chats> chatOptions;
    private FirebaseRecyclerAdapter<Chats, ChatsViewHolder> chatAdapter;
    private String URL = "https://fcm.googlepis.com/fcm/send";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.chatAppBar);
        setSupportActionBar(toolbar);

        requestQueue = Volley.newRequestQueue(this);
        profileImageAppBar = findViewById(R.id.profileImageChatAppBar);
        usernameAppBar = findViewById(R.id.usernameChatAppBar);
        statusImageChatAppBar = findViewById(R.id.statusImageChatAppBar);
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

        loadUserProfileImage();
        loadOtherUsers();
        loadMessages();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadUserProfileImage() {
        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
                    Picasso.get().load(userProfileImageUrl).into(holder.secondUserProfileImage);
                } else {
                    holder.firstUserMessage.setVisibility(View.VISIBLE);
                    holder.firstUserProfileImage.setVisibility(View.VISIBLE);
                    holder.secondUserMessage.setVisibility(View.GONE);
                    holder.secondUserProfileImage.setVisibility(View.GONE);

                    holder.firstUserMessage.setText(model.getMessage());
                    Picasso.get().load(otherUserProfileImageUrl).into(holder.firstUserProfileImage);
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
                                    sendNotification(chatMessageString);
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

    private void sendNotification(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/" + otherUserId);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("username", "Message from " + username);
            jsonObject1.put("body", message);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("userId", firebaseUser.getUid());
            jsonObject2.put("type", "message");

            jsonObject.put("notification", jsonObject1);
            jsonObject.put("data", jsonObject2);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    String key = getResources().getString(R.string.googleFirebaseCloudMessagingKey);
                    map.put("content-type", "application/json");
                    map.put("authorization", key);
                    return map;
                }
            };
            requestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
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
                    if (otherUserStatus.equals("Online")) {
                        statusImageChatAppBar.setColorFilter(Color.GREEN);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            Intent intent = new Intent( .this, MainActivity.class);
//            startActivity(intent);
//        }
//        return super.onOptionsItemSelected(item);
//    }
}