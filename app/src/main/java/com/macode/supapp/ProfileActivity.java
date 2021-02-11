package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private Toolbar toolbar;
    private CircleImageView profileActivityImage;
    private String profileImageUrl, username, cityAndState, phoneNumber, profession;
    private EditText profileActivityUsernameInput, profileActivityCityAndStateInput, profileActivityPhoneInput, profileActivityProfessionInput;
    private Button profileActivityEditButton;
    private DatabaseReference userReference;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Uri uri;
    private CardView updatingDataProgressCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.profileAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileActivityImage = findViewById(R.id.profileActivityImage);
        profileActivityUsernameInput = findViewById(R.id.profileActivityUsername);
        profileActivityCityAndStateInput = findViewById(R.id.profileActivityCityAndState);
        profileActivityPhoneInput = findViewById(R.id.profileActivityPhone);
        profileActivityProfessionInput = findViewById(R.id.profileActivityProfession);
        profileActivityEditButton = findViewById(R.id.editProfileButton);
        updatingDataProgressCardView = findViewById(R.id.updatingDataProgressCardView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

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

        profileActivityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select image"), REQUEST_CODE);
            }
        });

        profileActivityEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });
    }

    private void editProfile() {
        updatingDataProgressCardView.setVisibility(View.VISIBLE);
        username = profileActivityUsernameInput.getText().toString();
        cityAndState = profileActivityCityAndStateInput.getText().toString();
        phoneNumber = profileActivityPhoneInput.getText().toString();
        profession = profileActivityProfessionInput.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(profileActivityUsernameInput, "Username must be longer than 3 characters!");
        } else if (cityAndState.isEmpty() || cityAndState.length() < 5) {
            showError(profileActivityCityAndStateInput, "City and State is not valid!");
        } else if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            showError(profileActivityPhoneInput, "Phone number not valid!");
        } else if (profession.isEmpty() || profession.length() < 3) {
            showError(profileActivityProfessionInput, "Profession is not valid!");
        } else if (uri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
        } else {
            storageReference.child(firebaseUser.getUid()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.child(firebaseUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("username", username);
                                hashMap.put("cityAndState", cityAndState);
                                hashMap.put("phoneNumber", phoneNumber);
                                hashMap.put("profession", profession);
                                hashMap.put("profileImage", uri.toString());

                                databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        updatingDataProgressCardView.setVisibility(View.INVISIBLE);
                                        finish();
                                        startActivity(getIntent());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("First");
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                profileActivityImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showError(EditText input, String string) {
        input.setError(string);
        input.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}