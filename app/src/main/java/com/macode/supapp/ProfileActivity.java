package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileActivityImage;
    private String profileImageUrl, username, cityAndState, phoneNumber, profession;
    private EditText profileActivityUsernameInput, profileActivityCityAndStateInput, profileActivityPhoneInput, profileActivityProfessionInput;
    private Button profileActivityEditButton;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileActivityImage = findViewById(R.id.profileActivityImage);
        profileActivityUsernameInput = findViewById(R.id.profileActivityUsername);
        profileActivityCityAndStateInput = findViewById(R.id.profileActivityCityAndState);
        profileActivityPhoneInput = findViewById(R.id.profileActivityPhone);
        profileActivityProfessionInput = findViewById(R.id.profileActivityProfession);
        profileActivityEditButton = findViewById(R.id.editProfileButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    cityAndState = snapshot.child("cityAndState").getValue().toString();
                    phoneNumber = snapshot.child("phoneNumber").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileActivityImage);
                    profileActivityUsernameInput.setText(username);
                    profileActivityCityAndStateInput.setText(cityAndState);
                    profileActivityPhoneInput.setText(phoneNumber);
                    profileActivityProfessionInput.setText(profession);
                } else {
                    Toast.makeText(ProfileActivity.this, "Data does not exist!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}