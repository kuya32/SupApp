package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private CardView setUpCardView;
    private CircleImageView profileImage;
    private EditText usernameInput, cityAndStateInput, phoneNumberInput, professionInput;
    private String username, cityAndState, phoneNumber, profession;
    private Button saveButton;
    private Uri uri;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Toolbar toolbar;
    private CardView savingDataProgressCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        toolbar = findViewById(R.id.setUpAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");

        setUpCardView = findViewById(R.id.setUpCardView);
        profileImage = findViewById(R.id.setUpProfileImage);
        usernameInput = findViewById(R.id.setUpUsernameInput);
        cityAndStateInput = findViewById(R.id.setUpCityAndStateInput);
        phoneNumberInput = findViewById(R.id.setUpPhoneInput);
        professionInput = findViewById(R.id.setUpProfessionInput);
        saveButton = findViewById(R.id.setUpSaveButton);
        savingDataProgressCardView = findViewById(R.id.savingDataProgressCardView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select image"), REQUEST_CODE);
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
        savingDataProgressCardView.setVisibility(View.VISIBLE);
        username = usernameInput.getText().toString();
        cityAndState = cityAndStateInput.getText().toString();
        phoneNumber = phoneNumberInput.getText().toString();
        profession = professionInput.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(usernameInput, "Username must be longer than 3 characters!");
        } else if (cityAndState.isEmpty() || cityAndState.length() < 5) {
            showError(cityAndStateInput, "City and State is not valid!");
        } else if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            showError(phoneNumberInput, "Phone number not valid!");
        } else if (profession.isEmpty() || profession.length() < 3) {
            showError(professionInput, "Profession is not valid!");
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
                                hashMap.put("status", "Online");

                                databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        savingDataProgressCardView.setVisibility(View.INVISIBLE);
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
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}