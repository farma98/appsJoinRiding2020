package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinriding.adapters.AdapterUserAdd;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class GroupInfoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private String groupId;
    private String myGroupRole = "";
    private ActionBar actionBar;

    // views
    private ImageView groupIconQ;
    private TextView descriptionGroup, createdGroup, editGroup, addUserGroup, leaveGroup, userGroup;

    // adapter array list
    ArrayList<ModelMyListUsers> modelUsersList;
    AdapterUserAdd adapterUserAdd;

    RecyclerView recyclerViewInfoGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        // Action bar and ist title
        actionBar = getSupportActionBar();

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init views
        groupIconQ = findViewById(R.id.groupIconTv);
        descriptionGroup = findViewById(R.id.groupDescriptionTv);
        createdGroup = findViewById(R.id.createdByTv);
        editGroup = findViewById(R.id.editGroupTv);
        addUserGroup = findViewById(R.id.addUserTv);
        leaveGroup = findViewById(R.id.leaveGroupTv);
        userGroup = findViewById(R.id.userGroupTv);
        recyclerViewInfoGroup = findViewById(R.id.recyclerViewUserList);


        groupId = getIntent().getStringExtra("idGroup");

        mAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

        addUserGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // click next group add user list
                Intent myIntent = new Intent(GroupInfoActivity.this, GroupChatAddUserActivity.class);
                myIntent.putExtra("idGroup", groupId);
                startActivity(myIntent);
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(GroupInfoActivity.this, CreateGroupActivity.class);
                myIntent.putExtra("key", "editGroup");
                myIntent.putExtra("editGroupId", groupId);
                startActivity(myIntent);
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if user creator delete group
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";

                if (myGroupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are You Sure Delete This Group?";
                    positiveButtonTitle = "DELETE";
                }else{
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are You Sure Leave This Group?";
                    positiveButtonTitle = "LEAVE";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (myGroupRole.equals("creator")){
                                    deleteGroup();
                                }else{
                                    leaveGroup();
                                }
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(mAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Left The Group Success..", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Delete The Group Success..", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createdBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String name = ""+snapshot.child("nameUser").getValue();
                    createdGroup.setText("Created By : "+name+ " on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            myGroupRole = ""+snapshot.child("role").getValue();
                            actionBar.setSubtitle("("+myGroupRole+")");

                            if (myGroupRole.equals("participants")){
                                editGroup.setVisibility(View.GONE);
                                addUserGroup.setVisibility(View.GONE);
                                leaveGroup.setText("Leave Group");
                            }else if (myGroupRole.equals("admin")){
                                editGroup.setVisibility(View.GONE);
                                addUserGroup.setVisibility(View.VISIBLE);
                                leaveGroup.setText("Leave Group");
                            }else if (myGroupRole.equals("creator")){
                                editGroup.setVisibility(View.VISIBLE);
                                addUserGroup.setVisibility(View.VISIBLE);
                                leaveGroup.setText("Delete Group");
                            }
                        }
                        loadUserGroup();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadUserGroup() {
        modelUsersList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelUsersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get uid from Group > Participants
                    String uid = ""+snapshot.child("uid").getValue();

                    // get info user using uid git above
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
                    reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                ModelMyListUsers modelUsers = snapshot.getValue(ModelMyListUsers.class);
                                modelUsersList.add(modelUsers);
                            }

                            // adapter
                            adapterUserAdd = new AdapterUserAdd(GroupInfoActivity.this, modelUsersList, groupId, myGroupRole);
                            // set adapter
                            recyclerViewInfoGroup.setAdapter(adapterUserAdd);
                            userGroup.setText("Participants("+modelUsersList.size()+")");
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

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.orderByChild("idGroup").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String groupId = ""+snapshot.child("idGroup").getValue();
                            String groupTitle = ""+snapshot.child("nameGroup").getValue();
                            String groupDescription = ""+snapshot.child("descriptionGroup").getValue();
                            String groupIcon = ""+snapshot.child("photoGroup").getValue();
                            String timestamp = ""+snapshot.child("timeGroup").getValue();
                            String createdBy = ""+snapshot.child("createdGroup").getValue();

                            // convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

                            loadCreatorInfo(dateTime, createdBy);

                            // set group info
                            actionBar.setTitle(groupTitle);
                            descriptionGroup.setText(groupDescription);

                            try{
                                Picasso.get().load(groupIcon).placeholder(R.drawable.icon_group_grey).into(groupIconQ);
                            }catch (Exception e){
                                groupIconQ.setImageResource(R.drawable.icon_group_grey);
                            }


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
