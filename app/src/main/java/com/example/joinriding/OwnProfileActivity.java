package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.joinriding.adapters.AdapterPosts;
import com.example.joinriding.models.ModelPosts;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class OwnProfileActivity extends AppCompatActivity {
    // Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser fuser;
    private FirebaseDatabase databaseUser, databaseCycle;
    private DatabaseReference referenceUser, referenceCycle;

    // Declare View Linear Layout
    private LinearLayout infouser, infomotor, infopost;
    private TextView infouserbtn, infomotorbtn, infopostbtn;

    // Declare View
    private ImageView photoUser,photoCycle;
    private TextView nameUser, idUser, phoneUser, genderUser, addressUser;
    private TextView nameCycle, platCycle, yearCycle;

    // Declare
    private ProgressDialog progressDialog;
    private RecyclerView recyclerViewPostProfile;
    private List<ModelPosts> postsList;
    private AdapterPosts adapterPosts;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_profile);

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init firebase
        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();

        databaseUser = FirebaseDatabase.getInstance();
        databaseCycle = FirebaseDatabase.getInstance();
        referenceUser = databaseUser.getReference("TBL_USERS");
        referenceCycle = databaseCycle.getReference("TBL_CYCLE");

        progressDialog = new ProgressDialog(OwnProfileActivity.this);
        recyclerViewPostProfile = findViewById(R.id.recyclerViewPostProfile);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        // Init View Linear Layout
        infouser = findViewById(R.id.infoUser);
        infomotor = findViewById(R.id.infoMotor);
        infopost = findViewById(R.id.infoPost);
        infouserbtn = findViewById(R.id.infoUserBtn);
        infomotorbtn = findViewById(R.id.infoMotorBtn);
        infopostbtn = findViewById(R.id.infoPostBtn);

        // init view xml
        photoUser = findViewById(R.id.photoUserProfile);
        nameUser = findViewById(R.id.nameUserProfile);
        idUser = findViewById(R.id.idUserProfile);
        phoneUser = findViewById(R.id.phoneUserProfile);
        genderUser = findViewById(R.id.genderUserProfile);
        addressUser = findViewById(R.id.addressUserProfile);

        nameCycle = findViewById(R.id.cycleUserProfile);
        platCycle = findViewById(R.id.platUserProfile);
        yearCycle = findViewById(R.id.yearUserProfile);

        ////////////////////////////////////////////////////////////////////////////////////////////
        /*making layout visible*/
        infouser.setVisibility(View.VISIBLE);
        infomotor.setVisibility(View.GONE);
        infopost.setVisibility(View.GONE);

        infouserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.VISIBLE);
                infomotor.setVisibility(View.GONE);
                infopost.setVisibility(View.GONE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));

            }
        });

        infomotorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.GONE);
                infomotor.setVisibility(View.VISIBLE);
                infopost.setVisibility(View.GONE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));

            }
        });

        infopostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                infouser.setVisibility(View.GONE);
                infomotor.setVisibility(View.GONE);
                infopost.setVisibility(View.VISIBLE);
                infouserbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infomotorbtn.setTextColor(getResources().getColor(R.color.colorGreyDark));
                infopostbtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

            }
        });
    ////////////////////////////////////////////////////////////////////////////////////////////////
        // get uid of clicked user to review his post

        Query queryUser = referenceUser.orderByChild("uid").equalTo(uid);
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+ snapshot.child("photoUser").getValue();
                    String name = ""+ snapshot.child("nameUser").getValue();
                    String id = ""+ snapshot.child("uid").getValue();
                    String phone = ""+ snapshot.child("phoneUser").getValue();
                    String gender = ""+ snapshot.child("genderUser").getValue();
                    String address= ""+ snapshot.child("addressUser").getValue();

                    nameUser.setText(name);
                    idUser.setText(id);
                    phoneUser.setText(phone);
                    genderUser.setText(gender);
                    addressUser.setText(address);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Glide.with(getBaseContext())
                                    .load(photo)
                                    .error(R.drawable.icon_user_grey)
                                    .into(photoUser);
                        }
                        catch (Exception e){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postsList = new ArrayList<>();
        checkUserStatus();
        loadDataCycle();
        loadHisPost();
    }

    private void loadDataCycle() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        Query queryCycle = referenceCycle.orderByChild("uid").equalTo(fuser.getUid());
        queryCycle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photo = ""+ snapshot.child("photoCycle").getValue();
                    String name = ""+ snapshot.child("nameCycle").getValue();
                    String plat = ""+ snapshot.child("platCycle").getValue();
                    String year = ""+ snapshot.child("yearCycle").getValue();

                    nameCycle.setText(name);
                    platCycle.setText(plat);
                    yearCycle.setText(year);

                    // if image equal no image
                    if (!photo.equals("noImage")){
                        try {
                            // if image success set
                            Picasso.get().load(photo).resize(50, 50).centerCrop().into(photoCycle);
                        }
                        catch (Exception e){
                            // if failed set default
                            Picasso.get().load(R.drawable.icon_user_grey).resize(50, 50).centerCrop().into(photoUser);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadHisPost() {
        // linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // show newst post
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // set thisto recyclerview
        recyclerViewPostProfile.setLayoutManager(layoutManager);

        // init post list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // query to load post
        Query query = reference.orderByChild("uid").equalTo(uid);
        // get all data from reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelPosts myPost = snapshot.getValue(ModelPosts.class);

                    // add to list
                    postsList.add(myPost);
                    // adapter
                    adapterPosts = new AdapterPosts(OwnProfileActivity.this, postsList);
                    // set this adapter to recycler view
                    recyclerViewPostProfile.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OwnProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed(); // go previous activity
        return super.onSupportNavigateUp();
    }

    // inflate optionmenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflated menu
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);

        // Hide Some Menu
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
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
