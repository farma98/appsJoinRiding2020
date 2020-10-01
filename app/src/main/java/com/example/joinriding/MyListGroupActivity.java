package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
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

import com.example.joinriding.adapters.AdapterGroupList;
import com.example.joinriding.models.ModelGroupList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyListGroupActivity extends AppCompatActivity {
    private RecyclerView recyclerViewGroupList;
    private FirebaseAuth mAuth;

    private ArrayList<ModelGroupList> modelGroupLists;
    private AdapterGroupList adapterGroupList;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list_group);

        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Daftar Grup");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init swipe layout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        recyclerViewGroupList = findViewById(R.id.recyclerViewGroupList);
        mAuth = FirebaseAuth.getInstance();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        // Init Load Post
                        loadGroupList();
                    }
                }, 5000);
            }
        });
        loadGroupList();
    }

    private void loadGroupList() {
        modelGroupLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("Participants").child(mAuth.getUid()).exists()){
                        ModelGroupList modelGroupChatList = snapshot.getValue(ModelGroupList.class);
                        modelGroupLists.add(modelGroupChatList);
                    }
                }
                adapterGroupList = new AdapterGroupList(MyListGroupActivity.this, modelGroupLists);
                recyclerViewGroupList.setAdapter(adapterGroupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchGroupList(final String query) {
        modelGroupLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("Participants").child(mAuth.getUid()).exists()){
                        // serach by title
                        if (snapshot.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupList modelGroupChatList = snapshot.getValue(ModelGroupList.class);
                            modelGroupLists.add(modelGroupChatList);
                        }
                    }
                }
                adapterGroupList = new AdapterGroupList(MyListGroupActivity.this, modelGroupLists);
                recyclerViewGroupList.setAdapter(adapterGroupList);
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
            startActivity(new Intent(MyListGroupActivity.this, MainActivity.class));
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
                    searchGroupList(s);
                }else{
                    // search text empty, get all users
                    loadGroupList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called whenever user press any single letter
                // if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it
                    searchGroupList(s);
                }else{
                    // search text empty, get all users
                    loadGroupList();
                }
                return false;
            }
        });

        return true;
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
        }else if(id == R.id.action_create_group){
            startActivity(new Intent(MyListGroupActivity.this, CreateGroupActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
