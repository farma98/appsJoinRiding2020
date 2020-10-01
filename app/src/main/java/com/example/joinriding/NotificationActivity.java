package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.example.joinriding.adapters.AdapterNotifications;
import com.example.joinriding.models.ModelNotifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {
    // Declare Recycleview
    private RecyclerView notificationRecycelerView;
    private FirebaseAuth mAuth;
    private ArrayList<ModelNotifications> notificationsList;
    private AdapterNotifications adapterNotifications;
    private String uid;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // init swipe layout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        notificationRecycelerView = findViewById(R.id.recyclerViewNotification);

        mAuth = FirebaseAuth.getInstance();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        getAllNotif();
                    }
                }, 5000);
            }
        });


        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
            uid = user.getUid();
        }else{
            //user not signed,go to main
            startActivity(new Intent(NotificationActivity.this, MainActivity.class));
            finish();
        }
    }

    private void getAllNotif() {
        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Daftar Notification");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        notificationsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child("Notifications").child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notificationsList.clear();
                        for (DataSnapshot snapshot  : dataSnapshot.getChildren()){
                            ModelNotifications modelNotifications = snapshot.getValue(ModelNotifications.class);

                            // add to list
                            notificationsList.add(modelNotifications);
                        }

                        // adapter
                        adapterNotifications = new AdapterNotifications(NotificationActivity.this, notificationsList);
                        notificationRecycelerView.setAdapter(adapterNotifications);
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

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_settings){
            startActivity(new Intent(NotificationActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
