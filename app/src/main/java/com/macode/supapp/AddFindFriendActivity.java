package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.macode.supapp.utilities.AddFindFriendViewHolder;
import com.macode.supapp.utilities.Users;
import com.squareup.picasso.Picasso;

public class AddFindFriendActivity extends AppCompatActivity {

    private FirebaseRecyclerOptions<Users> userOptions;
    private FirebaseRecyclerAdapter<Users, AddFindFriendViewHolder> userAdapter;
    private Toolbar toolbar;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private RecyclerView addFindFriendRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_find_friend);

        toolbar = findViewById(R.id.findFriendAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add/Find Friends");
        addFindFriendRecyclerView = findViewById(R.id.findFriendRecyclerView);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loadUsers("");
    }

    private void loadUsers(String string) {
        Query query = userReference.orderByChild("username").startAt(string).endAt(string + "\uf8ff");
        userOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();
        userAdapter = new FirebaseRecyclerAdapter<Users, AddFindFriendViewHolder>(userOptions) {
            @Override
            protected void onBindViewHolder(@NonNull AddFindFriendViewHolder holder, int position, @NonNull Users model) {
                Picasso.get().load(model.getProfileImage()).into(holder.findFriendProfileImage);
                holder.findFriendUsername.setText(model.getUsername());
                holder.findFriendProfession.setText(model.getProfession());
            }

            @NonNull
            @Override
            public AddFindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_find_friend, parent, false);
                return new AddFindFriendViewHolder(view);
            }
        };
        userAdapter.startListening();
        addFindFriendRecyclerView.setAdapter(userAdapter);
    }
}