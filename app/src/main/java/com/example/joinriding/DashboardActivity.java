package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.joinriding.fragments.ChatsListFragment;
import com.example.joinriding.fragments.HomeFragment;
import com.example.joinriding.fragments.MapsFragment;
import com.example.joinriding.fragments.ProfileFragment;
import com.example.joinriding.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    // Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser fuser;

    // Declare
    private ActionBar actionBar;
    private BottomNavigationView botNavView;

    // Declare String Custom
    private String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Init Action bar
        actionBar = getSupportActionBar();

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Init Bottom Navigation
        botNavView = findViewById(R.id.navigationBottom);
        botNavView.setOnNavigationItemSelectedListener(selectedListener);

        // Home Fragment To Default Layout Load
        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.contentFrame, fragment1, "");
        ft1.commit();

        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("TBL_TOKENS");
        Token mToken = new Token(token);
        dbReference.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // handle item clicks
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            // Home Fragment Layout
                            actionBar.setTitle("Home");
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.contentFrame, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_maps:
                            // Profile Fragment Layout
                            actionBar.setTitle("Maps");
                            MapsFragment fragment2 = new MapsFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.contentFrame, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_chat:
                            // Chat Fragment Layout
                            actionBar.setTitle("Chat");
                            ChatsListFragment fragment3 = new ChatsListFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.contentFrame, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_profile:
                            // Profile Fragment Layout
                            actionBar.setTitle("Profile");
                            ProfileFragment fragment4 = new ProfileFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.contentFrame, fragment4, "");
                            ft4.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
            mUID = user.getUid();

            SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            // update token
            updateToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            //user not signed,go to main
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
