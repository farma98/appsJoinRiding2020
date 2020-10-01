package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.joinriding.adapters.AdapterUserAdd;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChatAddUserActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAddUser;
    private ActionBar actionBar;
    private FirebaseAuth mAuth;
    private String groupId, myGroupRole;

    private ArrayList<ModelMyListUsers> modelUsersList;
    private AdapterUserAdd adapterUserAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_add_user);

        // Action bar and ist title
        actionBar = getSupportActionBar();

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        // init views
        recyclerViewAddUser = findViewById(R.id.recyclerViewAddUser);
        groupId = getIntent().getStringExtra("idGroup");
        loadGroupInfo();
    }

    private void getAllUserGroup() {
        // init list
        modelUsersList = new ArrayList<>();
        // load user from database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelUsersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelMyListUsers modelUsers = snapshot.getValue(ModelMyListUsers.class);

                    // get all usrs accpet current sign in
                    if (!mAuth.getUid().equals(modelUsers.getUid())){
                        // not my uid
                        modelUsersList.add(modelUsers);
                    }
                }
                // set adapter
                adapterUserAdd = new AdapterUserAdd(GroupChatAddUserActivity.this, modelUsersList, ""+groupId, ""+myGroupRole);
                // set adapter to recyclerview
                recyclerViewAddUser.setAdapter(adapterUserAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.orderByChild("idGroup").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String groupIds = ""+snapshot.child("idGroup").getValue();
                    final String groupTitle = ""+snapshot.child("nameGroup").getValue();
                    String groupDescription = ""+snapshot.child("descriptionGroup").getValue();
                    String groupIcon = ""+snapshot.child("iconGroup").getValue();
                    String timestamp = ""+snapshot.child("timegroup").getValue();
                    String createdBy = ""+snapshot.child("createdGroup").getValue();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                    reference.child(groupIds).child("Participants").child(mAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        myGroupRole = ""+dataSnapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle+"("+myGroupRole+")");

                                        getAllUserGroup();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
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
