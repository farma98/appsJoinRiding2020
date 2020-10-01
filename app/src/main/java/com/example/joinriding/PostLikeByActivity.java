package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.joinriding.adapters.AdapterLikedBy;
import com.example.joinriding.adapters.AdapterMyListUsers;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLikeByActivity extends AppCompatActivity {
    // Declare
    private String postId;
    private RecyclerView recyclerViewLikeBy;
    private List<ModelMyListUsers> usersList;
    private AdapterLikedBy adapterLikedBy;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like_by);

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Disukai Oleh :");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        // get parameter post id
        Intent myIntent = getIntent();
        postId = myIntent.getStringExtra("postId");

        recyclerViewLikeBy = findViewById(R.id.recyclerViewLikeBy);

        usersList = new ArrayList<>();

        // get the list Uid of users who liked post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_LIKES");
        reference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String hisUid = ""+snapshot.getRef().getKey();
                    // get user info
                    getUser(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUser(String hisUid) {
        // get info for each user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ModelMyListUsers modelUsers = snapshot.getValue(ModelMyListUsers.class);
                            usersList.add(modelUsers);
                        }
                        // set adapter
                        adapterLikedBy = new AdapterLikedBy(PostLikeByActivity.this, usersList);
                        // set adapter to recylerview
                        recyclerViewLikeBy.setAdapter(adapterLikedBy);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
