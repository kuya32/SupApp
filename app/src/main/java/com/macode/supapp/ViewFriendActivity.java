package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendActivity extends AppCompatActivity {

    private String userId, profileImageUrl, username,
            profileImageUrl2, username2, cityAndState, profession, profession2, status, currentState = "nothingHappened";
    private Toolbar toolbar;
    private DatabaseReference userReference, requestReference, friendReference, currentUserReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private CircleImageView viewFriendProfileImageUrl;
    private TextView viewFriendUsername, viewFriendCityAndState, viewFriendProfession, viewFriendStatus;
    private Button positiveButton, negativeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);

        userId = getIntent().getStringExtra("userKey");

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        currentUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        requestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        toolbar = findViewById(R.id.viewFriendAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewFriendProfileImageUrl = findViewById(R.id.viewFriendProfileImage);
        viewFriendUsername = findViewById(R.id.viewFriendUsernameText);
        viewFriendCityAndState = findViewById(R.id.viewFriendCityAndState);
        viewFriendProfession = findViewById(R.id.viewFriendProfessionText);
        viewFriendStatus = findViewById(R.id.viewFriendStatus);
        positiveButton = findViewById(R.id.positiveButton);
        negativeButton = findViewById(R.id.negativeButton);

        loadUser();

        checkUserFriendship(userId);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendship(userId);
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendshipOver(userId);
            }
        });

    }

    private void friendshipOver(String userId) {
        if (currentState.equals("friends")) {
            friendReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        friendReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewFriendActivity.this, "Frienship Over", Toast.LENGTH_SHORT).show();
                                    currentState = "nothingHappened";
                                    positiveButton.setText("Send Friend Request");
                                    negativeButton.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }

        if ((currentState.equals("receivedFriendRequest"))) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "declined");
            requestReference.child(userId).child(firebaseUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friendship denied!", Toast.LENGTH_SHORT).show();
                        currentState = "receivedFriendRequestDenied";
                        positiveButton.setVisibility(View.GONE);
                        negativeButton.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void checkUserFriendship(String userId) {
        friendReference.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friends";
                    positiveButton.setText("Send Message");
                    negativeButton.setText("Unfriend");
                    negativeButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendReference.child(userId).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friends";
                    positiveButton.setText("Send Message");
                    negativeButton.setText("Unfriend");
                    negativeButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestReference.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "friendRequestPending";
                        positiveButton.setText("Cancel friend request");
                        negativeButton.setVisibility(View.GONE);
                    }
                    if (snapshot.child("status").getValue().toString().equals("declined")) {
                        currentState = "friendRequestDeclined";
                        positiveButton.setText("Cancel friend request");
                        negativeButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestReference.child(userId).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "receivedFriendRequest";
                        positiveButton.setText("Accept friend request");
                        negativeButton.setText("Decline friend request");
                        negativeButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (currentState.equals("nothingHappened")) {
            currentState = "nothingHappened";
            positiveButton.setText("Send friend request");
            negativeButton.setVisibility(View.GONE);
        }
    }

    private void friendship(String userId) {
        if (currentState.equals("nothingHappened")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "pending");
            requestReference.child(firebaseUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                        negativeButton.setVisibility(View.GONE);
                        currentState = "friendRequestPending";
                        positiveButton.setText("Cancel Friend Request");
                    } else {
                        Toast.makeText(ViewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (currentState.equals("friendRequestPending") || currentState.equals("friendRequestDenied")) {
            requestReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friend request cancelled", Toast.LENGTH_SHORT).show();
                        currentState = "nothingHappened";
                        positiveButton.setText("Send Friend Request");
                        negativeButton.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (currentState.equals("receivedFriendRequest")) {
            requestReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        final HashMap hashMap1 = new HashMap();
                        hashMap1.put("status", "friends");
                        hashMap1.put("username", username);
                        hashMap1.put("profileImageUrl", profileImageUrl);
                        hashMap1.put("profession", profession);
                        friendReference.child(firebaseUser.getUid()).child(userId).updateChildren(hashMap1).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    currentUserReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                username2 = snapshot.child("username").getValue().toString();
                                                profileImageUrl2 = snapshot.child("profileImage").getValue().toString();
                                                profession2 = snapshot.child("profession").getValue().toString();
                                                final HashMap hashMap2 = new HashMap();
                                                hashMap2.put("status", "friends");
                                                hashMap2.put("username", username2);
                                                hashMap2.put("profileImageUrl", profileImageUrl2);
                                                hashMap2.put("profession", profession2);
                                                friendReference.child(userId).child(firebaseUser.getUid()).updateChildren(hashMap2).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        Toast.makeText(ViewFriendActivity.this, "Friend added", Toast.LENGTH_SHORT).show();
                                                        currentState = "friends";
                                                        positiveButton.setText("Send Message");
                                                        negativeButton.setText("Unfriend");
                                                        negativeButton.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        if (currentState.equals("friendRequestDeclined")) {
            requestReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(ViewFriendActivity.this, "Friend added", Toast.LENGTH_SHORT).show();
                    currentState = "nothingHappened";
                    positiveButton.setText("Send friend request");
                    negativeButton.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loadUser() {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    getSupportActionBar().setTitle(username);
                    cityAndState = snapshot.child("cityAndState").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();
                    status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(viewFriendProfileImageUrl);
                    viewFriendUsername.setText(username);
                    viewFriendCityAndState.setText(cityAndState);
                    viewFriendProfession.setText(profession);
                    viewFriendStatus.setText(status);
                } else {
                    Toast.makeText(ViewFriendActivity.this, "Data was not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}