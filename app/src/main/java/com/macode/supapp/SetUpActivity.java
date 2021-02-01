package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private CircleImageView profileImage;
    private EditText usernameInput, cityAndStateInput, phoneNumberInput, professionInput;
    private String username, cityAndState, phoneNumber, profession, image;
    private Button saveButton;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        toolbar = findViewById(R.id.setUpAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");

        profileImage = findViewById(R.id.setUpProfileImage);
        usernameInput = findViewById(R.id.setUpUsernameInput);
        cityAndStateInput = findViewById(R.id.setUpCityAndStateInput);
        phoneNumberInput = findViewById(R.id.setUpPhoneInput);
        professionInput = findViewById(R.id.setUpProfessionInput);
        saveButton = findViewById(R.id.setUpSaveButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void saveData() {
        username = usernameInput.getText().toString();
        cityAndState = cityAndStateInput.getText().toString();
        phoneNumber = phoneNumberInput.getText().toString();
        profession = professionInput.getText().toString();
        image = imageUri.toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(usernameInput, "Username must be longer than 3 characters!");
        } else if (cityAndState.isEmpty() || cityAndState.length() < 5) {
            showError(cityAndStateInput, "City and State is not valid!");
        } else if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            showError(phoneNumberInput, "Phone number not valid!");
        } else if (profession.isEmpty() || profession.length() < 3) {
            showError(professionInput, "Profession is not valid!");
        } else if (imageUri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
        } else {
            storageReference.child(firebaseUser.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.child(firebaseUser.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("username", username);
                                hashMap.put("cityAndState", cityAndState);
                                hashMap.put("phoneNumber", phoneNumber);
                                hashMap.put("profession", profession);
                                hashMap.put("profileImage", image);
                                hashMap.put("status", "Offline");

                                databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Intent intent = new Intent(SetUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(SetUpActivity.this, "Setup profile completed!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    private void showError(EditText input, String string) {
        input.setError(string);
        input.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}