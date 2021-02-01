package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 101;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View view;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseUserReference, firebasePostReference;
    private StorageReference postImageReference;
    private String profileImageUrlData, usernameData, postDesc;
    private CircleImageView profileImageHeader;
    private TextView usernameHeader;
    private ImageView addImagePost, sendImagePost;
    private EditText postDescInput;
    private Uri imageUri;

    private ProgressBar addingPostProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_activity_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sup App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_hamburger);
        addImagePost = findViewById(R.id.addPostImage);
        sendImagePost = findViewById(R.id.sendPostButton);
        postDescInput = findViewById(R.id.addPostInput);
        addingPostProgressBar = findViewById(R.id.addingPostProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        firebasePostReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        postImageReference = FirebaseStorage.getInstance().getReference().child("PostImages");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        view = navigationView.inflateHeaderView(R.layout.drawer_header);
        profileImageHeader = view.findViewById(R.id.profileImageDrawerHeader);
        usernameHeader = view.findViewById(R.id.profileUsernameText);

        navigationView.setNavigationItemSelectedListener(this);

        sendImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPost();
            }
        });

        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }

    private void addPost() {
        postDesc = postDescInput.getText().toString();
        if (postDesc.isEmpty()) {
            postDescInput.setError("Please write something in post description!");
        } else if (postDesc.length() < 3 || postDesc.length() > 250) {
            postDescInput.setError("Please keep post between 3 to 250 characters!");
        } else if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        } else {
            addingPostProgressBar.setVisibility(View.VISIBLE);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            final String stringDate = format.format(date);

            postImageReference.child(firebaseUser.getUid() + " " + stringDate).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        postImageReference.child(firebaseUser.getUid() + " " + stringDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("datePost", stringDate);
                                hashMap.put("postImageUrl", uri.toString());
                                hashMap.put("postDesc", postDesc);
                                hashMap.put("userProfileImageUrl", profileImageUrlData);
                                hashMap.put("username", usernameData);
                                firebasePostReference.child(firebaseUser.getUid() + " " + stringDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        addingPostProgressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Post added", Toast.LENGTH_SHORT).show();
                                            addImagePost.setImageResource(R.drawable.ic_add_post_image);
                                            postDescInput.setText("");
                                        } else {
                                            Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseUser == null) {
            sendToLogin();
        } else {
            firebaseUserReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        profileImageUrlData = snapshot.child("profileImage").getValue().toString();
                        usernameData = snapshot.child("username").getValue().toString();
                        Picasso.get().load(profileImageUrlData).into(profileImageHeader);
                        usernameHeader.setText(usernameData);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {

        } else if (item.getItemId() == R.id.profile) {

        } else if (item.getItemId() == R.id.friends) {

        } else if (item.getItemId() == R.id.addFindFriends) {

        } else if (item.getItemId() == R.id.chat) {

        } else if (item.getItemId() == R.id.logout) {
            firebaseAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Impossibru! How did you do this!", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }
}