package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.example.joinriding.adapters.AdapterMyListUsers;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyListUsersActivity extends AppCompatActivity {
    // Declare
    RecyclerView recyclerViewMyListUsers;
    AdapterMyListUsers adapterMyListUsers;
    List<ModelMyListUsers> modelMyListUsers;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;

    // Declare Firebase Auth
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list_users);


        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Daftar User");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init swipe layout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // init recycler view
        recyclerViewMyListUsers = findViewById(R.id.myListUser);
        // set
        recyclerViewMyListUsers.setHasFixedSize(true);
        recyclerViewMyListUsers.setLayoutManager(new LinearLayoutManager(MyListUsersActivity.this));

        // init user list
        modelMyListUsers = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        // Init Load Post
                        // get all users
                        getAllUsers();
                    }
                }, 5000);
            }
        });
        getAllUsers();
        checkUserStatus();
    }


    private void getAllUsers() {
        // Get Current
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        // Get Patch
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        // Get All Data From Patch
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelMyListUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelMyListUsers modelUsers = snapshot.getValue(ModelMyListUsers.class);

                    // get all users excpet currently signed in user
                    if (!modelUsers.getUid().equals(fuser.getUid())){
                        modelMyListUsers.add(modelUsers);
                    }

                    // adapter
                    adapterMyListUsers = new AdapterMyListUsers(MyListUsersActivity.this, modelMyListUsers);
                    // set adapter to recyclerview
                    recyclerViewMyListUsers.setAdapter(adapterMyListUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {
        // get current user
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        // get patch Users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        // get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelMyListUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelMyListUsers modelUsers = snapshot.getValue(ModelMyListUsers.class);

                    // get all search users excpet currently signed in user
                    if (!modelUsers.getUid().equals(fuser.getUid())){

                        if (modelUsers.getNameUser().toLowerCase().contains(query.toLowerCase())){
                            modelMyListUsers.add(modelUsers);
                        }
                    }

                    // adapter
                    adapterMyListUsers = new AdapterMyListUsers(MyListUsersActivity.this, modelMyListUsers);
                    // refresh adapter
                    adapterMyListUsers.notifyDataSetChanged();
                    // set adapter to recyclerview
                    recyclerViewMyListUsers.setAdapter(adapterMyListUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
        }else{
            //user not signed,go to main
            startActivity(new Intent(MyListUsersActivity.this, MainActivity.class));
            finish();
        }
    }

    // inflate optionmenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflated menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_menu, menu);

        // hide addpost icon
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_notification).setVisible(false);

        // Search View
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // called when user press search button from keyboard
                // if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it
                    searchUsers(s);
                }else{
                    // search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called whenever user press any single letter
                // if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it
                    searchUsers(s);
                }else{
                    // search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });

        return true;
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
            startActivity(new Intent(MyListUsersActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go Previous Activity
        return super.onSupportNavigateUp();
    }
}
