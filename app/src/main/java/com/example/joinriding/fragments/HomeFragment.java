package com.example.joinriding.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.joinriding.AddPostActivity;
import com.example.joinriding.MainActivity;
import com.example.joinriding.NotificationActivity;
import com.example.joinriding.R;
import com.example.joinriding.SettingsActivity;
import com.example.joinriding.adapters.AdapterPosts;
import com.example.joinriding.models.ModelPosts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.mikepenz.actionitembadge.library.utils.NumberUtils;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    // FirebaseAuth
    private FirebaseAuth mAuth;

    // Declare RecyclerView, List, Adapter
    private RecyclerView recyclerViewPostLoad;
    private List<ModelPosts> postsList;
    private AdapterPosts adapterPosts;

    // swipe layout
    private SwipeRefreshLayout swipeRefreshLayout;

    BadgeStyle style;
    private int badgeCount = 10;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // init swipe layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.primary);

        // init badge
        style = ActionItemBadge.BadgeStyles.RED.getStyle();
        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Init Array List
        recyclerViewPostLoad = view.findViewById(R.id.recyclerViewPost);
        postsList = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        // Init Load Post
                        loadPost();
                    }
                }, 5000);
            }
        });
        loadPost();
        return view;
    }

    private void loadPost() {
        // Recycler View Linear Layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // Show Newest Post First, FOr This Load From Last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set Layout To Recycler View
        recyclerViewPostLoad.setLayoutManager(layoutManager);
        // Patch Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // Get All Data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelPosts modelPosts = snapshot.getValue(ModelPosts.class);

                    postsList.add(modelPosts);
                }
                // Adapter Posts
                adapterPosts = new AdapterPosts(getActivity(), postsList);
                // Set Adapter To RecyclerView
                recyclerViewPostLoad.setAdapter(adapterPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchPost(final String searchQuery){
        // Patch Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // Get All Data From Database
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelPosts modelPosts = snapshot.getValue(ModelPosts.class);

                    assert modelPosts != null;
                    if (modelPosts.getNamePosting().toLowerCase().contains(searchQuery.toLowerCase())
                            || modelPosts.getDescriptionPosting().toLowerCase().contains(searchQuery.toLowerCase())){
                        postsList.add(modelPosts);
                    }
                    // Adapter Posts
                    adapterPosts = new AdapterPosts(getActivity(), postsList);
                    // Set Adapter To RecyclerView
                    recyclerViewPostLoad.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){

        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu action in fragment
        super.onCreate(savedInstanceState);
    }

    // Inflate Option Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflated Menu
        inflater.inflate(R.menu.top_nav_menu, menu);

        // Hide Some Menu
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

        if (badgeCount > 0) {
            ActionItemBadge.update(getActivity(), menu.findItem(R.id.action_notification), FontAwesome.Icon.faw_android, ActionItemBadge.BadgeStyles.RED, badgeCount);
        } else {
            ActionItemBadge.hide(menu.findItem(R.id.action_notification));
        }

        // Search View By Title And Description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // Search Listener

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it
                    searchPost(s);
                }else{
                    // search text empty, get all users
                    loadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it
                    searchPost(s);
                }else{
                    // search text empty, get all users
                    loadPost();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }else if(id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }else if(id == R.id.action_notification){
            ActionItemBadge.update(item, badgeCount);
            startActivity(new Intent(getActivity(), NotificationActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
