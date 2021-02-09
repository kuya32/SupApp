package com.macode.supapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.macode.supapp.utilities.FindFriendViewHolder;
import com.macode.supapp.utilities.Users;
import com.squareup.picasso.Picasso;

public class FindFriendActivity extends AppCompatActivity {

    private FirebaseRecyclerOptions<Users> userOptions;
    private FirebaseRecyclerAdapter<Users, FindFriendViewHolder> userAdapter;
    private Toolbar toolbar;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private RecyclerView addFindFriendRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        toolbar = findViewById(R.id.findFriendAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addFindFriendRecyclerView = findViewById(R.id.findFriendRecyclerView);
        addFindFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loadUsers("");
    }

    private void loadUsers(String string) {
        Query query = userReference.orderByChild("username").startAt(string).endAt(string + "\uf8ff");
        userOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();
        userAdapter = new FirebaseRecyclerAdapter<Users, FindFriendViewHolder>(userOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull Users model) {
                if (!firebaseUser.getUid().equals(getRef(position).getKey().toString())) {
                    Picasso.get().load(model.getProfileImage()).into(holder.findFriendProfileImage);
                    holder.findFriendUsername.setText(model.getUsername());
                    holder.findFriendProfession.setText(model.getProfession());
                    holder.findFriendStatus.setText(model.getStatus());
                    if (model.getStatus().equals("Online")) {
                        holder.statusIndicatorImage.setColorFilter(Color.GREEN);
                    }
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FindFriendActivity.this, ViewFriendActivity.class);
                        intent.putExtra("userKey", getRef(position).getKey().toString());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_find_friend, parent, false);
                return new FindFriendViewHolder(view);
            }
        };
        userAdapter.startListening();
        addFindFriendRecyclerView.setAdapter(userAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadUsers(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(FindFriendActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}