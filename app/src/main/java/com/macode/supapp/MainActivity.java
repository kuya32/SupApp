package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.macode.supapp.utilities.CommentViewHolder;
import com.macode.supapp.utilities.Comments;
import com.macode.supapp.utilities.MyViewHolder;
import com.macode.supapp.utilities.Posts;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
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
    private DatabaseReference firebaseUserReference, firebasePostReference, likeReference, commentReference;
    private StorageReference postImageReference;
    private String profileImageUrlData, usernameData, postDesc;
    private CircleImageView profileImageHeader;
    private TextView usernameHeader;
    private ImageView addImagePost, sendImagePost;
    private EditText postDescInput;
    private Uri imageUri;
    private FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    private FirebaseRecyclerAdapter<Comments, CommentViewHolder> commentAdapter;
    private FirebaseRecyclerOptions<Posts> options;
    private FirebaseRecyclerOptions<Comments> commentOptions;
    private RecyclerView recyclerView;

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
        recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        firebasePostReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentReference = FirebaseDatabase.getInstance().getReference().child("Comments");
        postImageReference = FirebaseStorage.getInstance().getReference().child("PostImages");

        FirebaseMessaging.getInstance().subscribeToTopic(firebaseUser.getUid());

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

        loadPosts();
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

    private void loadPosts() {
        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(firebasePostReference, Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {
                final String postKey = getRef(position).getKey();
                holder.postDesc.setText(model.getPostDesc());
                String timeAgo = calculateTimeAgo(model.getDatePost());
                holder.timeAgo.setText(timeAgo);
                holder.username.setText(model.getUsername());
                Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImageUrl()).into(holder.profileImage);
                holder.countLikes(postKey, firebaseUser.getUid(), likeReference);
                holder.countComments(postKey, firebaseUser.getUid(), commentReference);

                holder.imageLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeReference.child(postKey).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    likeReference.child(postKey).child(firebaseUser.getUid()).removeValue();
                                    holder.imageLikeButton.setColorFilter(Color.GRAY);
                                } else {
                                    likeReference.child(postKey).child(firebaseUser.getUid()).setValue("like");
                                    holder.imageLikeButton.setColorFilter(Color.rgb(0, 201, 255));
                                }
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                holder.postCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String comment = holder.commentInput.getText().toString();
                        if (comment.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Please write something in the comment area!", Toast.LENGTH_SHORT).show();
                        } else {
                            Date date = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                            final String stringDate = format.format(date);
                            addComment(holder, postKey, commentReference, firebaseUser.getUid(), comment, stringDate);
                        }
                    }
                });
                loadComments(postKey);
                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                        intent.putExtra("url", model.getPostImageUrl());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post_view, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void loadComments(String postKey) {
        MyViewHolder.commentRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        commentOptions = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(commentReference.child(postKey), Comments.class).build();
        commentAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(commentOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comments model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageComment);
                holder.commentUsername.setText(model.getUsername());
                String timeAgo = calculateTimeAgo(model.getCommentDate());
                holder.commentTimeAgo.setText(timeAgo);
                holder.comment.setText(model.getComment());
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment, parent, false);
                return new CommentViewHolder(view);
            }
        };
        commentAdapter.startListening();
        MyViewHolder.commentRecyclerView.setAdapter(commentAdapter);
    }

    private void addComment(MyViewHolder holder, String postKey, DatabaseReference commentReference, String uid, String comment, String stringDate) {
        HashMap hashMap = new HashMap();
        hashMap.put("commentDate", stringDate);
        hashMap.put("username", usernameData);
        hashMap.put("profileImageUrl", profileImageUrlData);
        hashMap.put("comment", comment);

        commentReference.child(postKey).child(uid + " " + stringDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    holder.commentInput.setText(null);
                } else {
                    Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private String calculateTimeAgo(String datePost) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        try {
            long time = simpleDateFormat.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {

        } else if (item.getItemId() == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.friends) {
            Intent intent = new Intent(MainActivity.this, FriendActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.addFindFriends) {
            Intent intent = new Intent(MainActivity.this, FindFriendActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.chat) {
            Intent intent = new Intent(MainActivity.this, ChatUsersActivity.class);
            startActivity(intent);
            finish();
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