package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendActivity extends AppCompatActivity {

    private String userId, profileImageUrl, username, cityAndState, profession, status;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private CircleImageView viewFriendProfileImageUrl;
    private TextView viewFriendUsername, viewFriendCityAndState, viewFriendProfession, viewFriendStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);

        userId = getIntent().getStringExtra("userKey");

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        viewFriendProfileImageUrl = findViewById(R.id.viewFriendProfileImage);
        viewFriendUsername = findViewById(R.id.viewFriendUsernameText);
        viewFriendCityAndState = findViewById(R.id.viewFriendCityAndState);
        viewFriendProfession = findViewById(R.id.viewFriendProfessionText);
        viewFriendStatus = findViewById(R.id.viewFriendStatus);

        loadUser();
    }

    private void loadUser() {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
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