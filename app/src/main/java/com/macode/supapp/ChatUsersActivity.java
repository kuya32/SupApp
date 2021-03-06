package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.macode.supapp.utilities.Friends;
import com.macode.supapp.utilities.FriendsViewHolder;
import com.squareup.picasso.Picasso;

public class ChatUsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView chatUsersRecyclerView;
    private FirebaseRecyclerOptions<Friends> friendsOptions;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsAdapter;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        toolbar = findViewById(R.id.chatUsersAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat with Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatUsersRecyclerView = findViewById(R.id.chatUsersRecyclerView);
        chatUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loadFriends("");
    }

    private void loadFriends(String s) {
        Query query = databaseReference.child(firebaseUser.getUid()).orderByChild("username").startAt(s).endAt(s + "\uf8ff");
        friendsOptions = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        friendsAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageUrl);
                holder.username.setText(model.getUsername());
                holder.profession.setText(model.getProfession());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatUsersActivity.this, ChatActivity.class);
                        intent.putExtra("otherUserId", getRef(position).getKey().toString());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);

                return new FriendsViewHolder(view);
            }
        };
        friendsAdapter.startListening();
        chatUsersRecyclerView.setAdapter(friendsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}